package at.shockbytes.remote.communication;

import java.util.List;

import io.reactivex.Observable;

/**
 * @author Martin Macheiner
 *         Date: 20.10.2017.
 */

public interface CommunicationManager {

    void setup();

    void onResume();

    void onPause();

    // ------------------ Mouse messages -------------------

    void sendMouseLeftClickMessage();

    void sendMouseRightClickMessage();

    void sendMouseScrollMessage(int amount);

    void sendMouseMoveMessage(int deltaX, int deltaY);

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
