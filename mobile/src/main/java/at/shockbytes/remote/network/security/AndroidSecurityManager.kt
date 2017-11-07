package at.shockbytes.remote.network.security

import at.shockbytes.remote.network.model.DesktopApp
import at.shockbytes.remote.network.model.KeyExchangeResponse
import at.shockbytes.remote.util.RemiUtils
import io.reactivex.Observable
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager

/**
 * @author Martin Macheiner
 * Date: 23.10.2017.
 */

interface AndroidSecurityManager {

    val sslContext: SSLContext

    val x509TrustManager: X509TrustManager

    // -----------------------------------------

    fun getHostnameVerifier(desktopUrl: String): HostnameVerifier

    fun generateKeys(): Observable<RemiUtils.Irrelevant>

    fun addDesktopApp(response: KeyExchangeResponse)

    fun verifyDesktopApp(app: DesktopApp): Boolean

    fun removeVerifiedDesktopApp(desktopName: String)

    fun getVerifiedDesktopApps(): List<String>

    fun initializeKeyExchange(app: DesktopApp): Observable<Boolean>

    fun reset(): Observable<RemiUtils.Irrelevant>

    fun hasKeys(): Boolean

    fun getEncodedCertificate(): String

    fun close()

    @Throws(Exception::class)
    fun initializeKeyStores()

    @Throws(Exception::class)
    fun closeKeyStores()

}
