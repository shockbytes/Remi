package at.shockbytes.remote.fragment;


import android.Manifest;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import javax.inject.Inject;

import at.shockbytes.remote.R;
import at.shockbytes.remote.adapter.FilesAdapter;
import at.shockbytes.remote.core.RemiApp;
import at.shockbytes.remote.network.RemiClient;
import at.shockbytes.remote.network.model.FileTransferResponse;
import at.shockbytes.remote.network.model.RemiFile;
import at.shockbytes.remote.util.RemiUtils;
import at.shockbytes.util.adapter.BaseAdapter;
import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.observers.DisposableObserver;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class FilesFragment extends BaseFragment
        implements BaseAdapter.OnItemClickListener<RemiFile>,
        FilesAdapter.OnOverflowMenuItemClickListener<RemiFile> {

    private static final int REQ_CODE_EXTERNAL_STORAGE = 0x9482;
    private static final String ARG_PERMISSION = "arg_permission";

    public static FilesFragment newInstance(boolean hasPermission) {
        FilesFragment fragment = new FilesFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_PERMISSION, hasPermission);
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
    private Stack<String> backStack;
    private RemiFile fileToTransfer;

    private boolean hasPermission;

    public FilesFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((RemiApp) getActivity().getApplication()).getAppComponent().inject(this);
        hasPermission = getArguments().getBoolean(ARG_PERMISSION);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_files, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        if (hasPermission) {
            backStack = new Stack<>();
            client.requestBaseDirectories().subscribeWith(new FileObserver());
        } else {
            Snackbar.make(getView(), "No permission to request files!", Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    protected void setupViews() {

        recyclerView.setLayoutManager(getLayoutManager());
        adapter = new FilesAdapter(getContext(), new ArrayList<RemiFile>(),
                client.getConnectionPermissions().hasFileTransferPermission());
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

        if (remiFile.isDirectory()) {
            String parent = new File(remiFile.getPath()).getParent();
            if (parent != null) {
                backStack.push(parent + "/");
            }
            txtPath.setText(remiFile.getPath());
            client.requestDirectory(remiFile.getPath())
                    .subscribeWith(new FileObserver());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @android.support.annotation.NonNull String[] permissions,
                                           @android.support.annotation.NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onOverflowMenuItemClicked(int itemId, final RemiFile content) {

        switch (itemId) {

            case R.id.popup_file_item_copy:

                tryTransferFile(content);
                break;

            case R.id.popup_file_item_to_apps:

                showSnackbar(content.getName() + " added to apps");
                client.sendAddAppRequest(content.getPath()).subscribe();
                break;
        }
    }

    @OnClick(R.id.fragment_files_btn_back)
    protected void onClickNavigateBack(View view) {

        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

        if (!backStack.isEmpty()) {
            String filepath = backStack.pop();
            txtPath.setText(filepath);
            client.requestDirectory(filepath).subscribeWith(new FileObserver());
        } else {
            txtPath.setText("");
            client.requestBaseDirectories().subscribeWith(new FileObserver());
        }
    }

    private RecyclerView.LayoutManager getLayoutManager() {
        return new LinearLayoutManager(getContext());
    }

    private void tryTransferFile(final RemiFile content) {

        fileToTransfer = content;
        if (EasyPermissions.hasPermissions(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            transferFile();
        } else {
            EasyPermissions.requestPermissions(this, "Write the transferred file into the downloads folder",
                    REQ_CODE_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

    }

    @AfterPermissionGranted(REQ_CODE_EXTERNAL_STORAGE)
    private void transferFile() {

        if (fileToTransfer == null) {
            return;
        }

        // TODO Post notification for ongoing download
        showSnackbar("Copying " + fileToTransfer.getName());

        client.transferFile(fileToTransfer.getPath())
                .subscribe(new Consumer<FileTransferResponse>() {
                    @Override
                    public void accept(FileTransferResponse response) throws Exception {

                        Toast.makeText(getContext(), fileToTransfer.getName() + " copied!",
                                Toast.LENGTH_SHORT).show();
                        RemiUtils.copyFileToDownloadsFolder(response.getContent(),
                                fileToTransfer.getName());
                        fileToTransfer = null;
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        fileToTransfer = null;
                        showSnackbar(throwable.getMessage());
                    }
                });
    }

    private void showSnackbar(String text) {
        Snackbar.make(getView(), text, Snackbar.LENGTH_SHORT).show();
    }

    private class FileObserver extends DisposableObserver<List<RemiFile>> {

        @Override
        public void onNext(@NonNull List<RemiFile> remiFiles) {
            adapter.setData(remiFiles);
            // Caused to a NPE thrown because of initial directory request
            if (recyclerView != null) {
                recyclerView.scheduleLayoutAnimation();
            }
        }

        @Override
        public void onError(@NonNull Throwable e) {
            e.printStackTrace();
            showSnackbar("Can't access files");
            dispose();
        }

        @Override
        public void onComplete() {
            dispose();
        }
    }

}
