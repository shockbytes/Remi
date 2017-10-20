package at.shockbytes.remote.core;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.transition.Explode;
import android.transition.Slide;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.Window;

import at.shockbytes.remote.fragment.help.MainHelpFragment;

public class HelpActivity extends AppCompatActivity {

    public static Intent newIntent(Context context) {
        return new Intent(context, HelpActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
            getWindow().setExitTransition(new Slide(Gravity.BOTTOM));
            getWindow().setEnterTransition(new Explode());
        }

        if(getSupportActionBar() != null){
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        getSupportFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content, MainHelpFragment.newInstance())
                .commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {

            if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                getSupportFragmentManager().popBackStack();
            } else {
                supportFinishAfterTransition();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
