package at.shockbytes.remote.core;

import android.app.Application;

import at.shockbytes.remote.dagger.DaggerWearAppComponent;
import at.shockbytes.remote.dagger.WearAppComponent;
import at.shockbytes.remote.dagger.WearAppModule;


/**
 * @author Martin Macheiner
 *         Date: 20.10.2017.
 */

public class WearRemiApp extends Application {

    private WearAppComponent appComponent;

    @Override
    public void onCreate() {
        super.onCreate();

        appComponent = DaggerWearAppComponent.builder()
                .wearAppModule(new WearAppModule(this))
                .build();
    }

    public WearAppComponent getAppComponent() {
        return appComponent;
    }

}
