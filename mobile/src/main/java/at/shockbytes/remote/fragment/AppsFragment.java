package at.shockbytes.remote.fragment;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import at.shockbytes.remote.R;
import at.shockbytes.remote.adapter.AppsAdapter;
import at.shockbytes.remote.core.RemiApp;
import at.shockbytes.remote.network.RemiClient;
import at.shockbytes.util.adapter.BaseAdapter;
import butterknife.BindView;
import rx.Subscriber;
import rx.functions.Action1;
import rx.observers.SafeSubscriber;


public class AppsFragment extends BaseFragment
        implements BaseAdapter.OnItemClickListener<String>,
        AppsAdapter.OnOverflowMenuItemClickListener<String> {

    public static AppsFragment newInstance() {
        AppsFragment fragment = new AppsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Inject
    protected RemiClient client;

    @BindView(R.id.fragment_apps_rv)
    protected RecyclerView recyclerView;

    @BindView(R.id.fragment_apps_textview)
    protected TextView txtEmpty;

    private AppsAdapter adapter;

    private SafeSubscriber<List<String>> subscriber = new SafeSubscriber<>(new Subscriber<List<String>>() {
        @Override
        public void onCompleted() {

        }

        @Override
        public void onError(Throwable e) {
            e.printStackTrace();
            Snackbar.make(getView(), "Cannot request apps", Snackbar.LENGTH_LONG).show();
        }

        @Override
        public void onNext(List<String> apps) {
            txtEmpty.setAlpha(apps.size() == 0 ? 1 : 0);
            adapter.setData(apps);
        }
    });

    public AppsFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((RemiApp) getActivity().getApplication()).getAppComponent().inject(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_apps, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.wtf("Remi", getClass().toString() + " onViewCreated");
        setupViews();
    }

    @Override
    public void onStart() {
        super.onStart();
        client.requestApps().subscribe(subscriber);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (!subscriber.isUnsubscribed()) {
            subscriber.unsubscribe();
        }
    }

    @Override
    public void onItemClick(String app, View view) {
        client.sendAppExecutionRequest(app).subscribe();
        Snackbar.make(getView(), "Starte " + app, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onOverflowMenuItemClicked(int itemId, final String content) {

        if (itemId == R.id.popup_apps_item_remove) {
            client.removeApp(content).subscribe(new Action1<Void>() {
                @Override
                public void call(Void aVoid) {
                    adapter.deleteEntity(content);
                }
            });
        }
    }

    private void setupViews() {

        recyclerView.setLayoutManager(getLayoutManager());
        adapter = new AppsAdapter(getContext(), new ArrayList<String>());
        adapter.setOnItemClickListener(this);
        adapter.setOnOverflowMenuItemClickListener(this);
        recyclerView.setAdapter(adapter);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getContext(),
                DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(ContextCompat.getDrawable(getContext(),
                R.drawable.divider_recyclerview));
        recyclerView.addItemDecoration(dividerItemDecoration);
    }

    private RecyclerView.LayoutManager getLayoutManager() {
        return new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
    }

}