package at.shockbytes.remote.network.message;

import android.util.Base64;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import at.shockbytes.remote.network.model.ConnectionConfig;
import at.shockbytes.remote.network.model.FileTransferResponse;
import at.shockbytes.remote.network.model.KeyExchangeResponse;
import at.shockbytes.remote.network.model.RemiFile;
import at.shockbytes.remote.network.model.SlidesResponse;

/**
 * @author Martin Macheiner
 *         Date: 28.09.2017.
 */

public class JsonMessageDeserializer implements MessageDeserializer {

    private final Gson gson;

    public JsonMessageDeserializer() {
        gson = new Gson();
    }

    @Override
    public List<String> requestAppsMessage(String msg) {
        return gson.fromJson(msg, new TypeToken<ArrayList<String>>() {
        }.getType());
    }

    @Override
    public List<RemiFile> requestFilesMessage(String msg) {
        return gson.fromJson(msg, new TypeToken<ArrayList<RemiFile>>() {
        }.getType());
    }

    @Override
    public ConnectionConfig welcomeMessage(String msg) {

        JsonObject element = new JsonParser().parse(msg).getAsJsonObject();

        JsonObject permObj = element.get("permissions").getAsJsonObject();
        ConnectionConfig.ConnectionPermissions permissions =
                new ConnectionConfig.ConnectionPermissions(permObj.get("perm_mouse").getAsBoolean(),
                        permObj.get("perm_files").getAsBoolean(),
                        permObj.get("perm_file_transfer").getAsBoolean(),
                        permObj.get("perm_apps").getAsBoolean());
        return new ConnectionConfig(element.get("operating_system").getAsString(), permissions);
    }

    @Override
    public FileTransferResponse fileTransferMessage(String msg) {

        try {

            JSONObject object = new JSONObject(msg);

            String filename = object.getString("filename");
            long size = object.getLong("size");
            int transferCode = object.getInt("transferCode");
            byte[] content = new byte[0];
            String exception = "";
            if (size > 0) {
                content = Base64.decode(object.getString("content"), Base64.DEFAULT);
            } else {
                exception = object.getString("exception");
            }
            return new FileTransferResponse(filename, content, transferCode, exception);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new FileTransferResponse();
    }

    @Override
    public SlidesResponse requestSlides(String msg) {
        return gson.fromJson(msg, SlidesResponse.class);
    }

    @Override
    public KeyExchangeResponse keyExchangeResponse(String msg) {
        JsonObject element = new JsonParser().parse(msg).getAsJsonObject();
        return new KeyExchangeResponse(element.get("desktop").getAsString(),
                element.get("certificate").getAsString());
    }
}
