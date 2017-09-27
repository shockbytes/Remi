package at.shockbytes.remote.core;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import javax.inject.Inject;

import at.shockbytes.remote.R;
import at.shockbytes.remote.fragment.AppsFragment;
import at.shockbytes.remote.fragment.FilesFragment;
import at.shockbytes.remote.fragment.MouseFragment;
import at.shockbytes.remote.fragment.PresentationFragment;
import at.shockbytes.remote.network.RemiClient;
import at.shockbytes.remote.util.AppParams;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import icepick.Icepick;
import icepick.State;
import rx.functions.Action1;

public class MainActivity extends AppCompatActivity implements TabLayout.OnTabSelectedListener {

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

    private Unbinder unbinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ((RemiApp)getApplication()).getAppComponent().inject(this);
        unbinder = ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        tabPosition = AppParams.POSITION_MOUSE;
        Icepick.restoreInstanceState(this, savedInstanceState);

        initializeViews();
        startActivity(LoginActivity.newIntent(this));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setTitle(client.getDesktopOS());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
        client.disconnect().subscribe(new Action1<Void>() {
            @Override
            public void call(Void aVoid) {
                Toast.makeText(getApplicationContext(), "Disconnected", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.menu_main_logout:

                supportFinishAfterTransition();
                break;

            case R.id.menu_main_settings:

                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(this);
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
                ft.replace(R.id.main_content, AppsFragment.newInstance());
                break;

            case AppParams.POSITION_MOUSE:
                fabKeyboard.show();
                ft.replace(R.id.main_content, MouseFragment.newInstance());
                break;

            case AppParams.POSITION_FILES:
                fabKeyboard.hide();
                ft.replace(R.id.main_content, FilesFragment.newInstance());
                break;

            case AppParams.POSITION_PRESENTATION:
                fabKeyboard.hide();
                ft.replace(R.id.main_content, PresentationFragment.newInstance());
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

    private void initializeViews() {

        tabLayout.addOnTabSelectedListener(this);
        TabLayout.Tab initialTab = tabLayout.getTabAt(tabPosition);
        if (initialTab != null) {
            initialTab.select();
        }

        fabKeyboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "Show Keyboard", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
