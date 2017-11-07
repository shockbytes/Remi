package at.shockbytes.remote.fragment.dialog

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.widget.Button
import at.shockbytes.remote.R

/**
 * @author Martin Macheiner
 * Date: 05.11.2017.
 */

class ResetKeysDialogFragment : DialogFragment() {

    interface OnResetKeysListener {

        fun onReset()

    }

    private lateinit var btnPositive: Button

    var listener: OnResetKeysListener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = AlertDialog.Builder(context)
                .setTitle(R.string.reset_keys)
                .setMessage(R.string.reset_keys_message)
                .setIcon(R.drawable.ic_key)
                .setPositiveButton(R.string.reset) { _, _ ->
                    listener?.onReset()
                }
                .setNegativeButton(android.R.string.cancel) { _, _ -> dismiss() }
                .create()

        // Disable connection until desktop accepts key exchange
        dialog.setOnShowListener { _ ->
            btnPositive = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            btnPositive.setTextColor(ContextCompat.getColor(context, R.color.error))
        }

        return dialog
    }

    companion object {

        fun newInstance(): ResetKeysDialogFragment {
            val fragment = ResetKeysDialogFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }

}