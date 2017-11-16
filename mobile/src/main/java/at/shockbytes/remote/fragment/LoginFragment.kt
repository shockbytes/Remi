package at.shockbytes.remote.fragment


import android.content.Context
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Toast
import at.shockbytes.remote.R
import at.shockbytes.remote.adapter.DesktopAppsAdapter
import at.shockbytes.remote.core.RemiApp
import at.shockbytes.remote.debug.DebugOptions
import at.shockbytes.remote.fragment.dialog.AcceptDesktopConnectionDialogFragment
import at.shockbytes.remote.fragment.dialog.DebugOptionsDialogFragment
import at.shockbytes.remote.network.RemiClient
import at.shockbytes.remote.network.discovery.ServiceFinder
import at.shockbytes.remote.network.model.DesktopApp
import at.shockbytes.remote.network.security.AndroidSecurityManager
import at.shockbytes.util.adapter.BaseAdapter
import butterknife.BindView
import butterknife.OnClick
import butterknife.OnLongClick
import java.util.*
import javax.inject.Inject

class LoginFragment : BaseFragment(), BaseAdapter.OnItemClickListener<DesktopApp> {

    interface OnLoginActionListener {

        fun onStartAppTour()

        fun onConnected()

        fun onConnectionFailed(resultCode: Int)
    }

    @Inject
    lateinit var client: RemiClient

    @Inject
    lateinit var serviceFinder: ServiceFinder

    @Inject
    lateinit var securityManager: AndroidSecurityManager

    @BindView(R.id.fragment_login_rv_desktop_apps)
    lateinit var recyclerView: RecyclerView

    private var adapter: DesktopAppsAdapter? = null

    private var listener: OnLoginActionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity.application as RemiApp).appComponent.inject(this)
        createKeysIfNecessary()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as? OnLoginActionListener
    }

    override fun onItemClick(app: DesktopApp, view: View) {
        if (app.isTrusted) {
            connectToDevice(app)
        } else {
            showAcceptConnectionDialogFragment(app)
        }
    }

    @OnLongClick(R.id.fragment_login_imgview_icon)
    fun onClickDebugEntryIcon(): Boolean {

        val fragment = DebugOptionsDialogFragment.newInstance()
        fragment.debugListener = object : DebugOptions.OnDebugOptionSelectedListener {
            override fun onDebugOptionSelected(action: DebugOptions.DebugAction) {

                when (action) {
                    DebugOptions.DebugAction.FAKE_LOGIN -> listener?.onConnected()
                    DebugOptions.DebugAction.FAKE_DEVICES -> {
                        adapter?.data = Arrays.asList(
                                DesktopApp("Fake Windows", "192.168.0.2", "Windows", "asdfgh", false),
                                DesktopApp("Fake, but trustworthy Linux", "192.168.0.3", "Linux", "asdfgh", true),
                                DesktopApp("MacOS Fake", "192.168.0.4", "Mac", "asdfgh", false)
                        )
                        animateRecyclerView()
                    }
                    DebugOptions.DebugAction.REGENERATE_KEYS -> Toast.makeText(context, "Re-generate keys", Toast.LENGTH_SHORT).show()
                    DebugOptions.DebugAction.FORCE_UNAUTHORIZED_CONNECTION -> Toast.makeText(context, "Unauthorized connection", Toast.LENGTH_SHORT).show()
                    DebugOptions.DebugAction.RESET_KEY_STORES -> securityManager.reset().subscribe { Toast.makeText(context, "Keystores reset!", Toast.LENGTH_SHORT).show() }
                }

            }
        }
        fragment.show(fragmentManager, "debug-options-fragment")
        return true
    }

    @OnClick(R.id.fragment_login_btn_lookup)
    fun onClickBtnLookup() {

        serviceFinder.lookForDesktopApps()
                .map { desktopApp ->
                    desktopApp.isTrusted = securityManager.verifyDesktopApp(desktopApp)
                    desktopApp
                }
                .subscribe({ desktopApp ->
                    adapter?.addEntityAtFirst(desktopApp)
                    recyclerView.scrollToPosition(0)
                    animateRecyclerView()
                }) { throwable -> throwable.printStackTrace() }
    }

    @OnClick(R.id.fragment_login_imgbtn_apptour)
    fun onClickAppTour() {
        listener?.onStartAppTour()
    }

    override fun setupViews() {
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        adapter = DesktopAppsAdapter(context, ArrayList())
        adapter?.setOnItemClickListener(this)
        recyclerView.adapter = adapter
    }

    override fun onStart() {
        super.onStart()
        adapter?.data = ArrayList()
    }

    override fun onPause() {
        super.onPause()
        serviceFinder.stopListening()
    }

    private fun connectToDevice(app: DesktopApp) {
        client.connect(app).subscribe({ resultCode ->
            if (resultCode == RemiClient.CONNECTION_RESULT_OK) {
                listener?.onConnected()
            } else {
                listener?.onConnectionFailed(resultCode)
            }
        }) { throwable ->
            throwable.printStackTrace()
            listener?.onConnectionFailed(RemiClient.CONNECTION_RESULT_ERROR_NETWORK)
        }
    }

    private fun animateRecyclerView() {
        recyclerView.animate().alpha(1f).scaleX(1f).scaleY(1f)
                .setInterpolator(AccelerateDecelerateInterpolator()).setDuration(500)
                .withEndAction {
                    if (adapter?.itemCount ?: 0 > 1) {
                        recyclerView.smoothScrollBy(recyclerView.width / 5, 0)
                    }
                }
                .start()
    }

    private fun createKeysIfNecessary() {
        if (!securityManager.hasKeys()) {
            securityManager.generateKeys().subscribe(
                    { Toast.makeText(context, "Keys created and stored!", Toast.LENGTH_LONG).show() }
            ){ throwable ->
                throwable.printStackTrace()
                Toast.makeText(context, throwable.localizedMessage, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showAcceptConnectionDialogFragment(app: DesktopApp) {
        val fragment = AcceptDesktopConnectionDialogFragment.newInstance(app)
        fragment.listener = object : AcceptDesktopConnectionDialogFragment.OnAcceptDesktopConnectionListener {
            override fun onAccept(app: DesktopApp) {
                adapter?.updateEntity(app)
                connectToDevice(app)
            }
        }
        fragment.show(fragmentManager, "accept-connection-fragment")
    }

    companion object {

        fun newInstance(): LoginFragment {
            val fragment = LoginFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }
}
