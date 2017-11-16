package at.shockbytes.remote.core;

import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.transition.Explode;

import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

import at.shockbytes.remote.R;
import at.shockbytes.remote.dagger.AppComponent;
import at.shockbytes.remote.fragment.LoginFragment;
import at.shockbytes.remote.network.security.AndroidSecurityManager;
import at.shockbytes.remote.util.RemiUtils;

@SuppressWarnings("unchecked")
public class LoginActivity extends BaseActivity
        implements LoginFragment.OnLoginActionListener {

    @Inject
    protected AndroidSecurityManager securityManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorAccent));
            getWindow().setExitTransition(new Explode());
            getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.colorAccent));
        }

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.login_content, LoginFragment.Companion.newInstance())
                .commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        securityManager.close();
    }

    @Override
    public void onStartAppTour() {
        ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(this);
        startActivity(AppTourActivity.Companion.newIntent(this), optionsCompat.toBundle());
    }

    @Override
    public void onConnected() {
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(this);
        startActivity(MainActivity.Companion.newIntent(this), options.toBundle());
    }

    @Override
    public void onConnectionFailed(int resultCode) {
        String msg = RemiUtils.Companion.getConnectionErrorByResultCode(this, resultCode);
        Snackbar.make(findViewById(R.id.login_content), msg, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void injectToGraph(@NotNull AppComponent appComponent) {
        appComponent.inject(this);
    }
}
