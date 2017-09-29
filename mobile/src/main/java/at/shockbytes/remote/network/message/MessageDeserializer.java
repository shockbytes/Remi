package at.shockbytes.remote.network.message;

import java.util.List;

/**
 * @author Martin Macheiner
 *         Date: 28.09.2017.
 */

public interface MessageDeserializer {

    List<String> requestAppsMessage(String msg);

}
