package at.shockbytes.remote.core;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import at.shockbytes.remote.R;
import at.shockbytes.remote.fragment.LoginFragment;

public class LoginActivity extends AppCompatActivity {

    public static Intent newIntent(Context context) {
        return new Intent(context, LoginActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.login_content, LoginFragment.newInstance())
                .commit();
    }
}
