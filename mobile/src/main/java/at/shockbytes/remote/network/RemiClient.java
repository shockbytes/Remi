package at.shockbytes.remote.network;

import java.util.List;

import at.shockbytes.remote.network.model.FileTransferResponse;
import at.shockbytes.remote.network.model.RemoteFile;
import rx.Observable;

/**
 * @author Martin Macheiner
 *         Date: 26.09.2017.
 */

public interface RemiClient {

    enum ClientEvent {
        MOUSE_MOVE, MOUSE_CLICK_LEFT, MOUSE_CLICK_RIGHT, SCROLL,
        REQ_DIR, WRITE_TEXT, REQ_FILE_TRANSFER,
        REQ_APPS, ADD_APP, REMOVE_APP, START_APP,
        REQ_SLIDES
    }

    enum ServerEvent {
        RESP_DIR, RESP_FILE_TRANSFER, RESP_SLIDES
    }

    //------------------------------- Basic operations -------------------------------

    Observable<Void> connect(String serverUrl);

    Observable<Void> disconnect();

    void close();

    String getDesktopOS();

    //--------------------------------------------------------------------------------

    //-------------------------------- Apps operations -------------------------------

    Observable<List<String>> requestApps();

    Observable<Void> removeApp(String app);

    Observable<Void> sendAppExecutionRequest(String app);

    Observable<Void> sendAddAppRequest(String pathToApp);

    //--------------------------------------------------------------------------------

    //------------------------------- Mouse operations -------------------------------

    Observable<Void> sendLeftClick();

    Observable<Void> sendRightClick();

    Observable<Void> sendMouseMove(int deltaX, int deltaY);

    Observable<Void> sendScroll(int amount);

    //--------------------------------------------------------------------------------


    //--------------------------- File and text operations ---------------------------

    Observable<List<RemoteFile>> requestBaseDirectories();

    Observable<List<RemoteFile>> requestDirectory(String dir);

    Observable<FileTransferResponse> transferFile(String filepath, String filename);

    Observable<Void> writeText(int keyCode, boolean upperCase);

    //--------------------------------------------------------------------------------

}
