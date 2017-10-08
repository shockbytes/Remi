package at.shockbytes.remote.fragment;


import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import at.shockbytes.remote.R;
import at.shockbytes.remote.adapter.FilesAdapter;
import at.shockbytes.remote.core.RemiApp;
import at.shockbytes.remote.network.RemiClient;
import at.shockbytes.remote.network.model.RemiFile;
import at.shockbytes.util.adapter.BaseAdapter;
import butterknife.BindView;

public class FilesFragment extends BaseFragment implements BaseAdapter.OnItemClickListener<RemiFile>, FilesAdapter.OnOverflowMenuItemClickListener<RemiFile> {

    public static FilesFragment newInstance() {
        FilesFragment fragment = new FilesFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Inject
    protected RemiClient client;

    @BindView(R.id.fragment_files_rv)
    protected RecyclerView recyclerView;

    private FilesAdapter adapter;

    public FilesFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((RemiApp) getActivity().getApplication()).getAppComponent().inject(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_files, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        /* TODO Enable later
        client.requestBaseDirectories().subscribe(new Action1<List<RemiFile>>() {
            @Override
            public void call(List<RemiFile> remiFiles) {
                adapter.setData(remiFiles);
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                throwable.printStackTrace();
                Snackbar.make(getView(), "Can't get files", Snackbar.LENGTH_SHORT).show();
            }
        }); */
    }

    @Override
    protected void setupViews() {

        // TODO Remove later
        List<RemiFile> remiFiles = new ArrayList<>();
        remiFiles.add(new RemiFile("C:/", "C:/", true, false));
        remiFiles.add(new RemiFile("D:/", "D:/", true, false));
        remiFiles.add(new RemiFile("E:/", "E:/", true, false));

        recyclerView.setLayoutManager(getLayoutManager());
        adapter = new FilesAdapter(getContext(), new ArrayList<RemiFile>(remiFiles));
        adapter.setOnItemClickListener(this);
        adapter.setOnOverflowMenuItemClickListener(this);
        recyclerView.setAdapter(adapter);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getContext(),
                DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(ContextCompat.getDrawable(getContext(),
                R.drawable.divider_recyclerview));
        recyclerView.addItemDecoration(dividerItemDecoration);
    }

    @Override
    public void onItemClick(RemiFile remiFile, View view) {

        // TODO Subscribe
        client.requestDirectory(remiFile.getPath());
    }

    @Override
    public void onOverflowMenuItemClicked(int itemId, RemiFile content) {

        // TODO
        switch (itemId) {

            case R.id.popup_file_item_copy:

                break;

            case R.id.popup_file_item_to_apps:

                break;

        }


    }

    private RecyclerView.LayoutManager getLayoutManager() {
        return new LinearLayoutManager(getContext());
    }

}
