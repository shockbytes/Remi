package at.shockbytes.remote.fragment


import android.Manifest
import android.content.Context
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import at.shockbytes.remote.R
import at.shockbytes.remote.adapter.FilesAdapter
import at.shockbytes.remote.core.RemiApp
import at.shockbytes.remote.network.RemiClient
import at.shockbytes.remote.network.model.FileTransferResponse
import at.shockbytes.remote.network.model.RemiFile
import at.shockbytes.remote.util.RemiUtils
import at.shockbytes.util.adapter.BaseAdapter
import butterknife.BindView
import butterknife.OnClick
import io.reactivex.annotations.NonNull
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableObserver
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import java.io.File
import java.util.*
import javax.inject.Inject
import kotlinx.android.synthetic.main.fragment_files.fragment_files_rv as recyclerView

class FilesFragment : BaseFragment(),
        BaseAdapter.OnItemClickListener<RemiFile>,
        FilesAdapter.OnOverflowMenuItemClickListener<RemiFile> {

    interface OnSlidesSelectedListener {

        fun onSlidesSelected(pathToSlides: String)
    }

    @Inject
    lateinit var client: RemiClient

    @BindView(R.id.fragment_files_txt_path)
    lateinit var txtPath: TextView

    private lateinit var adapter: FilesAdapter
    private lateinit var backStack: Stack<String>
    private var fileToTransfer: RemiFile? = null

    private var hasPermission: Boolean = false

    private var slidesSelectedListener: OnSlidesSelectedListener? = null

    private var fileTransferObserver: FileTransferObserver? = null

    private var copyDisposable: Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity.application as RemiApp).appComponent.inject(this)
        hasPermission = arguments.getBoolean(ARG_PERMISSION)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_files, container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        slidesSelectedListener = context as? OnSlidesSelectedListener
    }

    override fun onStart() {
        super.onStart()

        if (hasPermission) {
            backStack = Stack()
            client.requestBaseDirectories().subscribeWith(FileObserver())
        } else {
            showSnackbar(getString(R.string.permission_files))
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        if (fileTransferObserver?.isDisposed == false) {
            fileTransferObserver?.dispose()
            Toast.makeText(context, R.string.filetransfer_cancelled, Toast.LENGTH_SHORT).show()
            RemiUtils.revokeFileTransferNotification(context)
        }

        if (copyDisposable?.isDisposed == false) {
            copyDisposable?.dispose()
        }

    }

    override fun setupViews() {

        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = FilesAdapter(context, ArrayList(),
                client.connectionPermissions.hasFileTransferPermission)
        adapter.setOnItemClickListener(this)
        adapter.setOnOverflowMenuItemClickListener(this)
        recyclerView.adapter = adapter

        val dividerItemDecoration = DividerItemDecoration(context,
                DividerItemDecoration.VERTICAL)
        dividerItemDecoration.setDrawable(ContextCompat.getDrawable(context,
                R.drawable.divider_recyclerview))
        recyclerView.addItemDecoration(dividerItemDecoration)
    }

    override fun onItemClick(remiFile: RemiFile, view: View) {

        if (remiFile.isDirectory) {
            val parent = File(remiFile.path).parent
            if (parent != null) {
                backStack.push(parent + "/")
            }
            txtPath.text = remiFile.path
            client.requestDirectory(remiFile.path).subscribeWith(FileObserver())
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onOverflowMenuItemClicked(itemId: Int, content: RemiFile) {

        when (itemId) {

            R.id.popup_file_item_copy -> tryTransferFile(content)
            R.id.popup_file_item_to_apps -> {
                showSnackbar(getString(R.string.files_menu_add_to_apps, content.name))
                client.sendAddAppRequest(content.path).subscribe()
            }
            R.id.popup_file_open_on_desktop -> {
                showSnackbar(getString(R.string.files_menu_open_on_desktop, content.name))
                client.sendAppOpenRequest(content.path).subscribe()
            }
            R.id.popup_file_open_in_slides -> slidesSelectedListener?.onSlidesSelected(content.path)
        }
    }

    @OnClick(R.id.fragment_files_btn_back)
    fun onClickNavigateBack(view: View) {
        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)

        if (!backStack.isEmpty()) {
            val filepath = backStack.pop()
            txtPath.text = filepath
            client.requestDirectory(filepath).subscribeWith(FileObserver())
        } else {
            txtPath.text = ""
            client.requestBaseDirectories().subscribeWith(FileObserver())
        }
    }

    @AfterPermissionGranted(REQ_CODE_EXTERNAL_STORAGE)
    private fun transferFile() {

        if (fileToTransfer != null) {
            RemiUtils.postFileTransferNotification(context, fileToTransfer?.name)
            showToast(getString(R.string.filetransfer_initiated, fileToTransfer?.name), false)

            fileTransferObserver = client.transferFile(fileToTransfer!!.path)
                    .subscribeWith(FileTransferObserver())
        }
    }

    private fun tryTransferFile(content: RemiFile) {

        fileToTransfer = content
        if (EasyPermissions.hasPermissions(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            transferFile()
        } else {
            EasyPermissions.requestPermissions(this, getString(R.string.rationale_external_storage),
                    REQ_CODE_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }

    private fun copyToLocalStorage(response: FileTransferResponse) {
        copyDisposable = RemiUtils.copyFileToDownloadsFolder(response.content, response.filename)
                .subscribe({
                    showSnackbar(getString(R.string.filetransfer_finished, response.filename),
                            getString(R.string.show)) { RemiUtils.openOutputFolder(context) }
                    fileToTransfer = null
                }, {
                    showToast(getString(R.string.filetransfer_finished_w_error, response.filename))
                    fileToTransfer = null
                })
    }

    private inner class FileObserver : DisposableObserver<List<RemiFile>>() {

        override fun onNext(@NonNull remiFiles: List<RemiFile>) {
            adapter.data = remiFiles
            recyclerView?.scheduleLayoutAnimation()
        }

        override fun onError(@NonNull e: Throwable) {
            e.printStackTrace()
            showSnackbar(getString(R.string.files_request_error))
            dispose()
        }

        override fun onComplete() {
            dispose()
        }
    }

    private inner class FileTransferObserver : DisposableObserver<FileTransferResponse>() {

        override fun onNext(response: FileTransferResponse) {

            val transferCode = response.getTransferCodeAsEnum()
            if (transferCode === FileTransferResponse.TransferCode.OKAY) {
                copyToLocalStorage(response)
            } else {
                showSnackbar(getString(RemiUtils.getErrorTextForTransferCode(transferCode)))
            }

            // Revoke notification anyway
            RemiUtils.revokeFileTransferNotification(context)
            dispose()
        }

        override fun onError(e: Throwable) {
            e.printStackTrace()
            fileToTransfer = null
            showSnackbar(e.localizedMessage)
            RemiUtils.revokeFileTransferNotification(context)
            dispose()
        }

        override fun onComplete() {
            RemiUtils.revokeFileTransferNotification(context)
            dispose()
        }
    }

    companion object {

        private const val REQ_CODE_EXTERNAL_STORAGE = 0x9482
        private const val ARG_PERMISSION = "arg_permission"

        fun newInstance(hasPermission: Boolean): FilesFragment {
            val fragment = FilesFragment()
            val args = Bundle()
            args.putBoolean(ARG_PERMISSION, hasPermission)
            fragment.arguments = args
            return fragment
        }
    }

}
