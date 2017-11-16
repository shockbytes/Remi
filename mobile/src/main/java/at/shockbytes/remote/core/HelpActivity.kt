package at.shockbytes.remote.core

import android.content.Context
import android.content.Intent
import android.os.Bundle

import at.shockbytes.remote.dagger.AppComponent
import at.shockbytes.remote.fragment.help.MainHelpFragment

class HelpActivity : BackNavigableActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportFragmentManager
                .beginTransaction()
                .replace(android.R.id.content, MainHelpFragment.newInstance())
                .commit()
    }


    override fun injectToGraph(appComponent: AppComponent) {
        // No injection necessary
    }

    companion object {

        fun newIntent(context: Context): Intent {
            return Intent(context, HelpActivity::class.java)
        }
    }
}
