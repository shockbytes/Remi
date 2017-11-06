package at.shockbytes.remote.network

import at.shockbytes.remote.network.model.*
import at.shockbytes.remote.util.RemiUtils
import io.reactivex.Observable

/**
 * @author Martin Macheiner
 * Date: 26.09.2017.
 */

interface RemiClient {

    val desktopOS: String

    val port: Int

    val connectionPermissions: ConnectionConfig.ConnectionPermissions

    val isSSLEnabled: Boolean

    enum class ClientEvent {
        MOUSE_MOVE, MOUSE_CLICK_LEFT, MOUSE_CLICK_RIGHT, SCROLL,
        REQ_BASE_DIR, REQ_DIR, WRITE_TEXT, REQ_FILE_TRANSFER,
        REQ_APPS, ADD_APP, REMOVE_APP, START_APP,
        REQ_SLIDES,
        OPEN_APP_ON_DESKTOP, REQ_SLIDES_FULLSCREEN
    }

    enum class ServerEvent {
        RESP_DIR, RESP_FILE_TRANSFER, RESP_SLIDES, RESP_APPS, ALREADY_CONNECTED, WELCOME
    }

    enum class SlidesProduct {
        POWERPOINT, GOOGLE_SLIDES
    }

    //------------------------------- Basic operations -------------------------------

    fun connect(app: DesktopApp): Observable<Int>

    fun disconnect(): Observable<Any>

    fun listenForConnectionLoss(): Observable<Any>

    fun close()

    //--------------------------------------------------------------------------------

    //-------------------------------- Apps operations -------------------------------

    fun requestApps(): Observable<List<String>>

    fun removeApp(app: String): Observable<RemiUtils.Irrelevant>

    fun sendAppExecutionRequest(app: String): Observable<RemiUtils.Irrelevant>

    fun sendAddAppRequest(pathToApp: String): Observable<RemiUtils.Irrelevant>

    fun sendAppOpenRequest(pathToApp: String): Observable<RemiUtils.Irrelevant>

    //--------------------------------------------------------------------------------

    //------------------------------- Mouse operations -------------------------------

    fun sendLeftClick(): Observable<RemiUtils.Irrelevant>

    fun sendRightClick(): Observable<RemiUtils.Irrelevant>

    fun sendMouseMove(deltaX: Int, deltaY: Int): Observable<RemiUtils.Irrelevant>

    fun sendScroll(amount: Int): Observable<RemiUtils.Irrelevant>

    //--------------------------------------------------------------------------------


    //--------------------------- File and text operations ---------------------------

    fun requestBaseDirectories(): Observable<List<RemiFile>>

    fun requestDirectory(dir: String): Observable<List<RemiFile>>

    fun transferFile(filepath: String): Observable<FileTransferResponse>

    fun writeText(keyCode: Int, isCapsLock: Boolean): Observable<RemiUtils.Irrelevant>

    //--------------------------------------------------------------------------------

    //------------------------------- Slides operations ------------------------------

    fun sendSlidesNextCommand(): Observable<RemiUtils.Irrelevant>

    fun sendSlidesPreviousCommand(): Observable<RemiUtils.Irrelevant>

    fun sendSlidesFullscreenCommand(product: SlidesProduct): Observable<RemiUtils.Irrelevant>

    fun requestSlides(filepath: String): Observable<SlidesResponse>

    companion object {

        val CONNECTION_RESULT_OK = 0
        val CONNECTION_RESULT_ERROR_ALREADY_CONNECTED = 1
        val CONNECTION_RESULT_ERROR_NETWORK = 2
    }

    //--------------------------------------------------------------------------------

}
