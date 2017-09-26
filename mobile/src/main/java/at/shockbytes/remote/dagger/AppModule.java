package at.shockbytes.remote.dagger;

import android.app.Application;

import dagger.Module;

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

}
