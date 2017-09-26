package at.shockbytes.remote.dagger;

import android.app.Application;

import javax.inject.Singleton;

import at.shockbytes.remote.network.RemiClient;
import at.shockbytes.remote.network.SocketIoRemiClient;
import dagger.Module;
import dagger.Provides;

/**
 * @author Martin Macheiner
 *         Date: 26.09.2017.
 */

@Module
public class NetworkModule {

    private Application app;

    public NetworkModule(Application app) {
        this.app = app;
    }

    @Singleton
    @Provides
    public RemiClient provideRemiClient() {
        return new SocketIoRemiClient();
    }

}
