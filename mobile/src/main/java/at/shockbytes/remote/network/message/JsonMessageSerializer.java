package at.shockbytes.remote.network.message;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Martin Macheiner
 *         Date: 27.09.2017.
 */

public class JsonMessageSerializer implements MessageSerializer {

    @Override
    public String mouseMoveMessage(int deltaX, int deltaY) {
        JSONObject object = new JSONObject();
        try {
            object.put("deltaX", deltaX);
            object.put("deltaY", deltaY);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object.toString();
    }

    @Override
    public String writeTextMessage(int keyCode, boolean isUpperCase) {
        JSONObject object = new JSONObject();
        try {
            object.put("keycode", keyCode);
            object.put("uppercase", isUpperCase);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object.toString();
    }
}
