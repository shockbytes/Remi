package at.shockbytes.remote.network.security

import at.shockbytes.remote.network.model.DesktopApp
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


    val hostNameVerifier: HostnameVerifier

    val sslContext: SSLContext

    val x509TrustManager: X509TrustManager

    // -----------------------------------------

    fun generateKeys(): Observable<RemiUtils.Irrelevant>

    fun addDesktopApp(certificate: String, encodedPublicKey: String)

    fun verifyDesktopApp(app: DesktopApp): Boolean

    fun initializeKeyExchange(app: DesktopApp): Observable<Boolean>

    fun reset()

    fun hasKeys(): Boolean

    fun getEncodedCertificate(): String

    @Throws(Exception::class)
    fun initializeKeyStores()

    @Throws(Exception::class)
    fun closeKeyStores()

}
