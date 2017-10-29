package at.shockbytes.remote.dagger;

import android.app.Application;

import javax.inject.Singleton;

import at.shockbytes.remote.network.RemiClient;
import at.shockbytes.remote.network.SocketIoRemiClient;
import at.shockbytes.remote.network.discovery.JmDnsServiceFinder;
import at.shockbytes.remote.network.discovery.ServiceFinder;
import at.shockbytes.remote.network.message.JsonMessageDeserializer;
import at.shockbytes.remote.network.message.JsonMessageSerializer;
import at.shockbytes.remote.network.message.MessageDeserializer;
import at.shockbytes.remote.network.message.MessageSerializer;
import at.shockbytes.remote.network.security.AndroidSecurityManager;
import dagger.Module;
import dagger.Provides;


/**
 * @author Martin Macheiner
 *         Date: 26.09.2017.
 */

@Module
public class NetworkModule {

    private final Application app;

    public NetworkModule(Application app) {
        this.app = app;
    }

    @Provides
    @Singleton
    RemiClient provideRemiClient(MessageSerializer serializer,
                                 MessageDeserializer msgDeserializer,
                                 AndroidSecurityManager securityManager) {
        return new SocketIoRemiClient(serializer, msgDeserializer, securityManager);
    }

    @Provides
    @Singleton
    MessageSerializer provideMessageSerializer() {
        return new JsonMessageSerializer();
    }

    @Provides
    @Singleton
    MessageDeserializer provideMessageDeserializer() {
        return new JsonMessageDeserializer();
    }

    @Provides
    @Singleton
    ServiceFinder provideServiceFinder() {
        return new JmDnsServiceFinder(app.getApplicationContext());
    }

}
