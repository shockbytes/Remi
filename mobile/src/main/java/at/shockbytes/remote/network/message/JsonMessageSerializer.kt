package at.shockbytes.remote.network.message

import com.google.gson.JsonObject

/**
 * @author Martin Macheiner
 * Date: 27.09.2017.
 */

class JsonMessageSerializer : MessageSerializer {

    override fun mouseMoveMessage(deltaX: Int, deltaY: Int): String {
        val element = JsonObject()
        element.addProperty("deltaX", deltaX)
        element.addProperty("deltaY", deltaY)
        return element.toString()
    }

    override fun writeTextMessage(keyCode: Int, isUpperCase: Boolean): String {
        val element = JsonObject()
        element.addProperty("keycode", keyCode)
        element.addProperty("uppercase", isUpperCase)
        return element.toString()
    }

    override fun keyExchange(deviceName: String, certificate: String): String {
        val element = JsonObject()
        element.addProperty("device", deviceName)
        element.addProperty("certificate", certificate)
        return element.toString()
    }
}
