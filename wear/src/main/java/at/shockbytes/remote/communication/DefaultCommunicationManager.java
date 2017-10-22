package at.shockbytes.remote.communication;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.CapabilityApi;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataItemBuffer;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import at.shockbytes.remote.R;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.ReplaySubject;

/**
 * @author Martin Macheiner
 *         Date: 20.10.2017.
 */

public class DefaultCommunicationManager implements CommunicationManager,
        GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks,
        DataApi.DataListener, CapabilityApi.CapabilityListener {

    private Context context;
    private GoogleApiClient apiClient;
    private Gson gson;
    private Node connectedNode;
    private List<String> cachedApps;

    private ReplaySubject<List<String>> appsSubject;

    @Inject
    public DefaultCommunicationManager(Context context, Gson gson) {
        this.context = context;
        this.gson = gson;
        connectedNode = null;
        appsSubject = ReplaySubject.create();
    }

    @Override
    public void setup() {

        if (apiClient == null) {
            apiClient = new GoogleApiClient.Builder(context)
                    .addApi(Wearable.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
    }

    @Override
    public void onResume() {
        apiClient.connect();
    }

    @Override
    public void onPause() {
        Wearable.DataApi.removeListener(apiClient, this);
        Wearable.CapabilityApi.removeCapabilityListener(apiClient, this,
                context.getString(R.string.capability));
        apiClient.disconnect();
    }

    @Override
    public void sendMouseLeftClickMessage() {
        // TODO
    }

    @Override
    public void sendMouseRightClickMessage() {
        // TODO
    }

    @Override
    public void sendMouseScrollMessage(int amount) {
        // TODO
    }

    @Override
    public void sendMouseMoveMessage(int deltaX, int deltaY) {
        // TODO
    }

    @Override
    public Observable<List<String>> requestApps() {
        return appsSubject
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io());
    }

    @Override
    public void sendAppStartMessage(String app) {
        // TODO
    }

    @Override
    public void sendSlidesNextMessage() {
        // TODO
    }

    @Override
    public void sendSlidesPreviousMessage() {
        // TODO
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

        Toast.makeText(context,
                "Cannot connect to handheld: " + connectionResult.getErrorMessage(),
                Toast.LENGTH_LONG).show();
        getApps(); // Try to get the cached apps
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Wearable.DataApi.addListener(apiClient, this);
        Wearable.CapabilityApi.addCapabilityListener(apiClient, this,
                context.getString(R.string.capability));
        getApps();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {

        for (DataEvent event : dataEventBuffer) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                DataItem item = event.getDataItem();
                String path = item.getUri().getPath();
                String data = new String(item.getData());
                switch (path) {
                    case "/apps":
                        List<String> apps = gson.fromJson(data,
                                new TypeToken<List<String>>() {
                                }.getType());

                        cachedApps = apps;
                        appsSubject.onNext(apps);
                        break;
                }
            }
        }
    }

    @Override
    public void onCapabilityChanged(CapabilityInfo info) {

        if (info.getNodes().size() > 0) {
            connectedNode = info.getNodes().iterator().next(); // Assume first node is handheld
            Toast.makeText(context, connectedNode.getDisplayName(), Toast.LENGTH_SHORT).show();
            synchronizeData();
        } else {
            connectedNode = null;
        }
    }

    public ArrayList<String> getCachedApps() {

        if (cachedApps == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(cachedApps);
    }

    private void getApps() {

        Wearable.DataApi.getDataItems(apiClient)
                .setResultCallback(new ResultCallback<DataItemBuffer>() {
                    @Override
                    public void onResult(@NonNull DataItemBuffer dataItems) {

                        for (DataItem item : dataItems) {
                            String data = new String(item.getData());
                            if (item.getUri().getPath().equals("/apps")) {
                                List<String> apps = gson.fromJson(data,
                                        new TypeToken<List<String>>() {
                                        }.getType());

                                cachedApps = apps;
                                appsSubject.onNext(apps);
                                break;
                            }
                        }
                        dataItems.release();
                    }
                });

    }

    private void synchronizeData() {

        byte[] data = null; // TODO
        Log.wtf("Corey", new String(data));
        Log.wtf("Corey", connectedNode.toString());
        Wearable.MessageApi.sendMessage(apiClient, connectedNode.getId(),
                "/wear_information", data);
    }

}
