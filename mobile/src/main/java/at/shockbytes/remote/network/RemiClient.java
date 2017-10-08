package at.shockbytes.remote.network;

import java.util.List;

import at.shockbytes.remote.network.model.FileTransferResponse;
import at.shockbytes.remote.network.model.RemiFile;
import rx.Observable;

/**
 * @author Martin Macheiner
 *         Date: 26.09.2017.
 */

public interface RemiClient {

    enum ClientEvent {
        MOUSE_MOVE, MOUSE_CLICK_LEFT, MOUSE_CLICK_RIGHT, SCROLL,
        REQ_BASE_DIR, REQ_DIR, WRITE_TEXT, REQ_FILE_TRANSFER,
        REQ_APPS, ADD_APP, REMOVE_APP, START_APP,
        REQ_SLIDES
    }

    enum ServerEvent {
        RESP_DIR, RESP_FILE_TRANSFER, RESP_SLIDES, RESP_APPS, DESKTOP_OS
    }

    //------------------------------- Basic operations -------------------------------

    Observable<Void> connect(String serverUrl);

    Observable<Void> disconnect();

    Observable<Void> listenForConnectionLoss();

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

    Observable<List<RemiFile>> requestBaseDirectories();

    Observable<List<RemiFile>> requestDirectory(String dir);

    Observable<FileTransferResponse> transferFile(String filepath);

    Observable<Void> writeText(int keyCode, boolean upperCase);

    //--------------------------------------------------------------------------------

}
