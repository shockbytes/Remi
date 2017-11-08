package at.shockbytes.remote.network.message

import at.shockbytes.remote.network.model.*

/**
 * @author Martin Macheiner
 * Date: 28.09.2017.
 */

interface MessageDeserializer {

    fun requestAppsMessage(msg: String): List<String>

    fun requestFilesMessage(msg: String): List<RemiFile>

    fun welcomeMessage(msg: String): ConnectionConfig

    fun fileTransferMessage(msg: String): FileTransferResponse

    fun requestSlides(msg: String): SlidesResponse

    fun keyExchangeResponse(msg: String): KeyExchangeResponse

}
