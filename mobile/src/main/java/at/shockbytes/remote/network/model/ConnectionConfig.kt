package at.shockbytes.remote.network.model

/**
 * @author Martin Macheiner
 * Date: 09.10.2017.
 */

data class ConnectionConfig(var desktopOS: String = "",
                            var permissions: ConnectionPermissions = ConnectionPermissions()) {

    data class ConnectionPermissions(var hasMousePermission: Boolean = true,
                                     var hasAppsPermission: Boolean = true,
                                     var hasFilesPermission: Boolean = true,
                                     var hasFileTransferPermission: Boolean = true)

}
