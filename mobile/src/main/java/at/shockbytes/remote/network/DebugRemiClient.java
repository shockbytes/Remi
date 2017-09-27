package at.shockbytes.remote.network;

import java.util.Arrays;
import java.util.List;

import at.shockbytes.remote.network.model.FileTransferResponse;
import at.shockbytes.remote.network.model.RemoteFile;
import rx.Observable;

/**
 * @author Martin Macheiner
 *         Date: 27.09.2017.
 */

public class DebugRemiClient implements RemiClient {
    @Override
    public Observable<Void> connect(String serverUrl) {
        return Observable.empty();
    }

    @Override
    public Observable<Void> disconnect() {
        return Observable.empty();
    }

    @Override
    public void close() {

    }

    public String getDesktopOS() {
        return "Debug";
    }

    @Override
    public Observable<List<String>> requestApps() {
        return Observable.just(Arrays.asList("Android Studio", "Stronghold Crusader", "Photoshop"));
    }

    @Override
    public Observable<Void> removeApp(String app) {
        return Observable.empty();
    }

    @Override
    public Observable<Void> sendAppExecutionRequest(String app) {
        return Observable.empty();
    }

    @Override
    public Observable<Void> sendAddAppRequest(String pathToApp) {
        return Observable.empty();
    }

    @Override
    public Observable<Void> sendLeftClick() {
        return Observable.empty();
    }

    @Override
    public Observable<Void> sendRightClick() {
        return Observable.empty();
    }

    @Override
    public Observable<Void> sendMouseMove(int deltaX, int deltaY) {
        return Observable.empty();
    }

    @Override
    public Observable<Void> sendScroll(int amount) {
        return Observable.empty();
    }

    @Override
    public Observable<List<RemoteFile>> requestBaseDirectories() {
        return Observable.empty();
    }

    @Override
    public Observable<List<RemoteFile>> requestDirectory(String dir) {
        return Observable.empty();
    }

    @Override
    public Observable<FileTransferResponse> transferFile(String filepath, String filename) {
        return Observable.empty();
    }

    @Override
    public Observable<Void> writeText(int keyCode, boolean upperCase) {
        return Observable.empty();
    }
}
