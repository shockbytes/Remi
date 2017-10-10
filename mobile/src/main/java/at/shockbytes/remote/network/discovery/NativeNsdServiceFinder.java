package at.shockbytes.remote.network.discovery;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;

import java.net.InetAddress;
import java.util.concurrent.Callable;

import at.shockbytes.remote.network.model.DesktopApp;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

/**
 * @author Martin Macheiner
 *         Date: 09.10.2017.
 */

public class NativeNsdServiceFinder implements ServiceFinder {

    private final String TAG = "Remi";
    private NsdManager.DiscoveryListener discoveryListener;
    private NsdManager.ResolveListener resolveListener;
    private NsdManager nsdManager;

    private NsdServiceInfo mService;

    private static final String SERVICE_TYPE = "_remi._tcp.";
    private static final String mServiceName = "remi_desktop_app";

    private PublishSubject<DesktopApp> publishSubject;

    private boolean isListening;

    public NativeNsdServiceFinder(Context context) {
        publishSubject = PublishSubject.create();
        nsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
        initializeDiscoveryListener();
        initializeResolveListener();
    }

    private void initializeDiscoveryListener() {

        // Instantiate a new DiscoveryListener
        discoveryListener = new NsdManager.DiscoveryListener() {

            //  Called as soon as service discovery begins.
            @Override
            public void onDiscoveryStarted(String regType) {
                Log.d(TAG, "Service discovery started");
            }

            @Override
            public void onServiceFound(NsdServiceInfo service) {
                // A service was found!  Do something with it.

                if (service.getServiceType().equals(SERVICE_TYPE)
                        && service.getServiceName().equals(mServiceName)) {
                    nsdManager.resolveService(service, resolveListener);
                }

                /*
                Log.d(TAG, "Service discovery success: " + service);
                if (!service.getServiceType().equals(SERVICE_TYPE)) {
                    // Service type is the string containing the protocol and
                    // transport layer for this service.
                    Log.d(TAG, "Unknown Service Type: " + service.getServiceType());
                } else if (service.getServiceName().equals(mServiceName)) {
                    // The name of the service tells the user what they'd be
                    // connecting to. It could be "Bob's Chat App".
                    Log.d(TAG, "Same machine: " + mServiceName);
                    nsdManager.resolveService(service, resolveListener);
                } else if (service.getServiceName().contains("NsdChat")){
                    Log.wtf(TAG, "Resolving with service name: " + service.getServiceName());
                    nsdManager.resolveService(service, resolveListener);
                } */
            }

            @Override
            public void onServiceLost(NsdServiceInfo service) {
                // When the network service is no longer available.
                // Internal bookkeeping code goes here.
                Log.e(TAG, "service lost" + service);
            }

            @Override
            public void onDiscoveryStopped(String serviceType) {
                Log.i(TAG, "Discovery stopped: " + serviceType);
            }

            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Start Discovery failed: Error code:" + errorCode + " / type: "  + serviceType);
                //nsdManager.stopServiceDiscovery(this);
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Stop Discovery failed: Error code:" + errorCode + " / type: "  + serviceType);
                //nsdManager.stopServiceDiscovery(this);
            }
        };
    }

    private void initializeResolveListener() {
        resolveListener = new NsdManager.ResolveListener() {

            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                // Called when the resolve fails.  Use the error code to debug.
                Log.e(TAG, "Resolve failed" + errorCode);
            }

            @Override
            public void onServiceResolved(NsdServiceInfo serviceInfo) {
                Log.e(TAG, "Resolve Succeeded. " + serviceInfo);

                if (serviceInfo.getServiceName().equals(mServiceName)) {
                    Log.d(TAG, "Same IP.");
                    //return;
                }
                mService = serviceInfo;
                int port = mService.getPort();
                InetAddress host = mService.getHost();
                Log.wtf(TAG, "PORT: " + port + " / Host: " + host);
            }
        };
    }

    @Override
    public boolean isListening() {
        return isListening;
    }

    @Override
    public void stopListening() {
        if (isListening) {
            nsdManager.stopServiceDiscovery(discoveryListener);
            isListening = false;
        }
    }

    @Override
    public Observable<DesktopApp> lookForDesktopApps() {
        return Observable.defer(new Callable<ObservableSource<DesktopApp>>() {
            @Override
            public ObservableSource<DesktopApp> call() throws Exception {
                nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener);
                isListening = true;
                return publishSubject;
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
    }

}
