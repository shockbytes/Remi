package at.shockbytes.remote.network;

import android.support.annotation.NonNull;
import android.util.Log;

import java.net.URISyntaxException;
import java.util.List;

import at.shockbytes.remote.network.message.MessageDeserializer;
import at.shockbytes.remote.network.message.MessageSerializer;
import at.shockbytes.remote.network.model.FileTransferResponse;
import at.shockbytes.remote.network.model.RemiFile;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import okhttp3.OkHttpClient;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func0;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

/**
 * @author Martin Macheiner
 *         Date: 26.09.2017.
 */

/*
 * TODO byte[] b = Base64.decode(data,Base64.DEFAULT);
 */

public class SocketIoRemiClient implements RemiClient {

    private String desktopOS;

    private Socket socket;
    private OkHttpClient okHttpClient;
    private MessageSerializer msgSerializer;
    private MessageDeserializer msgDeserializer;

    // TODO Read about hot and cold observables
    private PublishSubject<Void> connectedSubject;
    private PublishSubject<Void> disconnectedSubject;
    private PublishSubject<List<String>> requestAppsSubject;

    public SocketIoRemiClient(OkHttpClient okHttpClient,
                              MessageSerializer msgSerializer, MessageDeserializer msgDeserializer) {
        this.okHttpClient = okHttpClient;
        this.msgSerializer = msgSerializer;
        this.msgDeserializer = msgDeserializer;

        connectedSubject = PublishSubject.create();
        disconnectedSubject = PublishSubject.create();
        requestAppsSubject = PublishSubject.create();
    }

    @Override
    public Observable<Void> connect(final String serverUrl) {

        return Observable.defer(new Func0<Observable<Void>>() {
            @Override
            public Observable<Void> call() {

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
    public Observable<Void> disconnect() {
        return Observable.defer(new Func0<Observable<Void>>() {
            @Override
            public Observable<Void> call() {
                socket.disconnect();
                return Observable.empty();
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
    }

    @Override
    public Observable<Void> listenForConnectionLoss() {
        return disconnectedSubject
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io());
    }

    @Override
    public void close() {
        socket.close();
    }

    public String getDesktopOS() {
        return desktopOS;
    }

    @Override
    public Observable<List<String>> requestApps() {
        return Observable.defer(new Func0<Observable<List<String>>>() {
            @Override
            public Observable<List<String>> call() {
                socket.emit(ClientEvent.REQ_APPS.name().toLowerCase());
                return requestAppsSubject;
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
    }

    @Override
    public Observable<Void> removeApp(final String app) {
        return Observable.defer(new Func0<Observable<Void>>() {
            @Override
            public Observable<Void> call() {
                socket.emit(ClientEvent.REMOVE_APP.name().toLowerCase(), app);
                return Observable.empty();
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
    }

    @Override
    public Observable<Void> sendAppExecutionRequest(final String app) {
        return Observable.defer(new Func0<Observable<Void>>() {
            @Override
            public Observable<Void> call() {
                socket.emit(ClientEvent.START_APP.name().toLowerCase(), app);
                return Observable.empty();
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
    }

    @Override
    public Observable<Void> sendAddAppRequest(final String pathToApp) {
        return Observable.defer(new Func0<Observable<Void>>() {
            @Override
            public Observable<Void> call() {
                socket.emit(ClientEvent.ADD_APP.name().toLowerCase(), pathToApp);
                return Observable.empty();
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
    }

    @Override
    public Observable<Void> sendLeftClick() {
        return Observable.defer(new Func0<Observable<Void>>() {
            @Override
            public Observable<Void> call() {
                socket.emit(ClientEvent.MOUSE_CLICK_LEFT.name().toLowerCase());
                return Observable.empty();
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
    }

    @Override
    public Observable<Void> sendRightClick() {
        return Observable.defer(new Func0<Observable<Void>>() {
            @Override
            public Observable<Void> call() {
                socket.emit(ClientEvent.MOUSE_CLICK_RIGHT.name().toLowerCase());
                return Observable.empty();
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
    }

    @Override
    public Observable<Void> sendMouseMove(final int deltaX, final int deltaY) {
        return Observable.defer(new Func0<Observable<Void>>() {
            @Override
            public Observable<Void> call() {
                socket.emit(ClientEvent.MOUSE_MOVE.name().toLowerCase(),
                        msgSerializer.mouseMoveMessage(deltaX, deltaY));
                return Observable.empty();
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
    }

    @Override
    public Observable<Void> sendScroll(final int amount) {
        return Observable.defer(new Func0<Observable<Void>>() {
            @Override
            public Observable<Void> call() {
                socket.emit(ClientEvent.SCROLL.name().toLowerCase(), amount);
                return Observable.empty();
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
    }

    @Override
    public Observable<List<RemiFile>> requestBaseDirectories() {
        // TODO
        return Observable.empty();
    }

    @Override
    public Observable<List<RemiFile>> requestDirectory(String dir) {
        // TODO
        return Observable.empty();
    }

    @Override
    public Observable<FileTransferResponse> transferFile(String filepath) {
        // TODO
        return Observable.empty();
    }

    @Override
    public Observable<Void> writeText(int keyCode, boolean upperCase) {
        // TODO
        return Observable.empty();
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
                connectedSubject.onNext(null);
            }

        }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                disconnectedSubject.onNext(null);
            }
        }).on(eventName(ServerEvent.DESKTOP_OS), new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                desktopOS = (String) args[0];
            }
        }).on(eventName(ServerEvent.RESP_APPS), new Emitter.Listener(){

            @Override
            public void call(Object... args) {
                Log.wtf("Remi", (String)args[0]);
                String data = (String)args[0];
                requestAppsSubject.onNext(msgDeserializer.requestAppsMessage(data));
            }
        });
    }

    @NonNull
    private String eventName(ClientEvent event) {
        return event.name().toLowerCase();
    }

    @NonNull
    private String eventName(ServerEvent event) {
        return event.name().toLowerCase();
    }

}
