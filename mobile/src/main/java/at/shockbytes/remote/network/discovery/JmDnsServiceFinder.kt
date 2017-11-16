package at.shockbytes.remote.network.discovery

import android.content.Context

import at.shockbytes.remote.network.model.DesktopApp
import de.mannodermaus.rxbonjour.BonjourEvent
import de.mannodermaus.rxbonjour.BonjourService
import de.mannodermaus.rxbonjour.RxBonjour
import de.mannodermaus.rxbonjour.drivers.jmdns.JmDNSDriver
import de.mannodermaus.rxbonjour.platforms.android.AndroidPlatform
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.annotations.NonNull
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject

/**
 * @author Martin Macheiner
 * Date: 10.10.2017.
 */

class JmDnsServiceFinder(context: Context) : ServiceFinder {

    private val rxBonjour: RxBonjour = RxBonjour.Builder()
            .platform(AndroidPlatform.create(context))
            .driver(JmDNSDriver.create())
            .create()

    private val publishSubject: PublishSubject<DesktopApp> = PublishSubject.create()
    private var observer: EventObserver? = null

    override val isListening: Boolean
        get() = (observer?.isDisposed == false)

    override fun stopListening() {
        if (isListening) {
            observer?.dispose()
        }
    }

    override fun lookForDesktopApps(): Observable<DesktopApp> {

        if (!isListening) {
            observer = rxBonjour.newDiscovery(SERVICE_TYPE)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(EventObserver())
        }
        return publishSubject
    }

    private fun service2DesktopApp(service: BonjourService): DesktopApp {
        val m = service.txtRecords
        val signature = m["signature_1"] + m["signature_2"]
        return DesktopApp(m["name"]!!, m["ip"]!!, m["os"]!!, signature, false)
    }

    private inner class EventObserver : DisposableObserver<BonjourEvent>() {

        override fun onNext(@NonNull event: BonjourEvent) {
            if (event.javaClass.simpleName == "Added"
                    && event.service.type.contains(SERVICE_TYPE)
                    && event.service.name == SERVICE_NAME
                    && !event.service.txtRecords.isEmpty()) {
                publishSubject.onNext(service2DesktopApp(event.service))
            }
        }

        override fun onError(@NonNull e: Throwable) {
            e.printStackTrace()
            dispose()
        }

        override fun onComplete() {
            dispose()
        }
    }

    companion object {
        private const val SERVICE_TYPE = "_http._tcp"
        private const val SERVICE_NAME = "remi_desktop_app"
    }

}
