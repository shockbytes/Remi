package at.shockbytes.remote.core

import android.content.Context
import android.content.Intent
import android.os.Bundle

import at.shockbytes.remote.dagger.AppComponent
import at.shockbytes.remote.fragment.SettingsFragment

class SettingsActivity : BackNavigableActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fragmentManager
                .beginTransaction()
                .replace(android.R.id.content, SettingsFragment.newInstance())
                .commit()
    }

    override fun injectToGraph(appComponent: AppComponent) {
        // No injection necessary
    }

    companion object {

        val REQUEST_CODE = 0x1682

        fun newIntent(context: Context): Intent {
            return Intent(context, SettingsActivity::class.java)
        }
    }
}
