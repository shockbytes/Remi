package at.shockbytes.remote.core;

import android.app.Application;

import at.shockbytes.remote.dagger.AppComponent;
import at.shockbytes.remote.dagger.AppModule;
import at.shockbytes.remote.dagger.DaggerAppComponent;
import at.shockbytes.remote.dagger.NetworkModule;
import butterknife.ButterKnife;

/**
 * @author Martin Macheiner
 *         Date: 26.09.2017.
 */

public class RemiApp extends Application {

    private AppComponent appComponent;

    @Override
    public void onCreate() {
        super.onCreate();

        ButterKnife.setDebug(true);

        appComponent = DaggerAppComponent.builder()
                .appModule(new AppModule(this))
                .networkModule(new NetworkModule(this))
                .build();
    }

    public AppComponent getAppComponent() {
        return appComponent;
    }
}
