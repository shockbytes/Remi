package at.shockbytes.remote.dagger;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Vibrator;
import android.preference.PreferenceManager;

import com.google.gson.Gson;

import javax.inject.Singleton;

import at.shockbytes.remote.network.RemiClient;
import at.shockbytes.remote.network.security.AndroidSecurityManager;
import at.shockbytes.remote.network.security.DefaultAndroidSecurityManager;
import at.shockbytes.remote.wear.AndroidWearManager;
import at.shockbytes.remote.wear.WearableManager;
import dagger.Module;
import dagger.Provides;

/**
 * @author Martin Macheiner
 *         Date: 26.09.2017.
 */

@Module
public class AppModule {

    private final Application app;

    public AppModule(Application app) {
        this.app = app;
    }

    @Provides
    @Singleton
    Vibrator provideVibrator() {
        return (Vibrator) app.getSystemService(Context.VIBRATOR_SERVICE);
    }

    @Provides
    @Singleton
    SharedPreferences providedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(app);
    }

    @Provides
    @Singleton
    Gson provideGson() {
        return new Gson();
    }

    @Provides
    @Singleton
    WearableManager provideWearableManager(RemiClient client, Gson gson) {
        return new AndroidWearManager(app.getApplicationContext(), client, gson);
    }

    @Provides
    @Singleton
    AndroidSecurityManager provideSecurityManager() {
        return new DefaultAndroidSecurityManager(app.getApplicationContext());
    }

}
