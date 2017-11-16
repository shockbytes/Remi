package at.shockbytes.remote.core

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.TabLayout
import android.support.v4.app.ActivityOptionsCompat
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import at.shockbytes.remote.R
import at.shockbytes.remote.dagger.AppComponent
import at.shockbytes.remote.fragment.*
import at.shockbytes.remote.network.RemiClient
import at.shockbytes.remote.util.AppParams
import at.shockbytes.remote.wear.WearableManager
import butterknife.BindView
import io.reactivex.observers.ResourceObserver
import javax.inject.Inject

class MainActivity : BaseActivity(), TabLayout.OnTabSelectedListener,
        FilesFragment.OnSlidesSelectedListener, WearableManager.OnWearableConnectedListener {

    private var tabPosition: Int = 0

    @BindView(R.id.main_toolbar)
    lateinit var toolbar: Toolbar

    @BindView(R.id.main_appbar)
    lateinit var appBar: AppBarLayout

    @BindView(R.id.main_tablayout)
    lateinit var tabLayout: TabLayout

    @BindView(R.id.main_fab_edit)
    lateinit var fabKeyboard: FloatingActionButton

    @Inject
    lateinit var client: RemiClient

    @Inject
    lateinit var wearableManager: WearableManager


    // private MenuItem itemWear;

    private var selectedSlides: String? = null

    private val disconnectSubscriber = object : ResourceObserver<Any>() {
        override fun onComplete() {
            dispose()
        }

        override fun onError(e: Throwable) {
            e.printStackTrace()
            showToast(R.string.desktop_disconnection_error)
            dispose()
        }

        override fun onNext(obj: Any) {
            showToast(R.string.desktop_disconnected)
            supportFinishAfterTransition()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        tabPosition = AppParams.POSITION_MOUSE
        client.listenForConnectionLoss().subscribe(disconnectSubscriber)

        initializeViews()
    }

    override fun injectToGraph(appComponent: AppComponent) {
        appComponent.inject(this)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        /*
        itemWear = menu.findItem(R.id.menu_main_wear);
        setupDesktopMenuItem(menu);
        */
        return super.onCreateOptionsMenu(menu)
    }

    override fun onStart() {
        super.onStart()
        wearableManager.connect(this, this)
    }

    override fun onStop() {
        super.onStop()
        wearableManager.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()

        if (!disconnectSubscriber.isDisposed) {
            disconnectSubscriber.dispose()
        }

        client.disconnect().subscribe({
            client.close()
            showToast(R.string.disconnected)
        }) { throwable ->
            throwable.printStackTrace()
            showToast(R.string.disconnection_error)
        }
    }

    @SuppressLint("RestrictedApi")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {

            R.id.menu_main_wear ->
                showSnackbar(getString(R.string.wearable_connection_active))

            R.id.menu_main_desktop_os ->
                showSnackbar(getString(R.string.connected_to, item.title))

            R.id.menu_main_logout ->
                supportFinishAfterTransition()

            R.id.menu_main_help ->
                startActivity(HelpActivity.newIntent(this),
                        ActivityOptionsCompat.makeSceneTransitionAnimation(this).toBundle())

            R.id.menu_main_settings ->
                startActivityForResult(SettingsActivity.newIntent(this),
                        SettingsActivity.REQUEST_CODE,
                        ActivityOptionsCompat.makeSceneTransitionAnimation(this).toBundle())
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onTabSelected(tab: TabLayout.Tab) {

        appBar.setExpanded(true, true)
        val ft = supportFragmentManager
                .beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)

        tabPosition = tab.position
        when (tabPosition) {

            AppParams.POSITION_APPS -> {
                fabKeyboard.hide()
                ft.replace(R.id.main_content,
                        AppsFragment.newInstance(client.connectionPermissions.hasAppsPermission))
            }

            AppParams.POSITION_MOUSE -> {
                fabKeyboard.show()
                ft.replace(R.id.main_content, MouseFragment
                        .newInstance(client.connectionPermissions.hasMousePermission))
            }

            AppParams.POSITION_FILES -> {
                fabKeyboard.hide()
                ft.replace(R.id.main_content, FilesFragment
                        .newInstance(client.connectionPermissions.hasFilesPermission))
            }

            AppParams.POSITION_SLIDES -> {
                fabKeyboard.hide()
                ft.replace(R.id.main_content, SlidesFragment.newInstance(selectedSlides))
                selectedSlides = null // Reset to avoid multiple re-usage
            }
        }
        ft.commit()
    }

    override fun onTabUnselected(tab: TabLayout.Tab) {}

    override fun onTabReselected(tab: TabLayout.Tab) {}

    override fun onSlidesSelected(pathToSlides: String) {

        selectedSlides = pathToSlides
        val slidesTab = tabLayout.getTabAt(AppParams.POSITION_SLIDES)
        slidesTab?.select()
    }

    override fun onWearableConnected(wearableDevice: String) {

        /*
        itemWear.setVisible(true);
        showSnackbar(getString(R.string.wear_connected));
        */
    }

    override fun onWearableConnectionFailed(errorMessage: String) {

        /*
        itemWear.setVisible(false);
        showSnackbar(errorMessage);
        */
    }

    /*
    private void setupDesktopMenuItem(Menu menu) {
        String desktopOS = client.getDesktopOS();
        if (!desktopOS.equals("NA") && !desktopOS.isEmpty()) {
            menu.findItem(R.id.menu_main_desktop_os)
                    .setVisible(true)
                    .setTitle(client.getDesktopOS())
                    .setIcon(RemiUtils.Companion.getOperatingSystemIcon(client.getDesktopOS()));
        } else if (desktopOS.isEmpty()) { // <-- Debug mode
            menu.findItem(R.id.menu_main_desktop_os)
                    .setVisible(true)
                    .setTitle(getString(R.string.dev_mode))
                    .setIcon(R.drawable.ic_dev_mode_menu);
        }
    }
    */

    private fun initializeViews() {

        tabLayout.addOnTabSelectedListener(this)
        val initialTab = tabLayout.getTabAt(tabPosition)
        initialTab?.select()

        fabKeyboard.setOnClickListener {
            if (client.connectionPermissions.hasMousePermission) {
                KeyboardFragment.newInstance()
                        .show(supportFragmentManager, "keyboard-fragment")
            } else {
                showSnackbar(getString(R.string.permission_keyboard))
            }
        }
    }

    companion object {

        fun newIntent(context: Context): Intent {
            return Intent(context, MainActivity::class.java)
        }
    }

}
