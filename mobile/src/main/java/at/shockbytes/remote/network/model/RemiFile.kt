package at.shockbytes.remote.network.model

/**
 * Author:  Mescht
 * Date:    29.09.2017
 */

data class RemiFile(val name: String, val path: String,
                    val isDirectory: Boolean, val isExecutable: Boolean) {

    val extension: String
        get() {
            var ext = ""
            if (isDirectory) {
                ext = "directory"
            } else {
                val idx = name.lastIndexOf(".")
                if (idx > 0) {
                    ext = name.substring(idx + 1, name.length).toLowerCase()
                }
            }
            return ext
        }

}