package at.shockbytes.remote.fragment

import android.os.Bundle
import android.preference.Preference
import android.preference.PreferenceFragment
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import at.shockbytes.remote.R
import at.shockbytes.remote.core.RemiApp
import at.shockbytes.remote.fragment.dialog.AuthorizedDesktopsDialogFragment
import at.shockbytes.remote.fragment.dialog.ResetKeysDialogFragment
import at.shockbytes.remote.network.security.AndroidSecurityManager
import javax.inject.Inject

class SettingsFragment : PreferenceFragment(), Preference.OnPreferenceClickListener {

    @Inject
    lateinit var securityManager: AndroidSecurityManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity.application as RemiApp).appComponent.inject(this)
        addPreferencesFromResource(R.xml.settings)

        findPreference(getString(R.string.prefs_key_keys_reset)).onPreferenceClickListener = this
        findPreference(getString(R.string.prefs_key_keys_auth_desktops))
                .onPreferenceClickListener = this
    }

    override fun onPreferenceClick(preference: Preference): Boolean {

        when(preference.key) {

            getString(R.string.prefs_key_keys_reset) -> resetKeys()
            getString(R.string.prefs_key_keys_auth_desktops) -> showAuthorizedDesktops()
        }
        return true
    }

    private fun resetKeys() {

        val confirmDialog = ResetKeysDialogFragment.newInstance()
        confirmDialog.listener = object: ResetKeysDialogFragment.OnResetKeysListener {
            override fun onReset() {
                securityManager.reset().subscribe{
                    Toast.makeText(activity, "Keys resetted", Toast.LENGTH_SHORT).show()
                    // TODO Call MainActivity to finish itself
                }
            }
        }
        confirmDialog.show((activity as AppCompatActivity).supportFragmentManager, "reset_keys_dialog")
    }

    private fun showAuthorizedDesktops() {
        AuthorizedDesktopsDialogFragment
                .newInstance(securityManager.getVerifiedDesktopApps())
                .show((activity as AppCompatActivity).supportFragmentManager, "authorized_desktops_dialog")
    }

    companion object {

        fun newInstance(): SettingsFragment {
            val fragment = SettingsFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }
}
