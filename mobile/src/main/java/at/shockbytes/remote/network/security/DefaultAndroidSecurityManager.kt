package at.shockbytes.remote.network.security

import android.content.Context
import android.util.Base64
import android.util.Log
import at.shockbytes.remote.network.model.DesktopApp
import at.shockbytes.remote.util.RemiUtils
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.spongycastle.asn1.ASN1EncodableVector
import org.spongycastle.asn1.DERSequence
import org.spongycastle.asn1.x500.X500Name
import org.spongycastle.asn1.x509.*
import org.spongycastle.cert.X509CertificateHolder
import org.spongycastle.cert.jcajce.JcaX509CertificateConverter
import org.spongycastle.cert.jcajce.JcaX509v3CertificateBuilder
import org.spongycastle.operator.jcajce.JcaContentSignerBuilder
import java.math.BigInteger
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.*
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
            val context = SSLContext.getInstance("TLSv1.2")
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
            val cert = generateCertificate("CN=" + RemiUtils.getPhoneName(), pair, 1825, "SHA256WithRSAEncryption")
            // TODO Store cert & private key

            Log.wtf("Remi", "Certificate: " + Base64.encodeToString(cert.publicKey.encoded, Base64.DEFAULT))
            Log.wtf("Remi", "Public key:  " + (Base64.encodeToString(pair.public.encoded, Base64.DEFAULT)))

            Observable.just<RemiUtils.Irrelevant>(RemiUtils.Irrelevant.INSTANCE)
        }.observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.computation())
    }

    override fun addDesktopApp(certificate: String, encodedPublicKey: String) {
        // TODO
    }

    override fun verifyDesktopApp(app: DesktopApp) : Boolean {
        // TODO
        return false
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

    @Throws(Exception::class)
    private fun generateCertificate(distinguishedName: String, pair: KeyPair,
                                    days: Int, algorithm: String): X509Certificate {

        val issuerName = X500Name(distinguishedName)
        val serial = BigInteger.valueOf(Random().nextLong())
        val dateNotBefore = Time(Date(System.currentTimeMillis() - 1000L * 60 * 60 * 24 * 30))
        val dateNotAfter = Time(Date(System.currentTimeMillis() + (1000L * 60 * 60 * 24 * days)))

        val builder = JcaX509v3CertificateBuilder(issuerName, serial, dateNotBefore,
                dateNotAfter, issuerName, pair.public)
        builder.addExtension(Extension.basicConstraints, true, BasicConstraints(true))
        val usage = KeyUsage(KeyUsage.keyCertSign or KeyUsage.digitalSignature or
                KeyUsage.keyEncipherment or KeyUsage.dataEncipherment or KeyUsage.cRLSign)
        builder.addExtension(Extension.keyUsage, false, usage)

        val purposes = ASN1EncodableVector()
        purposes.add(KeyPurposeId.id_kp_serverAuth)
        purposes.add(KeyPurposeId.id_kp_clientAuth)
        purposes.add(KeyPurposeId.anyExtendedKeyUsage)
        builder.addExtension(Extension.extendedKeyUsage, false, DERSequence(purposes))

        val signer = JcaContentSignerBuilder(algorithm)
                .setProvider("BC").build(pair.private)
        val holder: X509CertificateHolder = builder.build(signer)

        val cert = JcaX509CertificateConverter().setProvider("BC").getCertificate(holder)
        cert.checkValidity(Date())
        cert.verify(pair.public)

        return cert
    }

}
