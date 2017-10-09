package at.shockbytes.remote.fragment;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;

import java.util.ArrayList;
import java.util.Arrays;

import javax.inject.Inject;

import at.shockbytes.remote.R;
import at.shockbytes.remote.adapter.DesktopAppsAdapter;
import at.shockbytes.remote.core.RemiApp;
import at.shockbytes.remote.network.RemiClient;
import at.shockbytes.remote.network.model.DesktopApp;
import at.shockbytes.remote.util.AppParams;
import at.shockbytes.remote.util.RemiUtils;
import at.shockbytes.util.adapter.BaseAdapter;
import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnLongClick;
import io.reactivex.functions.Consumer;

public class LoginFragment extends BaseFragment
        implements BaseAdapter.OnItemClickListener<DesktopApp> {

    public interface OnConnectionResponseListener {

        void onConnected(boolean isDebug);

        void onConnectionFailed(Throwable throwable);
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
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupViews();
    }

    @Override
    public void onItemClick(DesktopApp app, View view) {
        connectToDevice(RemiUtils.createUrlFromIp(app.getIp(), AppParams.STD_PORT, false));
    }

    @OnLongClick(R.id.fragment_login_imgview_icon)
    protected boolean onClickDebugEntryIcon() {

        listener.onConnected(true);
        return true;
    }

    @OnClick(R.id.fragment_login_btn_search)
    protected void onClickBtnSearch() {
        searchForDevices();
    }

    @Override
    protected void setupViews() {

        recyclerView.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        adapter = new DesktopAppsAdapter(getContext(), new ArrayList<DesktopApp>());
        adapter.setOnItemClickListener(this);
        recyclerView.setAdapter(adapter);
    }

    private void searchForDevices() {

        // TODO Search for devices
        adapter.setData(Arrays.asList(
                new DesktopApp("PC-Pickachu", "10.59.0.243", DesktopApp.OperatingSystem.WINDOWS),
                new DesktopApp("Localhost", "10.0.2.2", DesktopApp.OperatingSystem.WINDOWS),
                new DesktopApp("Liri OS", "192.168.1.89", DesktopApp.OperatingSystem.LINUX),
                new DesktopApp("Mac Mini", "192.128.0.40", DesktopApp.OperatingSystem.MAC_OS)));
        recyclerView.animate().alpha(1).scaleX(1).scaleY(1)
                .setInterpolator(new AccelerateDecelerateInterpolator()).setDuration(500).start();
    }

    private void connectToDevice(String url) {

        client.connect(url).subscribe(new Consumer<Object>() {
            @Override
            public void accept(Object o) throws Exception {
                listener.onConnected(false);
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) {
                throwable.printStackTrace();
                listener.onConnectionFailed(throwable);
            }
        });
    }

}
