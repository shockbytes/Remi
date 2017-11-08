package at.shockbytes.remote.network.message

/**
 * @author Martin Macheiner
 * Date: 27.09.2017.
 */

interface MessageSerializer {

    fun mouseMoveMessage(deltaX: Int, deltaY: Int): String

    fun writeTextMessage(keyCode: Int, isUpperCase: Boolean): String

    fun keyExchange(deviceName: String, certificate: String): String
}
