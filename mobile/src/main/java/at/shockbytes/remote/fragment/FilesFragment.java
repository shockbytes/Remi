package at.shockbytes.remote.fragment;


import android.Manifest;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
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
import io.reactivex.observers.DisposableObserver;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class FilesFragment extends BaseFragment
        implements BaseAdapter.OnItemClickListener<RemiFile>,
        FilesAdapter.OnOverflowMenuItemClickListener<RemiFile> {

    public interface OnSlidesSelectedListener {

        void onSlidesSelected(String pathToSlides);
    }

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

    private OnSlidesSelectedListener slidesSelectedListener;

    private FileTransferObserver fileTransferObserver;

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
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            slidesSelectedListener = (OnSlidesSelectedListener) context;
        } catch (IllegalArgumentException iae) {
            iae.printStackTrace();
            Log.wtf("Remi", "Activity must provide interface");
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        if (hasPermission) {
            backStack = new Stack<>();
            client.requestBaseDirectories().subscribeWith(new FileObserver());
        } else {
            showSnackbar(getString(R.string.permission_files));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (fileTransferObserver != null && !fileTransferObserver.isDisposed()) {
            fileTransferObserver.dispose();
            Toast.makeText(getContext(), R.string.filetransfer_cancelled, Toast.LENGTH_SHORT).show();
            RemiUtils.revokeFileTransferNotification(getContext());
        }
    }

    @Override
    protected void setupViews() {

        recyclerView.setLayoutManager(getLayoutManager());
        adapter = new FilesAdapter(getContext(), new ArrayList<RemiFile>(),
                client.getConnectionPermissions().getHasFileTransferPermission());
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

                showSnackbar(getString(R.string.files_menu_add_to_apps, content.getName()));
                client.sendAddAppRequest(content.getPath()).subscribe();
                break;

            case R.id.popup_file_open_on_desktop:

                showSnackbar(getString(R.string.files_menu_open_on_desktop, content.getName()));
                client.sendAppOpenRequest(content.getPath()).subscribe();
                break;

            case R.id.popup_file_open_in_slides:

                if (slidesSelectedListener != null) {
                    slidesSelectedListener.onSlidesSelected(content.getPath());
                }
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

    @AfterPermissionGranted(REQ_CODE_EXTERNAL_STORAGE)
    private void transferFile() {

        if (fileToTransfer == null) {
            return;
        }

        RemiUtils.postFileTransferNotification(getContext(), fileToTransfer.getName());
        showSnackbar(getString(R.string.filetransfer_initiated, fileToTransfer.getName()));

        fileTransferObserver = client.transferFile(fileToTransfer.getPath())
                .subscribeWith(new FileTransferObserver());
    }

    private RecyclerView.LayoutManager getLayoutManager() {
        return new LinearLayoutManager(getContext());
    }

    private void tryTransferFile(final RemiFile content) {

        fileToTransfer = content;
        if (EasyPermissions.hasPermissions(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            transferFile();
        } else {
            EasyPermissions.requestPermissions(this, getString(R.string.rationale_external_storage),
                    REQ_CODE_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
    }

    private void copyToInternalStorage(FileTransferResponse response) {

        String msg;
        try {
            RemiUtils.copyFileToDownloadsFolder(response.getContent(),
                    response.getFilename());
            msg = getString(R.string.filetransfer_finished, response.getFilename());
        } catch (IOException e) {
            e.printStackTrace();
            msg = getString(R.string.filetransfer_finished_w_error, response.getFilename());
        }

        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
        fileToTransfer = null;
        RemiUtils.revokeFileTransferNotification(getContext());
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
            showSnackbar(getString(R.string.files_request_error));
            dispose();
        }

        @Override
        public void onComplete() {
            dispose();
        }
    }

    private class FileTransferObserver extends DisposableObserver<FileTransferResponse> {

        @Override
        public void onNext(FileTransferResponse response) {

            FileTransferResponse.TransferCode transferCode = response.getTransferCodeAsEnum();
            if (transferCode == FileTransferResponse.TransferCode.OKAY) {
                copyToInternalStorage(response);
            } else {
                showSnackbar(getString(RemiUtils.getErrorTextForTransferCode(transferCode)));
            }
        }

        @Override
        public void onError(Throwable e) {
            e.printStackTrace();
            fileToTransfer = null;
            showSnackbar(e.getMessage());
            RemiUtils.revokeFileTransferNotification(getContext());
            dispose();
        }

        @Override
        public void onComplete() {
            RemiUtils.revokeFileTransferNotification(getContext());
            dispose();
        }
    }

}
