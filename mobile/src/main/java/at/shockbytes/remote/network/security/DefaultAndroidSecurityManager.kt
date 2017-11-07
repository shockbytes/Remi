package at.shockbytes.remote.network.security

import android.annotation.SuppressLint
import android.content.Context
import android.util.Base64
import android.util.Log
import at.shockbytes.remote.network.message.MessageDeserializer
import at.shockbytes.remote.network.message.MessageSerializer
import at.shockbytes.remote.network.model.DesktopApp
import at.shockbytes.remote.network.model.KeyExchangeResponse
import at.shockbytes.remote.util.RemiUtils
import at.shockbytes.remote.util.RemiUtils.eventName
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import io.socket.client.IO
import io.socket.client.Socket
import okhttp3.OkHttpClient
import org.spongycastle.asn1.ASN1EncodableVector
import org.spongycastle.asn1.DERSequence
import org.spongycastle.asn1.x500.X500Name
import org.spongycastle.asn1.x509.*
import org.spongycastle.cert.X509CertificateHolder
import org.spongycastle.cert.jcajce.JcaX509CertificateConverter
import org.spongycastle.cert.jcajce.JcaX509v3CertificateBuilder
import org.spongycastle.operator.jcajce.JcaContentSignerBuilder
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.math.BigInteger
import java.security.*
import java.security.cert.Certificate
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.*
import javax.net.ssl.*


/**
 * @author Martin Macheiner
 * Date: 23.10.2017.
 */

