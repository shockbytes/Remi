package at.shockbytes.remote.network;

import android.util.Log;

import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.Callable;

import at.shockbytes.remote.network.message.MessageDeserializer;
import at.shockbytes.remote.network.message.MessageSerializer;
import at.shockbytes.remote.network.model.ConnectionConfig;
import at.shockbytes.remote.network.model.FileTransferResponse;
import at.shockbytes.remote.network.model.RemiFile;
import at.shockbytes.remote.network.model.SlidesResponse;
import at.shockbytes.remote.network.security.AndroidSecurityManager;
import at.shockbytes.remote.util.RemiUtils;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import okhttp3.OkHttpClient;

import static at.shockbytes.remote.util.RemiUtils.Irrelevant;
import static at.shockbytes.remote.util.RemiUtils.eventName;

/**
 * @author Martin Macheiner
 *         Date: 26.09.2017.
 */

public class SocketIoRemiClient implements RemiClient {

    private static final boolean IS_SSL_ENABLED = true;
    private static final int STD_PORT = 9627;

    private Socket socket;
    private final AndroidSecurityManager securityManager;

    private final MessageSerializer msgSerializer;
    private final MessageDeserializer msgDeserializer;
    private ConnectionConfig connectionConfig;

    private final PublishSubject<Integer> connectedSubject;
    private final PublishSubject<Object> disconnectedSubject;
    private final PublishSubject<List<String>> requestAppsSubject;
    private final PublishSubject<List<RemiFile>> requestFilesSubject;
    private final PublishSubject<FileTransferResponse> requestFileTransferSubject;
    private final PublishSubject<SlidesResponse> requestSlidesSubject;

    public SocketIoRemiClient(MessageSerializer msgSerializer,
                              MessageDeserializer msgDeserializer,
                              AndroidSecurityManager securityManager) {
        this.msgSerializer = msgSerializer;
        this.msgDeserializer = msgDeserializer;
        this.securityManager = securityManager;

        connectionConfig = new ConnectionConfig();

        connectedSubject = PublishSubject.create();
        disconnectedSubject = PublishSubject.create();
        requestAppsSubject = PublishSubject.create();
        requestFilesSubject = PublishSubject.create();
        requestFileTransferSubject = PublishSubject.create();
        requestSlidesSubject = PublishSubject.create();
    }

