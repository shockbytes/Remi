package at.shockbytes.remote.fragment;


import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import javax.inject.Inject;

import at.shockbytes.remote.R;
import at.shockbytes.remote.adapter.FilesAdapter;
import at.shockbytes.remote.core.RemiApp;
import at.shockbytes.remote.network.RemiClient;
import at.shockbytes.remote.network.model.RemiFile;
import at.shockbytes.util.adapter.BaseAdapter;
import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.annotations.NonNull;
import io.reactivex.observers.DisposableObserver;

public class FilesFragment extends BaseFragment
        implements BaseAdapter.OnItemClickListener<RemiFile>,
        FilesAdapter.OnOverflowMenuItemClickListener<RemiFile> {

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

    @BindView(R.id.fragment_files_txt_path)
    protected TextView txtPath;

    private FilesAdapter adapter;
    private Stack<RemiFile> backStack;

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
        client.requestBaseDirectories().subscribeWith(new FileObserver());
        backStack = new Stack<>();
    }

    @Override
    protected void setupViews() {

        recyclerView.setLayoutManager(getLayoutManager());
        adapter = new FilesAdapter(getContext(), new ArrayList<RemiFile>());
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

        backStack.push(remiFile);
        txtPath.setText(remiFile.getPath());
        client.requestDirectory(remiFile.getPath()).subscribeWith(new FileObserver());
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

    @OnClick(R.id.fragment_files_btn_back)
    protected void onClickNavigateBack() {

        if (!backStack.isEmpty()) {
            RemiFile file = backStack.pop();
            txtPath.setText(file.getPath());
            client.requestDirectory(file.getPath()).subscribeWith(new FileObserver());
        } else {
            txtPath.setText("");
            client.requestBaseDirectories().subscribeWith(new FileObserver());
        }
    }

    private RecyclerView.LayoutManager getLayoutManager() {
        return new LinearLayoutManager(getContext());
    }

    private class FileObserver extends DisposableObserver<List<RemiFile>> {

        @Override
        public void onNext(@NonNull List<RemiFile> remiFiles) {
            adapter.setData(remiFiles);
        }

        @Override
        public void onError(@NonNull Throwable e) {
            e.printStackTrace();
            Snackbar.make(getView(), "Can't access files", Snackbar.LENGTH_SHORT).show();
            dispose();
        }

        @Override
        public void onComplete() {
            dispose();
        }
    }

}
