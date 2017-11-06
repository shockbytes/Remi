package at.shockbytes.remote.network.message;

/**
 * @author Martin Macheiner
 *         Date: 27.09.2017.
 */

public interface MessageSerializer {

    String mouseMoveMessage(int deltaX, int deltaY);

    String writeTextMessage(int keyCode, boolean isUpperCase);

    String keyExchange(String deviceName, String certificate);
}
