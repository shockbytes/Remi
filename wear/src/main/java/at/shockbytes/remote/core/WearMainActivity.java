package at.shockbytes.remote.core;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.wear.widget.drawer.WearableDrawerLayout;
import android.support.wear.widget.drawer.WearableNavigationDrawerView;
import android.support.wearable.activity.WearableActivity;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import at.shockbytes.remote.R;
import at.shockbytes.remote.adapter.ShockNavigationAdapter;
import at.shockbytes.remote.communication.CommunicationManager;
import at.shockbytes.remote.fragment.WearAppsFragment;
import at.shockbytes.remote.fragment.WearMouseFragment;
import at.shockbytes.remote.fragment.WearSlidesFragment;
import at.shockbytes.remote.util.WearAppParams;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class WearMainActivity extends WearableActivity
        implements WearableNavigationDrawerView.OnItemSelectedListener {

    @BindView(R.id.main_navigation_drawer)
    protected WearableNavigationDrawerView navigationDrawer;

    @BindView(R.id.main_drawer_layout)
    protected WearableDrawerLayout drawerLayout;

    @Inject
    protected CommunicationManager gateway;

    private Unbinder unbinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setAmbientEnabled();
        ((WearRemiApp) getApplication()).getAppComponent().inject(this);
        unbinder = ButterKnife.bind(this);
        gateway.connect();
        setupNavigationDrawer();
    }

    @Override
    protected void onStart() {
        super.onStart();
        gateway.onStart(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        gateway.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }

    @Override
    public void onItemSelected(int pos) {

        FragmentTransaction transaction = getFragmentManager().beginTransaction()
                .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);
        switch (pos) {

            case WearAppParams.NAVIGATION_POSITION_APPS:
                transaction.replace(R.id.wearable_main_content, WearAppsFragment.newInstance());
                break;

            case WearAppParams.NAVIGATION_POSITION_MOUSE:
                transaction.replace(R.id.wearable_main_content, WearMouseFragment.newInstance());
                break;

            case WearAppParams.NAVIGATION_POSITION_SLIDES:
                transaction.replace(R.id.wearable_main_content, WearSlidesFragment.newInstance());
                break;
        }
        transaction.commit();
    }

    @NonNull
    private List<ShockNavigationAdapter.NavigationItem> getNavigationItems() {

        return Arrays.asList(
                new ShockNavigationAdapter.NavigationItem(R.string.navigation_apps,
                        R.drawable.ic_tab_apps),
                new ShockNavigationAdapter.NavigationItem(R.string.navigation_mouse,
                        R.drawable.ic_tab_mouse),
                new ShockNavigationAdapter.NavigationItem(R.string.navigation_slides,
                        R.drawable.ic_tab_slides));
    }

    private void setupNavigationDrawer() {
        navigationDrawer.setAdapter(new ShockNavigationAdapter(this, getNavigationItems()));
        navigationDrawer.addOnItemSelectedListener(this);
        navigationDrawer.getController().peekDrawer();
        navigationDrawer.setCurrentItem(WearAppParams.NAVIGATION_POSITION_MOUSE, true);
    }
}
