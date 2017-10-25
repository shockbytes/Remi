package at.shockbytes.remote.network.discovery;

import android.content.Context;

import java.util.Map;

import at.shockbytes.remote.network.model.DesktopApp;
import de.mannodermaus.rxbonjour.BonjourEvent;
import de.mannodermaus.rxbonjour.BonjourService;
import de.mannodermaus.rxbonjour.RxBonjour;
import de.mannodermaus.rxbonjour.drivers.jmdns.JmDNSDriver;
import de.mannodermaus.rxbonjour.platforms.android.AndroidPlatform;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

/**
 * @author Martin Macheiner
 *         Date: 10.10.2017.
 */

public class JmDnsServiceFinder implements ServiceFinder {

    private final RxBonjour rxBonjour;

    private static final String SERVICE_TYPE = "_http._tcp";
    private static final String SERVICE_NAME = "remi_desktop_app";

    private final PublishSubject<DesktopApp> publishSubject;
    private EventObserver observer;

    public JmDnsServiceFinder(Context context) {

        publishSubject = PublishSubject.create();
        rxBonjour = new RxBonjour.Builder()
                .platform(AndroidPlatform.create(context))
                .driver(JmDNSDriver.create())
                .create();
    }

    @Override
    public boolean isListening() {
        return observer != null && !observer.isDisposed();
    }

    @Override
    public void stopListening() {
        if (isListening()) {
            observer.dispose();
        }
    }

    @Override
    public Observable<DesktopApp> lookForDesktopApps() {

        if (!isListening())  {
            observer = rxBonjour.newDiscovery(SERVICE_TYPE)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(new EventObserver());
        }
        return publishSubject;
    }

    private DesktopApp service2DesktopApp(BonjourService service) {
        Map<String, String> m = service.getTxtRecords();
        return new DesktopApp(m.get("name"), m.get("ip"), m.get("os"),
                m.get("signature"), m.get("public_key"));
    }

    private class EventObserver extends DisposableObserver<BonjourEvent> {

        @Override
        public void onNext(@NonNull BonjourEvent event) {

            if (event.getClass().getSimpleName().equals("Added")
                    && event.getService().getType().contains(SERVICE_TYPE)
                    && event.getService().getName().equals(SERVICE_NAME)
                    && !event.getService().getTxtRecords().isEmpty()) {
                publishSubject.onNext(service2DesktopApp(event.getService()));
            }
        }

        @Override
        public void onError(@NonNull Throwable e) {
            e.printStackTrace();
            dispose();
        }

        @Override
        public void onComplete() {
            dispose();
        }
    }

}
