package at.shockbytes.remote.fragment.dialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AnticipateOvershootInterpolator
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import at.shockbytes.remote.R
import at.shockbytes.remote.core.RemiApp
import at.shockbytes.remote.network.model.DesktopApp
import at.shockbytes.remote.network.security.AndroidSecurityManager
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.Unbinder
import io.reactivex.observers.DisposableObserver
import javax.inject.Inject


/**
 * @author Martin Macheiner
 * Date: 05.11.2017.
 */

class AcceptDesktopConnectionDialogFragment : DialogFragment() {

    interface OnAcceptDesktopConnectionListener {

        fun onAccept(app: DesktopApp)

    }


    @BindView(R.id.dialogfragment_accept_connection_imgview_check)
    lateinit var imgViewCheck : ImageView

    @BindView(R.id.dialogfragment_accept_connection_progressbar)
    lateinit var progressBar : ProgressBar

    @BindView(R.id.dialogfragment_accept_connection_txt_status)
    lateinit var txtStatus : TextView

    @Inject
    lateinit var securityManager: AndroidSecurityManager

    private lateinit var app: DesktopApp

    private lateinit var unbinder: Unbinder

    private lateinit var btnPositive: Button

    private lateinit var keyExchangeObserver: DisposableObserver<Boolean>

    var listener: OnAcceptDesktopConnectionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity.application as RemiApp).appComponent.inject(this)
        app = arguments.getParcelable(ARG_APP)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = AlertDialog.Builder(context)
                .setTitle(getString(R.string.connect_with, app.name))
                .setIcon(R.drawable.ic_desktop)
                .setView(createView())
                .setPositiveButton(R.string.connect) { _, _ ->
                    app.isTrusted = true
                    listener?.onAccept(app)
                }
                .setNegativeButton(android.R.string.cancel) { _, _ -> dismiss() }
                .create()

        // Disable connection until desktop accepts key exchange
        dialog.setOnShowListener { _ ->
            btnPositive = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            btnPositive.isEnabled = false
        }

        return dialog
    }

    override fun onStart() {
        super.onStart()

        keyExchangeObserver = securityManager.initializeKeyExchange(app)
                .subscribeWith(KeyExchangeObserver())
    }

    override fun onStop() {
        super.onStop()

        if (!keyExchangeObserver.isDisposed) {
            keyExchangeObserver.dispose()
        }
    }

    @SuppressLint("InflateParams")
    private fun createView(): View {
        val view = LayoutInflater.from(context)
                .inflate(R.layout.dialogfragment_accept_desktop_connection, null, false)
        unbinder = ButterKnife.bind(this, view)
        return view
    }

    override fun onDestroyView() {
        unbinder.unbind()
        super.onDestroyView()
    }

    /*
    private fun fakeAccept() {
        Observable.interval(5, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.computation())
                .take(1)
                .subscribe { _ -> desktopAccepted() }
    } */

    private fun desktopAccepted() {
        updateView(true,
                getString(R.string.connection_accept_confirmation, app.name),
                R.drawable.ic_check_circle)
    }

    private fun desktopRejected() {
        updateView(false,
                getString(R.string.connection_accept_reject, app.name),
                R.drawable.ic_reject)
    }

    private fun updateView(enabled: Boolean, text: String, iconId: Int) {

        imgViewCheck.setImageResource(iconId)
        btnPositive.isEnabled = enabled

        progressBar.animate()
                .alpha(0f)
                .scaleX(0.3f)
                .scaleY(0.3f)
                .setDuration(500)
                .setInterpolator(AnticipateOvershootInterpolator(4f))
                .start()

        imgViewCheck.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(500)
                .setInterpolator(AnticipateOvershootInterpolator(4f))
                .start()

        txtStatus.text = text
    }

    companion object {

        private val ARG_APP = "arg_desktop_app"

        fun newInstance(app: DesktopApp): AcceptDesktopConnectionDialogFragment {
            val fragment = AcceptDesktopConnectionDialogFragment()
            val args = Bundle()
            args.putParcelable(ARG_APP, app)
            fragment.arguments = args
            return fragment
        }
    }

    private inner class KeyExchangeObserver : DisposableObserver<Boolean>() {

        override fun onError(e: Throwable) {
            e.printStackTrace()
            dispose()
        }

        override fun onNext(t: Boolean) {

            if (t) {
                desktopAccepted()
            } else {
                desktopRejected()
            }
        }

        override fun onComplete() {
            dispose()
        }

    }


}