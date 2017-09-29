package at.shockbytes.remote.network.message;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

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
}
