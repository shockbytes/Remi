package at.shockbytes.remote.network.message;

import java.util.List;

import at.shockbytes.remote.network.model.ConnectionConfig;
import at.shockbytes.remote.network.model.FileTransferResponse;
import at.shockbytes.remote.network.model.RemiFile;
import at.shockbytes.remote.network.model.SlidesResponse;

/**
 * @author Martin Macheiner
 *         Date: 28.09.2017.
 */

public interface MessageDeserializer {

    List<String> requestAppsMessage(String msg);

    List<RemiFile> requestFilesMessage(String msg);

    ConnectionConfig welcomeMessage(String msg);

    FileTransferResponse fileTransferMessage(String msg);

    SlidesResponse requestSlides(String msg);

}
