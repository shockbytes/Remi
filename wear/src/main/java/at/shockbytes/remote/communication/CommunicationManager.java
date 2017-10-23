package at.shockbytes.remote.communication;

import android.app.Activity;

import java.util.List;

import io.reactivex.Observable;

/**
 * @author Martin Macheiner
 *         Date: 20.10.2017.
 */

public interface CommunicationManager {

    void connect();

    void onStart(Activity activity);

    void onStop();

    // ------------------ Mouse messages -------------------

    void sendMouseLeftClickMessage();

    void sendMouseRightClickMessage();

    // -----------------------------------------------------

    // ------------------- Apps messages -------------------

    Observable<List<String>> requestApps();

    void sendAppStartMessage(String app);

    // -----------------------------------------------------

    // ------------------ Slides messages ------------------

    void sendSlidesNextMessage();

    void sendSlidesPreviousMessage();

    // -----------------------------------------------------

}
