package at.shockbytes.remote.network.message;

import android.util.Base64;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import at.shockbytes.remote.network.model.ConnectionConfig;
import at.shockbytes.remote.network.model.FileTransferResponse;
import at.shockbytes.remote.network.model.RemiFile;

/**
 * @author Martin Macheiner
 *         Date: 28.09.2017.
 */

public class JsonMessageDeserializer implements MessageDeserializer {

    private Gson gson;

    public JsonMessageDeserializer() {
        gson = new Gson();
    }

    @Override
    public List<String> requestAppsMessage(String msg) {
        return gson.fromJson(msg, new TypeToken<ArrayList<String>>(){}.getType());
    }

    @Override
    public List<RemiFile> requestFilesMessage(String msg) {
        return gson.fromJson(msg, new TypeToken<ArrayList<RemiFile>>(){}.getType());
    }

    @Override
    public ConnectionConfig welcomeMessage(String msg) {

        try {

            JSONObject object = new JSONObject(msg);

            // TODO Fetch permissions
            ConnectionConfig.ConnectionPermissions permissions
                    = new ConnectionConfig.ConnectionPermissions();

            return new ConnectionConfig()
                    .setDesktopOS(object.getString("operating_system"))
                    .setPermissions(permissions);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new ConnectionConfig();
    }

    @Override
    public FileTransferResponse fileTransferMessage(String msg) {

        try {

            JSONObject object = new JSONObject(msg);

            String filename = object.getString("filename");
            long size = object.getLong("size");
            byte[] content = null;
            String exception = null;
            if (size > 0) {
                content = Base64.decode(object.getString("content"), Base64.DEFAULT);
            } else {
                exception = object.getString("exception");
            }
            return new FileTransferResponse(filename, content, exception);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new FileTransferResponse();
    }
}
