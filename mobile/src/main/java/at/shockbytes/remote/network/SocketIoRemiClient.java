package at.shockbytes.remote.network;

import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.Callable;

import at.shockbytes.remote.network.message.MessageDeserializer;
import at.shockbytes.remote.network.message.MessageSerializer;
import at.shockbytes.remote.network.model.ConnectionConfig;
import at.shockbytes.remote.network.model.FileTransferResponse;
import at.shockbytes.remote.network.model.RemiFile;
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

    private Socket socket;
    private OkHttpClient okHttpClient;

    private MessageSerializer msgSerializer;
    private MessageDeserializer msgDeserializer;
    private ConnectionConfig connectionConfig;

    private PublishSubject<Integer> connectedSubject;
    private PublishSubject<Object> disconnectedSubject;
    private PublishSubject<List<String>> requestAppsSubject;
    private PublishSubject<List<RemiFile>> requestFilesSubject;
    private PublishSubject<FileTransferResponse> requestFiletransferSubject;

    public SocketIoRemiClient(OkHttpClient okHttpClient,
                              MessageSerializer msgSerializer, MessageDeserializer msgDeserializer) {
        this.okHttpClient = okHttpClient;
        this.msgSerializer = msgSerializer;
        this.msgDeserializer = msgDeserializer;

        connectionConfig = new ConnectionConfig();

        connectedSubject = PublishSubject.create();
        disconnectedSubject = PublishSubject.create();
        requestAppsSubject = PublishSubject.create();
        requestFilesSubject = PublishSubject.create();
        requestFiletransferSubject = PublishSubject.create();
    }

    @Override
    public Observable<Integer> connect(final String serverUrl) {
        return Observable.defer(new Callable<ObservableSource<Integer>>() {
            @Override
            public ObservableSource<Integer> call() throws Exception {
                try {
                    setupSocket(serverUrl);
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
    public ConnectionConfig.ConnectionPermissions getConnectionPermissions() {
        return connectionConfig.getPermissions();
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
                return requestFiletransferSubject;
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

    private void setupSocket(String serverUrl) throws URISyntaxException {

        IO.Options opts = new IO.Options();
        opts.callFactory = okHttpClient;
        opts.webSocketFactory = okHttpClient;
        opts.port = 8080;

        socket = IO.socket(serverUrl, opts);

        socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                // TODO Do something in here?
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
                requestFiletransferSubject
                        .onNext(msgDeserializer.fileTransferMessage((String) args[0]));
            }
        });
    }

}
