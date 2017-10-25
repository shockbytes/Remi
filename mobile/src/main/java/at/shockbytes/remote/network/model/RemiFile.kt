package at.shockbytes.remote.network.model

/**
 * Author:  Mescht
 * Date:    29.09.2017
 */

data class RemiFile(val name: String, val path: String,
                    val isDirectory: Boolean, val isExecutable: Boolean) {

    private var extension : String = ""

    private fun getFileExtensionFromName() {

        extension = if (isDirectory) {
            "directory"
        } else {
            val idx = name.lastIndexOf(".")
            if (idx > 0)
                name.substring(idx + 1, name.length).toLowerCase()
            else
                ""
        }
    }

    fun getExtension(): String {
        if (extension == "") {
            getFileExtensionFromName()
        }
        return extension
    }

}