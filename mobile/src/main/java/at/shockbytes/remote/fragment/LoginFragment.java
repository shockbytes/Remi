package at.shockbytes.remote.fragment;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;

import javax.inject.Inject;

import at.shockbytes.remote.R;
import at.shockbytes.remote.adapter.DesktopAppsAdapter;
import at.shockbytes.remote.core.RemiApp;
import at.shockbytes.remote.debug.DebugOptions;
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

    public interface OnLoginActionListener {

        void onStartAppTour();

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

    private OnLoginActionListener listener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((RemiApp) getActivity().getApplication()).getAppComponent().inject(this);
        createKeysIfNecessary();
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
            listener = (OnLoginActionListener) context;
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onItemClick(DesktopApp app, View view) {

        if (app.isTrusted()) {
            connectToDevice(app);
        } else {
            showAcceptConnectionDialogFragment(app);
        }

    }

    @OnLongClick(R.id.fragment_login_imgview_icon)
    protected boolean onClickDebugEntryIcon() {

        DebugOptionsDialogFragment fragment = DebugOptionsDialogFragment.Companion.newInstance();
        fragment.setDebugListener(new DebugOptions.OnDebugOptionSelectedListener() {
            @Override
            public void onDebugOptionSelected(@NotNull DebugOptions.DebugAction action) {

                switch (action) {

                    case FAKE_LOGIN:
                        listener.onConnected();
                        break;
                    case FAKE_DEVICES:
                        adapter.setData(Arrays.asList(
                                new DesktopApp("Fake Windows", "192.168.0.2", "Windows", "asdfgh", false),
                                new DesktopApp("Fake, but trustworthy Linux", "192.168.0.3", "Linux", "asdfgh", true)
                        ));
                        animateRecyclerView();
                        break;
                    case REGENERATE_KEYS:
                        Toast.makeText(getContext(), "Re-generate keys", Toast.LENGTH_SHORT).show();
                        break;
                    case FORCE_UNAUTHORIZED_CONNECTION:
                        Toast.makeText(getContext(), "Unauthorized connection", Toast.LENGTH_SHORT).show();
                        break;

                    case RESET_KEY_STORES:
                        securityManager.reset().subscribe(new Consumer<RemiUtils.Irrelevant>() {
                            @Override
                            public void accept(RemiUtils.Irrelevant irrelevant) throws Exception {
                                Toast.makeText(getContext(), "Keystores reset!", Toast.LENGTH_SHORT).show();
                            }
                        });
                        break;
                }

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
        listener.onStartAppTour();
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

    private void connectToDevice(DesktopApp app) {

        client.connect(app).subscribe(new Consumer<Integer>() {
            @Override
            public void accept(Integer resultCode) throws Exception {

                if (resultCode == RemiClient.Companion.getCONNECTION_RESULT_OK()) {
                    listener.onConnected();
                } else {
                    listener.onConnectionFailed(resultCode);
                }
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) {
                throwable.printStackTrace();
                listener.onConnectionFailed(RemiClient.Companion.getCONNECTION_RESULT_ERROR_NETWORK());
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
                            recyclerView.smoothScrollBy(recyclerView.getWidth() / 5, 0);
                        }
                    }
                })
                .start();
    }

    private void createKeysIfNecessary() {
        if (!securityManager.hasKeys()) {
            securityManager.generateKeys().subscribe(new Consumer<RemiUtils.Irrelevant>() {
                @Override
                public void accept(RemiUtils.Irrelevant irrelevant) throws Exception {
                    Toast.makeText(getContext(), "Keys created and stored!", Toast.LENGTH_LONG).show();
                }
            }, new Consumer<Throwable>() {
                @Override
                public void accept(Throwable throwable) throws Exception {
                    throwable.printStackTrace();
                    Toast.makeText(getContext(), throwable.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void showAcceptConnectionDialogFragment(DesktopApp app) {
        AcceptDesktopConnectionDialogFragment fragment = AcceptDesktopConnectionDialogFragment
                .Companion.newInstance(app);
        fragment.setListener(new AcceptDesktopConnectionDialogFragment
                .OnAcceptDesktopConnectionListener() {
            @Override
            public void onAccept(@NonNull DesktopApp app) {
                adapter.updateEntity(app);
                connectToDevice(app);
            }
        });
        fragment.show(getFragmentManager(), "accept-connection-fragment");
    }

}
