package at.shockbytes.remote.network

import android.util.Log
import at.shockbytes.remote.network.message.MessageDeserializer
import at.shockbytes.remote.network.message.MessageSerializer
import at.shockbytes.remote.network.model.*
import at.shockbytes.remote.network.security.AndroidSecurityManager
import at.shockbytes.remote.util.RemiUtils
import at.shockbytes.remote.util.RemiUtils.Irrelevant
import at.shockbytes.remote.util.RemiUtils.Companion.eventName
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import io.socket.client.IO
import io.socket.client.Socket
import java.net.URISyntaxException
import java.util.concurrent.Callable

/**
 * @author Martin Macheiner
 * Date: 26.09.2017.
 */

class SocketIoRemiClient(private val msgSerializer: MessageSerializer,
                         private val msgDeserializer: MessageDeserializer,
                         private val securityManager: AndroidSecurityManager) : RemiClient {

    override val desktopOS: String
        get() = connectionConfig.desktopOS

    override val port: Int
        get() = STD_PORT

    override val connectionPermissions: ConnectionConfig.ConnectionPermissions
        get() = connectionConfig.permissions

    override val isSSLEnabled: Boolean
        get() = IS_SSL_ENABLED

    private lateinit var socket: Socket
    private var connectionConfig: ConnectionConfig

    private val connectedSubject: PublishSubject<Int>
    private val disconnectedSubject: PublishSubject<Any>
    private val requestAppsSubject: PublishSubject<List<String>>
    private val requestFilesSubject: PublishSubject<List<RemiFile>>
    private val requestFileTransferSubject: PublishSubject<FileTransferResponse>
    private val requestSlidesSubject: PublishSubject<SlidesResponse>

    init {

        connectionConfig = ConnectionConfig()

        connectedSubject = PublishSubject.create()
        disconnectedSubject = PublishSubject.create()
        requestAppsSubject = PublishSubject.create()
        requestFilesSubject = PublishSubject.create()
        requestFileTransferSubject = PublishSubject.create()
        requestSlidesSubject = PublishSubject.create()
    }

    override fun connect(app: DesktopApp): Observable<Int> {
        return Observable.defer(Callable<ObservableSource<Int>> {
            try {
                setupSocket(app)
                socket.connect()
            } catch (e: URISyntaxException) {
                e.printStackTrace()
                return@Callable Observable.error(e)
            }
            connectedSubject
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io())
    }

    override fun disconnect(): Observable<Any> {
        return Observable.defer(Callable<ObservableSource<Any>> {
            socket.disconnect()
            Observable.empty()
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io())
    }

    override fun listenForConnectionLoss(): Observable<Any> {
        return disconnectedSubject
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
    }

    override fun close() {
        socket.close()
    }

    override fun requestApps(): Observable<List<String>> {
        return Observable.defer {
            socket.emit(RemiClient.ClientEvent.REQ_APPS.name.toLowerCase())
            requestAppsSubject
        }.observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io())
    }

    override fun removeApp(app: String): Observable<RemiUtils.Irrelevant> {
        return Observable.defer {
            socket.emit(RemiClient.ClientEvent.REMOVE_APP.name.toLowerCase(), app)
            Observable.just(RemiUtils.Irrelevant.INSTANCE)
        }.observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io())
    }

    override fun sendAppExecutionRequest(app: String): Observable<RemiUtils.Irrelevant> {
        return Observable.defer {
            socket.emit(RemiClient.ClientEvent.START_APP.name.toLowerCase(), app)
            Observable.just(RemiUtils.Irrelevant.INSTANCE)
        }.observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io())
    }

    override fun sendAddAppRequest(pathToApp: String): Observable<RemiUtils.Irrelevant> {
        return Observable.defer {
            socket.emit(RemiClient.ClientEvent.ADD_APP.name.toLowerCase(), pathToApp)
            Observable.just(RemiUtils.Irrelevant.INSTANCE)
        }.observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io())
    }

    override fun sendAppOpenRequest(pathToApp: String): Observable<RemiUtils.Irrelevant> {
        return Observable.defer {
            socket.emit(RemiClient.ClientEvent.OPEN_APP_ON_DESKTOP.name.toLowerCase(), pathToApp)
            Observable.just(RemiUtils.Irrelevant.INSTANCE)
        }.observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io())
    }

    override fun sendLeftClick(): Observable<RemiUtils.Irrelevant> {
        return Observable.defer {
            socket.emit(RemiClient.ClientEvent.MOUSE_CLICK_LEFT.name.toLowerCase())
            Observable.just(RemiUtils.Irrelevant.INSTANCE)
        }.observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io())
    }

    override fun sendRightClick(): Observable<RemiUtils.Irrelevant> {
        return Observable.defer {
            socket.emit(RemiClient.ClientEvent.MOUSE_CLICK_RIGHT.name.toLowerCase())
            Observable.just(RemiUtils.Irrelevant.INSTANCE)
        }.observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io())
    }

    override fun sendMouseMove(deltaX: Int, deltaY: Int): Observable<RemiUtils.Irrelevant> {
        return Observable.defer {
            socket.emit(RemiClient.ClientEvent.MOUSE_MOVE.name.toLowerCase(),
                    msgSerializer.mouseMoveMessage(deltaX, deltaY))
            Observable.just(RemiUtils.Irrelevant.INSTANCE)
        }.observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io())
    }

    override fun sendScroll(amount: Int): Observable<RemiUtils.Irrelevant> {
        return Observable.defer {
            socket.emit(eventName(RemiClient.ClientEvent.SCROLL), amount)
            Observable.just(RemiUtils.Irrelevant.INSTANCE)
        }.observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io())
    }

    override fun requestBaseDirectories(): Observable<List<RemiFile>> {
        return Observable.defer {
            socket.emit(eventName(RemiClient.ClientEvent.REQ_BASE_DIR))
            requestFilesSubject
        }.observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io())
    }

    override fun requestDirectory(dir: String): Observable<List<RemiFile>> {
        return Observable.defer {
            socket.emit(eventName(RemiClient.ClientEvent.REQ_DIR), dir)
            requestFilesSubject
        }.observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io())
    }

    override fun transferFile(filepath: String): Observable<FileTransferResponse> {
        return Observable.defer {
            socket.emit(eventName(RemiClient.ClientEvent.REQ_FILE_TRANSFER), filepath)
            requestFileTransferSubject
        }.observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io())
    }

    override fun writeText(keyCode: Int, isCapsLock: Boolean): Observable<RemiUtils.Irrelevant> {
        return Observable.defer {
            socket.emit(eventName(RemiClient.ClientEvent.WRITE_TEXT),
                    msgSerializer.writeTextMessage(keyCode, isCapsLock))
            Observable.just(RemiUtils.Irrelevant.INSTANCE)
        }.observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io())
    }

    override fun sendSlidesNextCommand(): Observable<RemiUtils.Irrelevant> {
        return writeText(RemiUtils.KEYCODE_NEXT, false)
    }

    override fun sendSlidesPreviousCommand(): Observable<RemiUtils.Irrelevant> {
        return writeText(RemiUtils.KEYCODE_BACK, false)
    }

    override fun sendSlidesFullscreenCommand(product: RemiClient.SlidesProduct): Observable<RemiUtils.Irrelevant> {
        return Observable.defer {
            socket.emit(eventName(RemiClient.ClientEvent.REQ_SLIDES_FULLSCREEN), product.ordinal)
            Observable.just(RemiUtils.Irrelevant.INSTANCE)
        }.observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io())
    }

    override fun requestSlides(filepath: String): Observable<SlidesResponse> {
        return Observable.defer {
            socket.emit(eventName(RemiClient.ClientEvent.REQ_SLIDES), filepath)
            requestSlidesSubject
        }.observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io())
    }

    @Throws(Exception::class)
    private fun setupSocket(app: DesktopApp) {

        val okHttpClient = RemiUtils.getOkHttpClient(securityManager, app, isSSLEnabled)

        val opts = IO.Options()
        opts.callFactory = okHttpClient
        opts.webSocketFactory = okHttpClient
        opts.secure = isSSLEnabled
        opts.timeout = 5000

        opts.port = STD_PORT

        val serverUrl = RemiUtils.createUrlFromIp(app.ip, port, isSSLEnabled)
        socket = IO.socket(serverUrl, opts)

        socket
                .on(Socket.EVENT_CONNECT) { Log.d("Remi", "Connected to Desktop App!") }
                .on(Socket.EVENT_DISCONNECT) { disconnectedSubject.onNext(Irrelevant.INSTANCE) }
                .on(eventName(RemiClient.ServerEvent.WELCOME)) { args ->
                    connectionConfig = msgDeserializer.welcomeMessage(args[0] as String)
                    connectedSubject.onNext(RemiClient.CONNECTION_RESULT_OK)
                }
                .on(eventName(RemiClient.ServerEvent.ALREADY_CONNECTED)) {
                    connectedSubject.onNext(RemiClient.CONNECTION_RESULT_ERROR_ALREADY_CONNECTED)
                }
                .on(eventName(RemiClient.ServerEvent.RESP_APPS)) { args ->
                    requestAppsSubject.onNext(msgDeserializer.requestAppsMessage(args[0] as String))
                }
                .on(eventName(RemiClient.ServerEvent.RESP_DIR)) { args ->
                    requestFilesSubject.onNext(msgDeserializer.requestFilesMessage(args[0] as String))
                }
                .on(eventName(RemiClient.ServerEvent.RESP_FILE_TRANSFER)) { args ->
                    requestFileTransferSubject.onNext(msgDeserializer.fileTransferMessage(args[0] as String))
                }
                .on(eventName(RemiClient.ServerEvent.RESP_SLIDES)) { args ->
                    requestSlidesSubject.onNext(msgDeserializer.requestSlides(args[0] as String))
                }
    }

    companion object {

        private val IS_SSL_ENABLED = true
        private val STD_PORT = 9627
    }

}
