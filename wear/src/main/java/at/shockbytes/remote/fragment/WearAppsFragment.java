package at.shockbytes.remote.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.wear.widget.WearableLinearLayoutManager;
import android.support.wear.widget.WearableRecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import at.shockbytes.remote.R;
import at.shockbytes.remote.adapter.WearAppsAdapter;
import at.shockbytes.remote.communication.CommunicationManager;
import at.shockbytes.remote.core.WearRemiApp;
import at.shockbytes.util.adapter.BaseAdapter;
import butterknife.BindView;
import io.reactivex.functions.Consumer;

/**
 * @author Martin Macheiner
 *         Date: 22.10.2017.
 */

public class WearAppsFragment extends WearBaseFragment
        implements BaseAdapter.OnItemClickListener<String> {

    public static WearAppsFragment newInstance() {
        WearAppsFragment fragment = new WearAppsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @BindView(R.id.wear_fragment_apps_rv)
    protected WearableRecyclerView recyclerView;

    @Inject
    protected CommunicationManager gateway;

    private WearAppsAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((WearRemiApp) getActivity().getApplication()).getAppComponent().inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.wear_fragment_apps, container, false);
    }

    @Override
    protected void setupViews() {

        adapter = new WearAppsAdapter(getContext(), new ArrayList<String>());
        adapter.setOnItemClickListener(this);
        recyclerView.setLayoutManager(new WearableLinearLayoutManager(getContext()));
        recyclerView.setCircularScrollingGestureEnabled(true);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        gateway.requestApps().subscribe(new Consumer<List<String>>() {
            @Override
            public void accept(List<String> apps) throws Exception {
                adapter.setData(apps);
            }
        });

        //adapter.setData(Arrays.asList("Stronghold Crusader", "Android Studio", "Eclipse", "IntelliJ IDEA"));
    }

    @Override
    public void onItemClick(String app, View view) {

        Toast.makeText(getContext(), "Start: " + app, Toast.LENGTH_SHORT).show();
        gateway.sendAppStartMessage(app);
    }
}
