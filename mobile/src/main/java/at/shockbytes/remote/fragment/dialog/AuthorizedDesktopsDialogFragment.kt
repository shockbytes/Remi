package at.shockbytes.remote.fragment.dialog

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import at.shockbytes.remote.R

/**
 * @author Martin Macheiner
 * Date: 05.11.2017.
 */

class AuthorizedDesktopsDialogFragment : DialogFragment() {

    private lateinit var desktops: ArrayList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        desktops = arguments.getStringArrayList(ARG_DESKTOPS)
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(context)
                .setTitle(R.string.dialogfragment_authorized_desktops_title)
                .setIcon(R.drawable.ic_desktop)
                .setItems(desktops.toTypedArray(), null)
                .setPositiveButton(android.R.string.ok) { _, _ -> dismiss() }
                .setNegativeButton(android.R.string.cancel) { _, _ -> dismiss() }
                .create()
    }

    companion object {

        val ARG_DESKTOPS = "arg_desktops"

        fun newInstance(desktops: List<String>): AuthorizedDesktopsDialogFragment {
            val fragment = AuthorizedDesktopsDialogFragment()
            val args = Bundle()
            args.putStringArrayList(ARG_DESKTOPS, ArrayList<String>(desktops))
            fragment.arguments = args
            return fragment
        }
    }

}