    @Override
    public Observable<Integer> connect(final String desktopUrl) {
        return Observable.defer(new Callable<ObservableSource<Integer>>() {
            @Override
            public ObservableSource<Integer> call() throws Exception {
                try {
                    setupSocket(desktopUrl);
                    socket.connect();
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                    return Observable.error(e);
                }
                return connectedSubject
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io());
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
    }

    @Override
    public Observable<Object> disconnect() {
        return Observable.defer(new Callable<ObservableSource<Object>>() {
            @Override
            public ObservableSource<Object> call() {
                socket.disconnect();
                return Observable.empty();
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
    }

    @Override
    public Observable<Object> listenForConnectionLoss() {
        return disconnectedSubject
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io());
    }

    @Override
    public void close() {
        socket.close();
    }

    public String getDesktopOS() {
        return connectionConfig.getDesktopOS();
    }

    @Override
    public int getPort() {
        return STD_PORT;
    }

    @Override
    public ConnectionConfig.ConnectionPermissions getConnectionPermissions() {
        return connectionConfig.getPermissions();
    }

    @Override
    public boolean isSSLEnabled() {
        return IS_SSL_ENABLED;
    }

    @Override
    public Observable<List<String>> requestApps() {
        return Observable.defer(new Callable<ObservableSource<List<String>>>() {
            @Override
            public ObservableSource<List<String>> call() throws Exception {
                socket.emit(ClientEvent.REQ_APPS.name().toLowerCase());
                return requestAppsSubject;
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
    }

    @Override
    public Observable<Object> removeApp(final String app) {
        return Observable.defer(new Callable<ObservableSource<Object>>() {
            @Override
            public ObservableSource<Object> call() {
                socket.emit(ClientEvent.REMOVE_APP.name().toLowerCase(), app);
                return Observable.empty();
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
    }

    @Override
    public Observable<Object> sendAppExecutionRequest(final String app) {
        return Observable.defer(new Callable<ObservableSource<Object>>() {
            @Override
            public ObservableSource<Object> call() {
                socket.emit(ClientEvent.START_APP.name().toLowerCase(), app);
                return Observable.empty();
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
    }

    @Override
    public Observable<Object> sendAddAppRequest(final String pathToApp) {
        return Observable.defer(new Callable<ObservableSource<Object>>() {
            @Override
            public ObservableSource<Object> call() {
                socket.emit(ClientEvent.ADD_APP.name().toLowerCase(), pathToApp);
                return Observable.empty();
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
    }

    @Override
    public Observable<Object> sendAppOpenRequest(final String pathToApp) {
        return Observable.defer(new Callable<ObservableSource<Object>>() {
            @Override
            public ObservableSource<Object> call() {
                socket.emit(ClientEvent.OPEN_APP_ON_DESKTOP.name().toLowerCase(), pathToApp);
                return Observable.empty();
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
    }

    @Override
    public Observable<Object> sendLeftClick() {
        return Observable.defer(new Callable<ObservableSource<Object>>() {
            @Override
            public ObservableSource<Object> call() {
                socket.emit(ClientEvent.MOUSE_CLICK_LEFT.name().toLowerCase());
                return Observable.empty();
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
    }

    @Override
    public Observable<Object> sendRightClick() {
        return Observable.defer(new Callable<ObservableSource<Object>>() {
            @Override
            public ObservableSource<Object> call() {
                socket.emit(ClientEvent.MOUSE_CLICK_RIGHT.name().toLowerCase());
                return Observable.empty();
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
    }

    @Override
    public Observable<Object> sendMouseMove(final int deltaX, final int deltaY) {
        return Observable.defer(new Callable<ObservableSource<Object>>() {
            @Override
            public ObservableSource<Object> call() {
                socket.emit(ClientEvent.MOUSE_MOVE.name().toLowerCase(),
                        msgSerializer.mouseMoveMessage(deltaX, deltaY));
                return Observable.empty();
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
    }

    @Override
    public Observable<Object> sendScroll(final int amount) {
        return Observable.defer(new Callable<ObservableSource<Object>>() {
            @Override
            public ObservableSource<Object> call() {
                socket.emit(eventName(ClientEvent.SCROLL), amount);
                return Observable.empty();
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
    }

    @Override
    public Observable<List<RemiFile>> requestBaseDirectories() {
        return Observable.defer(new Callable<ObservableSource<List<RemiFile>>>() {
            @Override
            public ObservableSource<List<RemiFile>> call() throws Exception {
                socket.emit(eventName(ClientEvent.REQ_BASE_DIR));
                return requestFilesSubject;
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
    }

    @Override
    public Observable<List<RemiFile>> requestDirectory(final String dir) {
        return Observable.defer(new Callable<ObservableSource<List<RemiFile>>>() {
            @Override
            public ObservableSource<List<RemiFile>> call() throws Exception {
                socket.emit(eventName(ClientEvent.REQ_DIR), dir);
                return requestFilesSubject;
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
    }

    @Override
    public Observable<FileTransferResponse> transferFile(final String filepath) {
        return Observable.defer(new Callable<ObservableSource<FileTransferResponse>>() {
            @Override
            public ObservableSource<FileTransferResponse> call() throws Exception {
                socket.emit(eventName(ClientEvent.REQ_FILE_TRANSFER), filepath);
                return requestFileTransferSubject;
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
    }

    @Override
    public Observable<Object> writeText(final int keyCode, final boolean isCapsLock) {
        return Observable.defer(new Callable<ObservableSource<Object>>() {
            @Override
            public ObservableSource<Object> call() throws Exception {
                socket.emit(eventName(ClientEvent.WRITE_TEXT),
                        msgSerializer.writeTextMessage(keyCode, isCapsLock));
                return Observable.empty();
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
    }

    @Override
    public Observable<Object> sendSlidesNextCommand() {
        return writeText(RemiUtils.KEYCODE_NEXT, false);
    }

    @Override
    public Observable<Object> sendSlidesPreviousCommand() {
        return writeText(RemiUtils.KEYCODE_BACK, false);
    }

    @Override
    public Observable<Object> sendSlidesFullscreenCommand(final SlidesProduct product) {
        return Observable.defer(new Callable<ObservableSource<Object>>() {
            @Override
            public ObservableSource<Object> call() throws Exception {
                socket.emit(eventName(ClientEvent.REQ_SLIDES_FULLSCREEN), product.ordinal());
                return Observable.empty();
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
    }

    @Override
    public Observable<SlidesResponse> requestSlides(final String filepath) {
        return Observable.defer(new Callable<ObservableSource<SlidesResponse>>() {
            @Override
            public ObservableSource<SlidesResponse> call() throws Exception {
                socket.emit(eventName(ClientEvent.REQ_SLIDES), filepath);
                return requestSlidesSubject
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io());
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
    }

    private void setupSocket(String serverUrl) throws URISyntaxException {

        OkHttpClient okHttpClient = RemiUtils.getOkHttpClient(securityManager);

        IO.Options opts = new IO.Options();
        opts.callFactory = okHttpClient;
        opts.webSocketFactory = okHttpClient;
        opts.secure = isSSLEnabled();

        opts.port = STD_PORT;

        socket = IO.socket(serverUrl, opts);

        socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.d("Remi", "Connected to Desktop App!");
            }
        }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                disconnectedSubject.onNext(Irrelevant.INSTANCE);
            }
        }).on(eventName(ServerEvent.WELCOME), new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                connectionConfig = msgDeserializer.welcomeMessage((String) args[0]);
                connectedSubject.onNext(RemiClient.CONNECTION_RESULT_OK);
            }
        }).on(eventName(ServerEvent.ALREADY_CONNECTED), new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                connectedSubject.onNext(RemiClient.CONNECTION_RESULT_ERROR_ALREADY_CONNECTED);
            }
        }).on(eventName(ServerEvent.RESP_APPS), new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                requestAppsSubject
                        .onNext(msgDeserializer.requestAppsMessage((String) args[0]));
            }
        }).on(eventName(ServerEvent.RESP_DIR), new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                requestFilesSubject
                        .onNext(msgDeserializer.requestFilesMessage((String) args[0]));
            }
        }).on(eventName(ServerEvent.RESP_FILE_TRANSFER), new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                requestFileTransferSubject
                        .onNext(msgDeserializer.fileTransferMessage((String) args[0]));
            }
        }).on(eventName(ServerEvent.RESP_SLIDES), new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                requestSlidesSubject
                        .onNext(msgDeserializer.requestSlides((String) args[0]));
            }
        });
    }

}
