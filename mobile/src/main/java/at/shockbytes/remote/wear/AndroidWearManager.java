package at.shockbytes.remote.wear;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.gson.Gson;

import java.util.List;

import javax.inject.Inject;

import at.shockbytes.remote.R;
import at.shockbytes.remote.network.RemiClient;
import io.reactivex.functions.Consumer;

/**
 * @author Martin Macheiner
 *         Date: 18.03.2017.
 */

public class AndroidWearManager implements WearableManager,
        GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks,
        MessageApi.MessageListener {

    private GoogleApiClient apiClient;
    private final Gson gson;
    private final Context context;
    private final RemiClient client;

    private OnWearableConnectedListener connectionListener;

    @Inject
    public AndroidWearManager(Context context, RemiClient client, Gson gson) {
        this.context = context;
        this.client = client;
        this.gson = gson;
    }

    @Override
    public void connect(FragmentActivity activity, OnWearableConnectedListener connectionListener) {
        this.connectionListener = connectionListener;

        if (apiClient == null) {
            apiClient = new GoogleApiClient.Builder(context)
                    .addApi(Wearable.API)
                    .enableAutoManage(activity, 1, this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
    }

    @Override
    public void synchronize() {
        client.requestApps().subscribe(new Consumer<List<String>>() {
            @Override
            public void accept(List<String> workouts) {
                synchronizeApps(workouts);
            }
        });
    }

    @Override
    public void synchronizeApps(List<String> apps) {

        PutDataRequest request = PutDataRequest.create("/apps/list");
        String serializedWorkouts = gson.toJson(apps);
        request.setData(serializedWorkouts.getBytes());

        Wearable.DataApi.putDataItem(apiClient, request);
    }

    @Override
    public void onStop() {
        Wearable.MessageApi.removeListener(apiClient, this);
        Wearable.CapabilityApi.removeLocalCapability(apiClient, context.getString(R.string.capability));
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

        Log.wtf("Remi", "onConnectionFailed: " + connectionResult.getErrorMessage() + " / " +
                connectionResult.getErrorCode());
        connectionListener.onWearableConnectionFailed(connectionResult.getErrorMessage());
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Wearable.MessageApi.addListener(apiClient, this);
        Wearable.CapabilityApi.addLocalCapability(apiClient, context.getString(R.string.capability));
        synchronize();
        connectionListener.onWearableConnected(""); // TODO Find right name of connected wearable
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {

        String path = messageEvent.getPath();
        String data = new String(messageEvent.getData());
        if (path.equals("/slides")) {
            handleSlidesMessage(data);
        } else if (path.equals("/apps/start")) {
            client.sendAppExecutionRequest(data).subscribe();
        } else if (path.equals("/mouse/click")) {
            handleMouseClickMessage(data);
        }
    }

    private void handleSlidesMessage(String data) {
        if (data.equals("next")) {
            client.sendSlidesNextCommand().subscribe();
        } else if (data.equals("previous")) {
            client.sendSlidesPreviousCommand().subscribe();
        }
    }

    private void handleMouseClickMessage(String data) {
        if (data.equals("left")) {
            client.sendLeftClick().subscribe();
        } else if (data.equals("right")) {
            client.sendRightClick().subscribe();
        }
    }

}
