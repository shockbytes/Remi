package at.shockbytes.remote.wear;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

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
    private Gson gson;
    private Context context;
    private RemiClient client;

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
    public void synchronizeApps(List<String> apps) {

        PutDataRequest request = PutDataRequest.create("/apps");
        String serializedWorkouts = gson.toJson(apps);
        request.setData(serializedWorkouts.getBytes());

        Wearable.DataApi.putDataItem(apiClient, request);
    }

    @Override
    public void onPause() {
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
        connectionListener.onWearableConnected(null);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {

        Log.wtf("Remi", "information received! + " + messageEvent.toString());

        String text = "";

        String path = messageEvent.getPath();
        switch (path) {

            case "/exec_apps":
                text = "Start: " + new String(messageEvent.getData());
                break;

            case "/hello":

                text = "Hello";
                break;

        }
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }

    private void synchronize() {
        client.requestApps().subscribe(new Consumer<List<String>>() {
            @Override
            public void accept(List<String> workouts) {
                synchronizeApps(workouts);
            }
        });
    }

}
