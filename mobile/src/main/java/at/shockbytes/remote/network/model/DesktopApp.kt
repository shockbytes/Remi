package at.shockbytes.remote.network.model

/**
 * @author Martin Macheiner
 * Date: 25.10.2017.
 */

data class DesktopApp(val name : String, val ip : String, val os : String = "NA",
                      val signature: String, val publicKey : String)