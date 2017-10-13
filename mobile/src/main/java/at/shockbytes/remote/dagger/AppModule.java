package at.shockbytes.remote.dagger;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Vibrator;
import android.preference.PreferenceManager;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * @author Martin Macheiner
 *         Date: 26.09.2017.
 */

@Module
public class AppModule {

    private Application app;

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

}
