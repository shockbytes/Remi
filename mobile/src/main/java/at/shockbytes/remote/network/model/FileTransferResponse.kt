package at.shockbytes.remote.network.model

import java.util.*

/**
 * @author Martin Macheiner
 * Date: 26.09.2017.
 */

data class FileTransferResponse (val filename: String = "",
                                 private val transferCode: Int = TransferCode.PARSE_ERROR.ordinal,
                                 val content: ByteArray?,
                                 private val exception: String?) {

    enum class TransferCode {
        OKAY, NO_ACCESS, TOO_BIG, ENCODING_ERROR, PARSE_ERROR
    }

    val isEmpty: Boolean
        get() = content?.isEmpty() ?: false

    fun getTransferCodeAsEnum() : TransferCode {
        return TransferCode.values()[transferCode]
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FileTransferResponse

        if (filename != other.filename) return false
        if (!Arrays.equals(content, other.content)) return false
        if (transferCode != other.transferCode) return false
        if (exception != other.exception) return false

        return true
    }

    override fun hashCode(): Int {
        var result = filename.hashCode()
        result = 31 * result + Arrays.hashCode(content)
        result = 31 * result + transferCode
        result = 31 * result + (exception?.hashCode() ?: 0)
        return result
    }

}
