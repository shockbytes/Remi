package at.shockbytes.remote.network;

import java.util.Arrays;
import java.util.List;

import at.shockbytes.remote.network.model.FileTransferResponse;
import at.shockbytes.remote.network.model.RemiFile;
import io.reactivex.Observable;

/**
 * @author Martin Macheiner
 *         Date: 27.09.2017.
 */

public class DebugRemiClient implements RemiClient {

    @Override
    public Observable<Object> connect(String serverUrl) {
        return Observable.empty();
    }

    @Override
    public Observable<Object> disconnect() {
        return Observable.empty();
    }

    @Override
    public Observable<Object> listenForConnectionLoss() {
        return Observable.empty();
    }

    @Override
    public void close() {
        // Do nothing
    }

    public String getDesktopOS() {
        return "Debug";
    }

    @Override
    public Observable<List<String>> requestApps() {
        return Observable.just(Arrays.asList("Android Studio", "Stronghold Crusader", "Photoshop"));
    }

    @Override
    public Observable<Object> removeApp(String app) {
        return Observable.empty();
    }

    @Override
    public Observable<Object> sendAppExecutionRequest(String app) {
        return Observable.empty();
    }

    @Override
    public Observable<Object> sendAddAppRequest(String pathToApp) {
        return Observable.empty();
    }

    @Override
    public Observable<Object> sendLeftClick() {
        return Observable.empty();
    }

    @Override
    public Observable<Object> sendRightClick() {
        return Observable.empty();
    }

    @Override
    public Observable<Object> sendMouseMove(int deltaX, int deltaY) {
        return Observable.empty();
    }

    @Override
    public Observable<Object> sendScroll(int amount) {
        return Observable.empty();
    }

    @Override
    public Observable<List<RemiFile>> requestBaseDirectories() {
        return Observable.empty();
    }

    @Override
    public Observable<List<RemiFile>> requestDirectory(String dir) {
        return Observable.empty();
    }

    @Override
    public Observable<FileTransferResponse> transferFile(String filepath) {
        return Observable.empty();
    }

    @Override
    public Observable<Object> writeText(int keyCode, boolean upperCase) {
        return Observable.empty();
    }
}
