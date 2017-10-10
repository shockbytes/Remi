package at.shockbytes.remote.network.discovery;

import at.shockbytes.remote.network.model.DesktopApp;
import io.reactivex.Observable;

/**
 * @author Martin Macheiner
 *         Date: 10.10.2017.
 */

public interface ServiceFinder {

    boolean isListening();

    void stopListening();

    Observable<DesktopApp> lookForDesktopApps();
}
