package at.shockbytes.remote.network.model

import android.annotation.SuppressLint
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * @author Martin Macheiner
 * Date: 25.10.2017.
 */

@SuppressLint("ParcelCreator")
@Parcelize
data class DesktopApp(val name : String, val ip : String, val os : String = "NA",
                      val signature: String, var isTrusted : Boolean = false) : Parcelable