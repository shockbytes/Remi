package at.shockbytes.remote.fragment;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;

import javax.inject.Inject;

import at.shockbytes.remote.R;
import at.shockbytes.remote.adapter.DesktopAppsAdapter;
import at.shockbytes.remote.core.AppTourActivity;
import at.shockbytes.remote.core.RemiApp;
import at.shockbytes.remote.fragment.dialog.AcceptDesktopConnectionDialogFragment;
import at.shockbytes.remote.fragment.dialog.DebugOptionsDialogFragment;
import at.shockbytes.remote.network.RemiClient;
import at.shockbytes.remote.network.discovery.ServiceFinder;
import at.shockbytes.remote.network.model.DesktopApp;
import at.shockbytes.remote.network.security.AndroidSecurityManager;
import at.shockbytes.remote.util.RemiUtils;
import at.shockbytes.util.adapter.BaseAdapter;
import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnLongClick;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

public class LoginFragment extends BaseFragment
        implements BaseAdapter.OnItemClickListener<DesktopApp> {

    public interface OnConnectionResponseListener {

        void onConnected();

        void onConnectionFailed(int resultCode);
    }

    public static LoginFragment newInstance() {
        LoginFragment fragment = new LoginFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public LoginFragment() {
    }

    @Inject
    protected RemiClient client;

    @Inject
    protected ServiceFinder serviceFinder;

    @Inject
    protected AndroidSecurityManager securityManager;

    @BindView(R.id.fragment_login_rv_desktop_apps)
    protected RecyclerView recyclerView;

    private DesktopAppsAdapter adapter;

    private OnConnectionResponseListener listener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((RemiApp) getActivity().getApplication()).getAppComponent().inject(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (OnConnectionResponseListener) context;
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onItemClick(DesktopApp app, View view) {

        if (app.isTrusted()) {
            connectToDevice(RemiUtils.createUrlFromIp(app.getIp(), client.getPort(),
                    client.isSSLEnabled()));
        } else {
            showAcceptConnectionDialogFragment(app);
        }

    }

    @OnLongClick(R.id.fragment_login_imgview_icon)
    protected boolean onClickDebugEntryIcon() {

        DebugOptionsDialogFragment fragment = DebugOptionsDialogFragment.Companion.newInstance();
        fragment.setListener(new DebugOptionsDialogFragment.OnDebugOptionSelectedListener() {
            @Override
            public void onDebugLoginClicked() {
                listener.onConnected();
            }

            @Override
            public void onFakeDevicesClicked() {
                adapter.setData(Arrays.asList(
                        new DesktopApp("Fake Windows", "192.168.0.2", "Windows", "asdfgh", false),
                        new DesktopApp("Fake, but trustworthy Linux", "192.168.0.3", "Linux", "asdfgh", true)
                ));
                animateRecyclerView();
            }

            @Override
            public void onRegenerateKeysClicked() {
                Toast.makeText(getContext(), "Re-generate keys", Toast.LENGTH_SHORT).show();
            }
        });
        fragment.show(getFragmentManager(), "debug-options-fragment");

        return true;
    }

    @OnClick(R.id.fragment_login_btn_lookup)
    protected void onClickBtnLookup() {

        serviceFinder.lookForDesktopApps()
                .map(new Function<DesktopApp, DesktopApp>() {
                    @Override
                    public DesktopApp apply(DesktopApp desktopApp) throws Exception {
                        desktopApp.setTrusted(securityManager.verifyDesktopApp(desktopApp));
                        return desktopApp;
                    }
                })
                .subscribe(new Consumer<DesktopApp>() {
                    @Override
                    public void accept(DesktopApp desktopApp) throws Exception {
                        adapter.addEntityAtFirst(desktopApp);
                        recyclerView.scrollToPosition(0);
                        animateRecyclerView();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        throwable.printStackTrace();
                    }
                });
    }

    @OnClick(R.id.fragment_login_imgbtn_apptour)
    protected void onClickAppTour() {
        ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity());
        startActivity(AppTourActivity.Companion.newIntent(getContext()), optionsCompat.toBundle());
    }

    @Override
    protected void setupViews() {

        recyclerView.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        adapter = new DesktopAppsAdapter(getContext(), new ArrayList<DesktopApp>());
        adapter.setOnItemClickListener(this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (adapter != null) {
            adapter.setData(new ArrayList<DesktopApp>());
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        serviceFinder.stopListening();
    }

    private void connectToDevice(String url) {

        client.connect(url).subscribe(new Consumer<Integer>() {
            @Override
            public void accept(Integer resultCode) throws Exception {

                if (resultCode == RemiClient.CONNECTION_RESULT_OK) {
                    listener.onConnected();
                } else {
                    listener.onConnectionFailed(resultCode);
                }
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) {
                throwable.printStackTrace();
                listener.onConnectionFailed(RemiClient.CONNECTION_RESULT_ERROR_NETWORK);
            }
        });
    }

    private void animateRecyclerView() {
        recyclerView.animate().alpha(1).scaleX(1).scaleY(1)
                .setInterpolator(new AccelerateDecelerateInterpolator()).setDuration(500)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        if (adapter.getItemCount() > 1) {
                            // TODO Not hardcode 80, rather use 1/10 or 1/5 of  recyclerview
                            recyclerView.smoothScrollBy(80, 0);
                        }
                    }
                })
                .start();
    }

    private void showAcceptConnectionDialogFragment(DesktopApp app) {

        AcceptDesktopConnectionDialogFragment fragment = AcceptDesktopConnectionDialogFragment
                .Companion.newInstance(app);
        fragment.setListener(new AcceptDesktopConnectionDialogFragment
                .OnAcceptDesktopConnectionListener() {
            @Override
            public void onAccept(@NonNull DesktopApp app) {
                adapter.updateEntity(app);
                connectToDevice(RemiUtils.createUrlFromIp(app.getIp(), client.getPort(),
                        client.isSSLEnabled()));
            }
        });
        fragment.show(getFragmentManager(), "accept-connection-fragment");
    }

}
