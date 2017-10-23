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
import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;


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
    OkHttpClient provideOkHttpClient() {

        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        return new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                //.hostnameVerifier(myHostnameVerifier)
                //.sslSocketFactory(mySSLContext.getSocketFactory(), myX509TrustManager)
                .build();
    }

    @Provides
    @Singleton
    RemiClient provideRemiClient(OkHttpClient okHttpClient,
                                 MessageSerializer serializer,
                                 MessageDeserializer msgDeserializer) {
        return new SocketIoRemiClient(okHttpClient, serializer, msgDeserializer);
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
