package at.shockbytes.remote.network.model

import java.util.*

/**
 * Author:  Mescht
 * Date:    16.10.2017
 */

data class SlidesResponse(val name : String = "", val errorCode : Int,
                          val slides : List<SlidesEntry>, val slideAmount : Int) {

    data class SlidesEntry(val base64Image: ByteArray = ByteArray(0),
                           val notes: String = "", val slideNumber : Int) {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as SlidesEntry

            if (!Arrays.equals(base64Image, other.base64Image)) return false
            if (notes != other.notes) return false
            if (slideNumber != other.slideNumber) return false

            return true
        }

        override fun hashCode(): Int {
            var result = Arrays.hashCode(base64Image)
            result = 31 * result + notes.hashCode()
            result = 31 * result + slideNumber
            return result
        }
    }
}