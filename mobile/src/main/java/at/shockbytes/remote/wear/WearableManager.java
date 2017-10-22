package at.shockbytes.remote.wear;

import android.support.v4.app.FragmentActivity;

import java.util.List;

/**
 * @author Martin Macheiner
 *         Date: 20.10.2017.
 */

public interface WearableManager {

    interface OnWearableConnectedListener {

        void onWearableConnected(String wearableDevice);

        void onWearableConnectionFailed(String errorMessage);

    }

    void connect(FragmentActivity activity, OnWearableConnectedListener listener);

    void synchronizeApps(List<String> apps);

    void onPause();

}
