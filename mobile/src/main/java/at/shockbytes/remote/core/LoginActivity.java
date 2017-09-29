package at.shockbytes.remote.core;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.transition.Explode;

import at.shockbytes.remote.R;
import at.shockbytes.remote.fragment.LoginFragment;

public class LoginActivity extends AppCompatActivity
        implements LoginFragment.OnConnectionResponseListener{

    public static Intent newIntent(Context context) {
        return new Intent(context, LoginActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorAccent));
            getWindow().setExitTransition(new Explode());
        }

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.login_content, LoginFragment.newInstance())
                .commit();
    }

    @Override
    public void onConnected() {

        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(this);
        startActivity(MainActivity.newIntent(this, false), options.toBundle());
        //supportFinishAfterTransition();
    }

    @Override
    public void onConnectionFailed(Throwable t) {
        Snackbar.make(findViewById(R.id.login_content), t.getMessage(), Snackbar.LENGTH_SHORT).show();
    }
}
