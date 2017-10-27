package at.shockbytes.remote.communication;

import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

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

    private GoogleApiClient apiClient;
    private Node connectedNode;

    private final Gson gson;
    private final Context context;


    private final ReplaySubject<List<String>> appsSubject;

    private Activity activity;

    @Inject
    public DefaultCommunicationManager(Context context, Gson gson) {
        this.context = context;
        this.gson = gson;
        connectedNode = null;
        appsSubject = ReplaySubject.create();
    }

    @Override
    public void connect() {

        if (apiClient == null) {
            apiClient = new GoogleApiClient.Builder(context)
                    .addApi(Wearable.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
    }

    @Override
    public void onStart(Activity activity) {
        this.activity = activity;
        apiClient.connect();
    }

    @Override
    public void onStop() {
        activity = null;
        Wearable.DataApi.removeListener(apiClient, this);
        Wearable.CapabilityApi.removeCapabilityListener(apiClient, this,
                context.getString(R.string.capability));
        apiClient.disconnect();
    }

    @Override
    public void sendMouseLeftClickMessage() {
        sendMessage("/mouse/click", "left".getBytes());
    }

    @Override
    public void sendMouseRightClickMessage() {
        sendMessage("/mouse/click", "right".getBytes());
    }

    @Override
    public Observable<List<String>> requestApps() {
        return appsSubject
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io());
    }

    @Override
    public void sendAppStartMessage(String app) {
        sendMessage("/apps/start", app.getBytes());
    }

    @Override
    public void sendSlidesNextMessage() {
        sendMessage("/slides", "next".getBytes());
    }

    @Override
    public void sendSlidesPreviousMessage() {
        sendMessage("/slides", "previous".getBytes());
    }

    @Override
    public void sendSlidesFullscreenRequest(SlidesProduct slidesProduct) {
        sendMessage("/slides/fullscreen", slidesProduct.name().getBytes());
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

        Toast.makeText(context,
                "Cannot connect to phone: " + connectionResult.getErrorCode(),
                Toast.LENGTH_LONG).show();

        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(activity, 1);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        }

        getApps(); // Try to get the cached apps
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Wearable.DataApi.addListener(apiClient, this);
        Wearable.CapabilityApi.addCapabilityListener(apiClient, this,
                context.getString(R.string.capability));
        Wearable.NodeApi.getConnectedNodes(apiClient)
                .setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                    @Override
                    public void onResult(@NonNull NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
                        if (getConnectedNodesResult.getNodes().size() > 0) {
                            connectedNode = getConnectedNodesResult.getNodes().get(0);
                        }
                    }
                });

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
                    case "/apps/list":
                        List<String> apps = gson.fromJson(data,
                                new TypeToken<List<String>>() {
                                }.getType());

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
        } else {
            connectedNode = null;
        }
    }

    private void getApps() {

        Wearable.DataApi.getDataItems(apiClient)
                .setResultCallback(new ResultCallback<DataItemBuffer>() {
                    @Override
                    public void onResult(@NonNull DataItemBuffer dataItems) {

                        for (DataItem item : dataItems) {
                            String data = new String(item.getData());
                            if (item.getUri().getPath().equals("/apps/list")) {
                                List<String> apps = gson.fromJson(data,
                                        new TypeToken<List<String>>() {
                                        }.getType());

                                appsSubject.onNext(apps);
                                break;
                            }
                        }
                        dataItems.release();
                    }
                });

    }

    private void sendMessage(@NonNull String path, @NonNull byte[] data) {
        if (apiClient.isConnected() && connectedNode != null) {
            Wearable.MessageApi.sendMessage(apiClient, connectedNode.getId(), path, data);
        }
    }

}
