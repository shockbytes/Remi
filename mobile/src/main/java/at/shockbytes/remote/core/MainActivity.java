package at.shockbytes.remote.core;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.Explode;
import android.transition.Fade;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import javax.inject.Inject;

import at.shockbytes.remote.R;
import at.shockbytes.remote.fragment.AppsFragment;
import at.shockbytes.remote.fragment.FilesFragment;
import at.shockbytes.remote.fragment.KeyboardFragment;
import at.shockbytes.remote.fragment.MouseFragment;
import at.shockbytes.remote.fragment.SlidesFragment;
import at.shockbytes.remote.network.RemiClient;
import at.shockbytes.remote.util.AppParams;
import at.shockbytes.remote.wear.WearableManager;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import icepick.Icepick;
import icepick.State;
import io.reactivex.functions.Consumer;
import io.reactivex.observers.ResourceObserver;

@SuppressWarnings("unchecked")
public class MainActivity extends AppCompatActivity
        implements TabLayout.OnTabSelectedListener, FilesFragment.OnSlidesSelectedListener,
        WearableManager.OnWearableConnectedListener {

    public static Intent newIntent(Context context) {
        return new Intent(context, MainActivity.class);
    }

    @State
    protected int tabPosition;

    @BindView(R.id.main_toolbar)
    protected Toolbar toolbar;

    @BindView(R.id.main_appbar)
    protected AppBarLayout appBar;

    @BindView(R.id.main_tablayout)
    protected TabLayout tabLayout;

    @BindView(R.id.main_fab_edit)
    protected FloatingActionButton fabKeyboard;

    @Inject
    protected RemiClient client;

    @Inject
    protected WearableManager wearableManager;

    private Unbinder unbinder;

    // private MenuItem itemWear;

    private String selectedSlides;

    private final ResourceObserver<Object> disconnectSubscriber = new ResourceObserver<Object>() {
        @Override
        public void onComplete() {
            dispose();
        }

        @Override
        public void onError(Throwable e) {
            e.printStackTrace();
            showToast(R.string.desktop_disconnection_error);
            dispose();
        }

        @Override
        public void onNext(Object obj) {
            showToast(R.string.desktop_disconnected);
            supportFinishAfterTransition();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setEnterTransition(new Explode());
            getWindow().setExitTransition(new Fade());
        }
        setContentView(R.layout.activity_main);
        ((RemiApp) getApplication()).getAppComponent().inject(this);
        unbinder = ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        tabPosition = AppParams.POSITION_MOUSE;
        Icepick.restoreInstanceState(this, savedInstanceState);

        client.listenForConnectionLoss().subscribe(disconnectSubscriber);

        initializeViews();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        /*
        itemWear = menu.findItem(R.id.menu_main_wear);
        setupDesktopMenuItem(menu);
        */
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onStart() {
        super.onStart();
        wearableManager.connect(this, this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        wearableManager.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();

        if (!disconnectSubscriber.isDisposed()) {
            disconnectSubscriber.dispose();
        }

        client.disconnect().subscribe(new Consumer<Object>() {
            @Override
            public void accept(Object object) {
                client.close();
                showToast(R.string.disconnected);
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) {
                throwable.printStackTrace();
                showToast(R.string.disconnection_error);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.menu_main_wear:

                showSnackbar(getString(R.string.wearable_connection_active));
                break;

            case R.id.menu_main_desktop_os:

                showSnackbar(getString(R.string.connected_to, item.getTitle()));
                break;

            case R.id.menu_main_logout:

                supportFinishAfterTransition();
                break;

            case R.id.menu_main_help:

                ActivityOptionsCompat options = ActivityOptionsCompat
                        .makeSceneTransitionAnimation(this);
                startActivity(HelpActivity.newIntent(this), options.toBundle());
                break;

            case R.id.menu_main_settings:

                options = ActivityOptionsCompat.makeSceneTransitionAnimation(this);
                startActivity(SettingsActivity.newIntent(this), options.toBundle());
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Icepick.saveInstanceState(this, outState);
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {

        appBar.setExpanded(true, true);
        FragmentTransaction ft = getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);

        tabPosition = tab.getPosition();
        switch (tabPosition) {

            case AppParams.POSITION_APPS:
                fabKeyboard.hide();
                ft.replace(R.id.main_content,
                        AppsFragment.newInstance(client.getConnectionPermissions().getHasAppsPermission()));
                break;

            case AppParams.POSITION_MOUSE:
                fabKeyboard.show();
                ft.replace(R.id.main_content, MouseFragment
                        .newInstance(client.getConnectionPermissions().getHasMousePermission()));
                break;

            case AppParams.POSITION_FILES:
                fabKeyboard.hide();
                ft.replace(R.id.main_content, FilesFragment
                        .newInstance(client.getConnectionPermissions().getHasFilesPermission()));
                break;

            case AppParams.POSITION_SLIDES:
                fabKeyboard.hide();
                ft.replace(R.id.main_content, SlidesFragment.newInstance(selectedSlides));
                selectedSlides = null; // Reset to avoid multiple re-usage
                break;
        }
        ft.commit();
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {
    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {
    }

    @Override
    public void onSlidesSelected(String pathToSlides) {

        selectedSlides = pathToSlides;
        TabLayout.Tab slidesTab = tabLayout.getTabAt(AppParams.POSITION_SLIDES);
        if (slidesTab != null) {
            slidesTab.select();
        }
    }

    @Override
    public void onWearableConnected(String wearableDevice) {

        /*
        itemWear.setVisible(true);
        showSnackbar(getString(R.string.wear_connected));
        */
    }

    @Override
    public void onWearableConnectionFailed(String errorMessage) {

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
                    .setIcon(RemiUtils.getOperatingSystemIcon(client.getDesktopOS()));
        } else if (desktopOS.isEmpty()) { // <-- Debug mode
            menu.findItem(R.id.menu_main_desktop_os)
                    .setVisible(true)
                    .setTitle(getString(R.string.dev_mode))
                    .setIcon(R.drawable.ic_dev_mode_menu);
        }
    }
    */

    private void initializeViews() {

        tabLayout.addOnTabSelectedListener(this);
        TabLayout.Tab initialTab = tabLayout.getTabAt(tabPosition);
        if (initialTab != null) {
            initialTab.select();
        }

        fabKeyboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (client.getConnectionPermissions().getHasMousePermission()) {
                    KeyboardFragment.newInstance()
                            .show(getSupportFragmentManager(), "keyboard-fragment");
                } else {
                    showSnackbar(getString(R.string.permission_keyboard));
                }

            }
        });
    }

    private void showSnackbar(String text) {
        if (text != null && !text.isEmpty()) {
            Snackbar.make(findViewById(R.id.main_layout), text, Snackbar.LENGTH_LONG).show();
        }
    }

    private void showToast(int text) {
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
    }

}
