package at.shockbytes.remote.network;

import java.util.List;

import at.shockbytes.remote.network.model.ConnectionConfig;
import at.shockbytes.remote.network.model.FileTransferResponse;
import at.shockbytes.remote.network.model.RemiFile;
import at.shockbytes.remote.network.model.SlidesResponse;
import io.reactivex.Observable;

/**
 * @author Martin Macheiner
 *         Date: 26.09.2017.
 */

public interface RemiClient {

    int CONNECTION_RESULT_OK = 0;
    int CONNECTION_RESULT_ERROR_ALREADY_CONNECTED = 1;
    int CONNECTION_RESULT_ERROR_NETWORK = 2;

    enum ClientEvent {
        MOUSE_MOVE, MOUSE_CLICK_LEFT, MOUSE_CLICK_RIGHT, SCROLL,
        REQ_BASE_DIR, REQ_DIR, WRITE_TEXT, REQ_FILE_TRANSFER,
        REQ_APPS, ADD_APP, REMOVE_APP, START_APP,
        REQ_SLIDES,
        OPEN_APP_ON_DESKTOP, REQ_SLIDES_FULLSCREEN
    }

    enum ServerEvent {
        RESP_DIR, RESP_FILE_TRANSFER, RESP_SLIDES, RESP_APPS, ALREADY_CONNECTED, WELCOME
    }

    enum SlidesProduct {
        POWERPOINT, GOOGLE_SLIDES
    }

    //------------------------------- Basic operations -------------------------------

    Observable<Integer> connect(String desktopUrl);

    Observable<Object> disconnect();

    Observable<Object> listenForConnectionLoss();

    void close();

    String getDesktopOS();

    int getPort();

    ConnectionConfig.ConnectionPermissions getConnectionPermissions();

    boolean isSSLEnabled();

    //--------------------------------------------------------------------------------

    //-------------------------------- Apps operations -------------------------------

    Observable<List<String>> requestApps();

    Observable<Object> removeApp(String app);

    Observable<Object> sendAppExecutionRequest(String app);

    Observable<Object> sendAddAppRequest(String pathToApp);

    Observable<Object> sendAppOpenRequest(String pathToApp);

    //--------------------------------------------------------------------------------

    //------------------------------- Mouse operations -------------------------------

    Observable<Object> sendLeftClick();

    Observable<Object> sendRightClick();

    Observable<Object> sendMouseMove(int deltaX, int deltaY);

    Observable<Object> sendScroll(int amount);

    //--------------------------------------------------------------------------------


    //--------------------------- File and text operations ---------------------------

    Observable<List<RemiFile>> requestBaseDirectories();

    Observable<List<RemiFile>> requestDirectory(String dir);

    Observable<FileTransferResponse> transferFile(String filepath);

    Observable<Object> writeText(int keyCode, boolean isCapsLock);

    //--------------------------------------------------------------------------------

    //------------------------------- Slides operations ------------------------------

    Observable<Object> sendSlidesNextCommand();

    Observable<Object> sendSlidesPreviousCommand();

    Observable<Object> sendSlidesFullscreenCommand(SlidesProduct product);

    Observable<SlidesResponse> requestSlides(String filepath);

    //--------------------------------------------------------------------------------

}
