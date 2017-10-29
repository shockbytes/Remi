package at.shockbytes.remote.network.security

import android.content.Context
import at.shockbytes.remote.util.RemiUtils
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.security.KeyPairGenerator
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.*

/**
 * @author Martin Macheiner
 * Date: 23.10.2017.
 */

class DefaultAndroidSecurityManager(private val context: Context) : AndroidSecurityManager {

    override
    val hostNameVerifier: HostnameVerifier
        get() = HostnameVerifier { s, sslSession -> true }// TODO

    override val sslContext: SSLContext
        get() {
            val context = SSLContext.getInstance("TLSv2")
            context.init(keyManagers, trustManagers, null)
            return context
        }

    override
    val x509TrustManager: X509TrustManager
        get() = object : X509TrustManager {
            @Throws(CertificateException::class)
            override fun checkClientTrusted(x509Certificates: Array<X509Certificate>, s: String) {
                // TODO
            }

            @Throws(CertificateException::class)
            override fun checkServerTrusted(x509Certificates: Array<X509Certificate>, s: String) {
                // TODO
            }

            override fun getAcceptedIssuers(): Array<X509Certificate> {
                return arrayOf()
            }
        }

    private val keyManagers: Array<KeyManager>
        get() = arrayOf() // TODO

    private val trustManagers: Array<TrustManager>
        get() = arrayOf() // TODO

    init {
        initializeKeyStores()
    }

    override fun generateKeys(): Observable<RemiUtils.Irrelevant> {
        return Observable.defer<RemiUtils.Irrelevant> {

            val keyGen = KeyPairGenerator.getInstance("RSA")
            val random = SecureRandom.getInstance("SHA1PRNG")
            keyGen.initialize(2048, random)
            val pair = keyGen.generateKeyPair()
            val priv = pair.private
            val pub = pair.public
            // TODO Store key

            Observable.just<RemiUtils.Irrelevant>(RemiUtils.Irrelevant.INSTANCE)
        }.observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.computation())
    }

    override fun addDesktopApp(certificate: String, encodedPublicKey: String) {
        // TODO
    }

    override fun identifyDesktopApp(signature: String) {
        // TODO
    }

    override fun reset() {
        // TODO
    }

    override fun hasKeys(): Boolean {
        // TODO
        return false
    }

    @Throws(Exception::class)
    override fun initializeKeyStores() {
        // TODO
    }

    @Throws(Exception::class)
    override fun closeKeyStores() {
        // TODO
    }
}
