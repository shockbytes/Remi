package at.shockbytes.remote.network.message

import android.util.Base64
import at.shockbytes.remote.network.model.*
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import java.util.*

/**
 * @author Martin Macheiner
 * Date: 28.09.2017.
 */

class JsonMessageDeserializer : MessageDeserializer {

    private val gson: Gson = Gson()

    override fun requestAppsMessage(msg: String): List<String> {
        return gson.fromJson<List<String>>(msg, object : TypeToken<ArrayList<String>>() {}.type)
    }

    override fun requestFilesMessage(msg: String): List<RemiFile> {
        return gson.fromJson<List<RemiFile>>(msg, object : TypeToken<ArrayList<RemiFile>>() {}.type)
    }

    override fun welcomeMessage(msg: String): ConnectionConfig {

        val element = JsonParser().parse(msg).asJsonObject
        val permissionObject = element.get("permissions").asJsonObject

        val permissions = ConnectionConfig.ConnectionPermissions(
                permissionObject.get("perm_mouse").asBoolean,
                permissionObject.get("perm_files").asBoolean,
                permissionObject.get("perm_file_transfer").asBoolean,
                permissionObject.get("perm_apps").asBoolean)
        return ConnectionConfig(element.get("operating_system").asString, permissions)
    }

    override fun fileTransferMessage(msg: String): FileTransferResponse {

        val element = JsonParser().parse(msg).asJsonObject

        val filename = element.get("filename").asString
        val size = element.get("size").asLong
        val transferCode = element.get("transferCode").asInt
        val exception: String? = element.get("exception")?.asString

        var content: ByteArray? = null
        if (size > 0) {
            content = Base64.decode(element.get("content").asString, Base64.DEFAULT)
        }
        return FileTransferResponse(filename, transferCode, content, exception)
    }

    override fun requestSlides(msg: String): SlidesResponse {
        return gson.fromJson(msg, SlidesResponse::class.java)
    }

    override fun keyExchangeResponse(msg: String): KeyExchangeResponse {
        val element = JsonParser().parse(msg).asJsonObject
        return KeyExchangeResponse(element.get("desktop").asString,
                element.get("certificate").asString)
    }
}
