package at.shockbytes.remote.fragment.dialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import at.shockbytes.remote.R
import at.shockbytes.remote.debug.DebugOptions
import butterknife.ButterKnife
import butterknife.OnClick
import butterknife.Unbinder

/**
 * @author Martin Macheiner
 * Date: 05.11.2017.
 */

class DebugOptionsDialogFragment : DialogFragment() {

    private lateinit var unbinder : Unbinder

    var debugListener: DebugOptions.OnDebugOptionSelectedListener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(context)
                .setTitle(R.string.debug_options)
                .setIcon(R.drawable.ic_dev_mode)
                .setView(createView())
                .setPositiveButton(android.R.string.cancel) { _, _ ->  dismiss() }
                .create()
    }

    @SuppressLint("InflateParams")
    private fun createView() : View {
        val view = LayoutInflater.from(context).inflate(R.layout.dialogfragment_debug_options, null, false)
        unbinder = ButterKnife.bind(this, view)
        return view
    }

    override fun onDestroyView() {
        unbinder.unbind()
        super.onDestroyView()
    }

    @OnClick(R.id.dialogfragment_debug_options_btn_login)
    fun onClickDebugLogin() {
        debugListener?.onDebugOptionSelected(DebugOptions.DebugAction.FAKE_LOGIN)
        dismiss()
    }

    @OnClick(R.id.dialogfragment_debug_options_btn_fake_devices)
    fun onClickFakeDevices() {
        debugListener?.onDebugOptionSelected(DebugOptions.DebugAction.FAKE_DEVICES)
        dismiss()
    }

    @OnClick(R.id.dialogfragment_debug_options_btn_key_recreation)
    fun onClickRecreateKeys() {
        debugListener?.onDebugOptionSelected(DebugOptions.DebugAction.REGENERATE_KEYS)
        dismiss()
    }

    @OnClick(R.id.dialogfragment_debug_options_btn_unauthorized_connection)
    fun onClickForceUnauthorizedConnection() {
        debugListener?.onDebugOptionSelected(DebugOptions.DebugAction.FORCE_UNAUTHORIZED_CONNECTION)
        dismiss()
    }

    companion object {
        fun newInstance() : DebugOptionsDialogFragment {
            val fragment = DebugOptionsDialogFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }


}