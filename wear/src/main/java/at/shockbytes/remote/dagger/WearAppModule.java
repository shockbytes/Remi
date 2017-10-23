package at.shockbytes.remote.dagger;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Vibrator;
import android.preference.PreferenceManager;

import com.google.gson.Gson;

import javax.inject.Singleton;

import at.shockbytes.remote.communication.CommunicationManager;
import at.shockbytes.remote.communication.DefaultCommunicationManager;
import dagger.Module;
import dagger.Provides;

/**
 * @author Martin Macheiner
 *         Date: 21.02.2017.
 */
@Module
public class WearAppModule {

    private final Application app;

    public WearAppModule(Application app) {
        this.app = app;
    }

    @Provides
    @Singleton
    SharedPreferences provideSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(app);
    }

    @Provides
    @Singleton
    CommunicationManager provideMobileManager(Gson gson) {
        return new DefaultCommunicationManager(app.getApplicationContext(), gson);
    }

    @Provides
    @Singleton
    Vibrator provideVibrator() {
        return (Vibrator) app.getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
    }

    @Provides
    @Singleton
    Gson provideGson() {
        return new Gson();
    }
}