class DefaultAndroidSecurityManager(private val context: Context,
                                    private val okHttpClient: OkHttpClient,
                                    private val msgSerializer: MessageSerializer,
                                    private val msgDeserializer: MessageDeserializer) : AndroidSecurityManager {

    enum class KeyExchangeEvent {
        DH_EXCHANGE, DESKTOP_CERTIFICATE, REJECT_CONNECTION
    }

    override val sslContext: SSLContext
        get() {
            val context = SSLContext.getInstance("TLSv1.2")
            context.init(null, trustManagers, null)
            return context
        }

    override val x509TrustManager: X509TrustManager
        get() = object : X509TrustManager {
            @Throws(CertificateException::class)
            @SuppressLint("TrustAllX509TrustManager")
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
                //(trustManagers[0] as? X509TrustManager)?.checkClientTrusted(chain, authType)
            }

            @Throws(CertificateException::class)
            @SuppressLint("TrustAllX509TrustManager")
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
                try {
                    if (chain.isNotEmpty()) {
                        chain[0].checkValidity()
                    } else {
                        (trustManagers[0] as? X509TrustManager)?.checkServerTrusted(chain, authType)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun getAcceptedIssuers(): Array<X509Certificate> {
                return arrayOf(getCertificate() as X509Certificate)
            }
        }

    private val trustManagers: Array<TrustManager>
        get() {
            val tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
            tmf.init(trustStore)
            return tmf.trustManagers
        }

    private val keyExchangePort = 9628

    private val keyExchangeSubject: PublishSubject<Boolean> = PublishSubject.create()

    private lateinit var socket: Socket

    private lateinit var keyStoreFile: File
    private lateinit var trustStoreFile: File

    private lateinit var keyStore: KeyStore
    private lateinit var trustStore: KeyStore

    private val base64Options = Base64.NO_WRAP

    init {
        initializeKeyStores()
    }

    override fun getHostnameVerifier(desktopUrl: String): HostnameVerifier {
        return HostnameVerifier { hostName, _ -> hostName == desktopUrl }
    }

    override fun generateKeys(): Observable<RemiUtils.Irrelevant> {
        return Observable.defer<RemiUtils.Irrelevant> {

            val keyGen = KeyPairGenerator.getInstance("RSA")
            val random = SecureRandom.getInstance("SHA1PRNG")
            keyGen.initialize(2048, random)

            val pair = keyGen.generateKeyPair()
            val certificate = generateCertificate("CN=" + RemiUtils.getPhoneName(),
                    pair, 1825, "SHA256WithRSAEncryption")

            val certChain = arrayOf<Certificate>(certificate)
            keyStore.setCertificateEntry(Companion.ALIAS, certificate)
            keyStore.setKeyEntry(ALIAS_PRIVATE, pair.private, CharArray(0), certChain)

            Observable.just<RemiUtils.Irrelevant>(RemiUtils.Irrelevant.INSTANCE)
        }.observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.computation())
    }

    override fun addDesktopApp(response: KeyExchangeResponse) {

        val decoded: ByteArray = Base64.decode(response.certificate, base64Options)
        val factory: CertificateFactory = CertificateFactory.getInstance("X.509")
        val clientCertificate: Certificate = factory.generateCertificate(ByteArrayInputStream(decoded))

        trustStore.setCertificateEntry(response.desktop, clientCertificate)
    }

    override fun removeVerifiedDesktopApp(desktopName: String) {
        trustStore.deleteEntry(desktopName)
    }

    override fun verifyDesktopApp(app: DesktopApp): Boolean {

        val appSignature = app.signature.toByteArray(charset("UTF8"))
        val aliases = trustStore.aliases()
        while (aliases.hasMoreElements()) {
            val alias = aliases.nextElement()

            val publicKey = trustStore.getCertificate(alias).publicKey
            val signature = Signature.getInstance("SHA1WithRSA")
            signature.initVerify(publicKey)
            signature.update(SIGNATURE_SEED)
            if (signature.verify(Base64.decode(appSignature, base64Options))) {
                return true
            }
        }
        return false
    }

    override fun getVerifiedDesktopApps(): List<String> {
        return trustStore.aliases().toList()
    }


    override fun initializeKeyExchange(app: DesktopApp): Observable<Boolean> {
        connectToDesktop(RemiUtils.createUrlFromIp(app.ip, keyExchangePort, false))
        return keyExchangeSubject
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
    }

    override fun reset(): Observable<RemiUtils.Irrelevant> {
        return Observable.defer {
            closeKeyStores()
            deleteKeyStores()
            initializeKeyStores()
            generateKeys().blockingFirst()
            Observable.just(RemiUtils.Irrelevant.INSTANCE)
        }.observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.computation())
    }

    override fun hasKeys(): Boolean {
        try {
            return getCertificate() != null && getPrivateKey() != null
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    override fun close() {
        closeKeyStores()
    }

    override fun getEncodedCertificate(): String {
        val enc: ByteArray? = getCertificate()?.encoded ?: ByteArray(0)
        return Base64.encodeToString(enc, base64Options)
    }

    @Throws(Exception::class)
    override fun initializeKeyStores() {
        keyStoreFile = File(context.filesDir, "keystore.+${KeyStore.getDefaultType()}")
        trustStoreFile = File(context.filesDir, "truststore.${KeyStore.getDefaultType()}")

        keyStore = createKeyStoreIfNecessary(keyStoreFile, KeyStore.getDefaultType())
        trustStore = createKeyStoreIfNecessary(trustStoreFile, KeyStore.getDefaultType())
    }

    @Throws(Exception::class)
    override fun closeKeyStores() {
        keyStore.store(FileOutputStream(keyStoreFile), CharArray(0))
        trustStore.store(FileOutputStream(trustStoreFile), CharArray(0))
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

    private fun connectToDesktop(desktopUrl: String) {

        val opts = IO.Options()
        opts.callFactory = okHttpClient
        opts.webSocketFactory = okHttpClient
        opts.secure = false
        opts.port = keyExchangePort

        socket = IO.socket(desktopUrl, opts)

        socket
                .on(Socket.EVENT_CONNECT) {
                    socket.emit("app_certificate",
                            msgSerializer.keyExchange(RemiUtils.getPhoneName(), getEncodedCertificate()))
                }
                .on(eventName(KeyExchangeEvent.DH_EXCHANGE)) {
                    Log.wtf("Remi", "Diffie Hellman key exchange")
                }
                .on(eventName(KeyExchangeEvent.REJECT_CONNECTION)) {
                    keyExchangeSubject.onNext(false)
                    socket.disconnect()
                }
                .on(eventName(KeyExchangeEvent.DESKTOP_CERTIFICATE)) { (args) ->

                    val resp = msgDeserializer.keyExchangeResponse(args as String)
                    addDesktopApp(resp)
                    keyExchangeSubject.onNext(true)
                    socket.disconnect()
                }

        socket.connect()

    }

    @Throws(Exception::class)
    private fun createKeyStoreIfNecessary(file: File, keystoreType: String): KeyStore {
        val keyStore = KeyStore.getInstance(keystoreType)
        if (file.exists()) {
            // if exists, load
            keyStore.load(FileInputStream(file), CharArray(0))
        } else {
            // if not exists, create
            keyStore.load(null, CharArray(0))
            keyStore.store(FileOutputStream(file), CharArray(0))
        }
        return keyStore
    }

    @Throws(Exception::class)
    private fun getPrivateKey(): PrivateKey? {
        val privateKey = keyStore.getEntry(ALIAS_PRIVATE,
                KeyStore.PasswordProtection(CharArray(0))) as KeyStore.PrivateKeyEntry
        return privateKey.privateKey
    }

    @Throws(Exception::class)
    private fun getCertificate(): java.security.cert.Certificate? {
        val cert = keyStore.getCertificate(Companion.ALIAS)
        return cert
    }

    private fun deleteKeyStores() {
        keyStoreFile.delete()
        trustStoreFile.delete()
    }

    companion object {
        private val ALIAS = "remi_desktop"
        private val ALIAS_PRIVATE = "remi_keypair"
        private val SIGNATURE_SEED = "ajklndcjasdchbaldscnahghcadmj218ioklsnop21".toByteArray(charset("UTF8"))
    }

}
