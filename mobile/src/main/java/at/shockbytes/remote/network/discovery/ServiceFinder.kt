package at.shockbytes.remote.network.discovery

import at.shockbytes.remote.network.model.DesktopApp
import io.reactivex.Observable

/**
 * @author Martin Macheiner
 * Date: 10.10.2017.
 */

interface ServiceFinder {

    val isListening: Boolean

    fun stopListening()

    fun lookForDesktopApps(): Observable<DesktopApp>
}
