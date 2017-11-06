package at.shockbytes.remote.core;

import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.transition.Explode;

import at.shockbytes.remote.R;
import at.shockbytes.remote.fragment.LoginFragment;
import at.shockbytes.remote.util.RemiUtils;

@SuppressWarnings("unchecked")
public class LoginActivity extends AppCompatActivity
        implements LoginFragment.OnLoginActionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ((RemiApp) getApplication()).getAppComponent().inject(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorAccent));
            getWindow().setExitTransition(new Explode());
            getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.colorAccent));
        }

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.login_content, LoginFragment.newInstance())
                .commit();
    }

    @Override
    public void onStartAppTour() {
        ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(this);
        startActivity(AppTourActivity.Companion.newIntent(this), optionsCompat.toBundle());
    }

    @Override
    public void onConnected() {
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(this);
        startActivity(MainActivity.newIntent(this), options.toBundle());
    }

    @Override
    public void onConnectionFailed(int resultCode) {

        String msg = RemiUtils.getConnectionErrorByResultCode(this, resultCode);
        Snackbar.make(findViewById(R.id.login_content), msg, Snackbar.LENGTH_SHORT).show();
    }

}
