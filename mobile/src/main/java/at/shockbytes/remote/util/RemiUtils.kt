package at.shockbytes.remote.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.support.annotation.RequiresApi
import android.support.v4.app.NotificationCompat
import android.support.v4.content.ContextCompat
import android.util.Base64
import at.shockbytes.remote.R
import at.shockbytes.remote.network.RemiClient
import at.shockbytes.remote.network.model.DesktopApp
import at.shockbytes.remote.network.model.FileTransferResponse
import at.shockbytes.remote.network.model.RemiFile
import at.shockbytes.remote.network.model.text.*
import at.shockbytes.remote.network.security.AndroidSecurityManager
import at.shockbytes.remote.network.security.DefaultAndroidSecurityManager
import at.shockbytes.remote.util.RemiUtils.FileCategory.*
import at.shockbytes.util.ResourceManager
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.CipherSuite
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import okhttp3.TlsVersion
import okhttp3.logging.HttpLoggingInterceptor
import org.apache.commons.io.IOUtils
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


@Suppress("UNUSED_EXPRESSION")
/**
 * @author Martin Macheiner
 * Date: 30.09.2017.
 */

class RemiUtils : ResourceManager() {

    enum class Irrelevant {
        INSTANCE
    }

    internal enum class FileCategory {
        FOLDER, TEXT, CODE, PDF, EXE, JAR, APP, APK, IMAGE,
        MUSIC, VIDEO, ARCHIVE, POWERPOINT, WORD, EXCEL, NA
    }

    enum class ArrowDirection {
        UP, LEFT, DOWN, RIGHT
    }

    companion object {

        val KEYCODE_BACK = 37
        val KEYCODE_NEXT = 39

        private val NOT_FILE_TRANSFER_ID = 0x2710

        private val fileExtensionMap: MutableMap<FileCategory, Int>

        init {
            fileExtensionMap = HashMap()
            fileExtensionMap.put(FOLDER, R.drawable.ic_file_folder)
            fileExtensionMap.put(CODE, R.drawable.ic_file_code)
            fileExtensionMap.put(IMAGE, R.drawable.ic_file_image)
            fileExtensionMap.put(TEXT, R.drawable.ic_file_text)
            fileExtensionMap.put(ARCHIVE, R.drawable.ic_file_archive)
            fileExtensionMap.put(PDF, R.drawable.ic_file_pdf)
            fileExtensionMap.put(EXE, R.drawable.ic_file_exe)
            fileExtensionMap.put(MUSIC, R.drawable.ic_file_music)
            fileExtensionMap.put(VIDEO, R.drawable.ic_file_video)
            fileExtensionMap.put(POWERPOINT, R.drawable.ic_file_powerpoint)
            fileExtensionMap.put(EXCEL, R.drawable.ic_file_excel)
            fileExtensionMap.put(WORD, R.drawable.ic_file_word)
            fileExtensionMap.put(APK, R.drawable.ic_file_apk)
            fileExtensionMap.put(APP, R.drawable.ic_file_app)
            fileExtensionMap.put(JAR, R.drawable.ic_file_jar)
            fileExtensionMap.put(NA, R.drawable.ic_file_unknown)
        }

        fun createUrlFromIp(ip: String, port: Int, useHttps: Boolean): String {
            val scheme = if (useHttps) "https://" else "http://"
            return scheme + ip + ":" + port
        }

        fun getDrawableResourceForFileType(file: RemiFile): Int {
            return fileExtensionMap[getFileCategory(file)] ?: R.drawable.ic_file_unknown
        }

        // On a german keyboard, let's switch Z and Y
        val keyboard: List<RemiKeyEvent>
            get() {

                val keyboard = ArrayList<RemiKeyEvent>()

                keyboard.add(StandardRemiKeyEvent("1", 49))
                keyboard.add(StandardRemiKeyEvent("2", 50))
                keyboard.add(StandardRemiKeyEvent("3", 51))
                keyboard.add(StandardRemiKeyEvent("4", 52))
                keyboard.add(StandardRemiKeyEvent("5", 53))
                keyboard.add(StandardRemiKeyEvent("6", 54))
                keyboard.add(StandardRemiKeyEvent("7", 55))
                keyboard.add(StandardRemiKeyEvent("8", 56))
                keyboard.add(StandardRemiKeyEvent("9", 57))
                keyboard.add(StandardRemiKeyEvent("0", 48))

                keyboard.add(StandardRemiKeyEvent("q", 81))
                keyboard.add(StandardRemiKeyEvent("w", 87))
                keyboard.add(StandardRemiKeyEvent("e", 69))
                keyboard.add(StandardRemiKeyEvent("r", 82))
                keyboard.add(StandardRemiKeyEvent("t", 84))
                keyboard.add(StandardRemiKeyEvent("y", 89))
                keyboard.add(StandardRemiKeyEvent("u", 85))
                keyboard.add(StandardRemiKeyEvent("i", 73))
                keyboard.add(StandardRemiKeyEvent("o", 79))
                keyboard.add(StandardRemiKeyEvent("p", 80))

                keyboard.add(StandardRemiKeyEvent("a", 65))
                keyboard.add(StandardRemiKeyEvent("s", 83))
                keyboard.add(StandardRemiKeyEvent("d", 68))
                keyboard.add(StandardRemiKeyEvent("f", 70))
                keyboard.add(StandardRemiKeyEvent("g", 71))
                keyboard.add(StandardRemiKeyEvent("h", 72))
                keyboard.add(StandardRemiKeyEvent("j", 74))
                keyboard.add(StandardRemiKeyEvent("k", 75))
                keyboard.add(StandardRemiKeyEvent("l", 76))
                keyboard.add(StandardRemiKeyEvent("/", 111))

                keyboard.add(StandardRemiKeyEvent("*", 106))
                keyboard.add(StandardRemiKeyEvent("z", 90))
                keyboard.add(StandardRemiKeyEvent("x", 88))
                keyboard.add(StandardRemiKeyEvent("c", 67))
                keyboard.add(StandardRemiKeyEvent("v", 86))
                keyboard.add(StandardRemiKeyEvent("b", 66))
                keyboard.add(StandardRemiKeyEvent("n", 78))
                keyboard.add(StandardRemiKeyEvent("m", 77))
                keyboard.add(BackspaceRemiKeyEvent("", 8))

                keyboard.add(StandardRemiKeyEvent("+", 521))
                keyboard.add(StandardRemiKeyEvent(",", 44))
                keyboard.add(SpaceRemiKeyEvent("SPACE", 32))
                keyboard.add(StandardRemiKeyEvent(".", 46))
                keyboard.add(EnterRemiKeyEvent("", 10))

                val language = java.util.Locale.getDefault().language
                if (language == "de") {
                    val eventZ = keyboard[31]
                    keyboard[31] = keyboard[15]
                    keyboard[15] = eventZ
                }
                return keyboard
            }

        fun getArrowKeyEvent(direction: ArrowDirection): RemiKeyEvent {

            return when (direction) {
                RemiUtils.ArrowDirection.UP -> StandardRemiKeyEvent("UP", 38)
                RemiUtils.ArrowDirection.LEFT -> StandardRemiKeyEvent("LEFT", 37)
                RemiUtils.ArrowDirection.DOWN -> StandardRemiKeyEvent("DOWN", 40)
                RemiUtils.ArrowDirection.RIGHT -> StandardRemiKeyEvent("RIGHT", 39)
            }
        }

        fun getConnectionErrorByResultCode(context: Context, resultCode: Int): String {

            return when (resultCode) {
                RemiClient.CONNECTION_RESULT_ERROR_ALREADY_CONNECTED ->
                    context.getString(R.string.connection_error_already_connected)

                RemiClient.CONNECTION_RESULT_ERROR_NETWORK ->
                    context.getString(R.string.connection_error_network)

                else -> context.getString(R.string.connection_error_unknown)
            }
        }

        fun getOperatingSystemIcon(os: String): Int {

            return when (os) {
                "Windows" -> R.drawable.ic_os_windows
                "Linux" -> R.drawable.ic_os_linux
                "Mac" -> R.drawable.ic_os_apple
                "NA" -> R.drawable.ic_os_na
                else -> 0
            }
        }

        @Throws(IOException::class)
        fun copyFileToDownloadsFolder(content: ByteArray?, filename: String): Observable<RemiUtils.Irrelevant> {
            return Observable.defer {
                val fileOut = File(Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), filename)
                val inStream = ByteArrayInputStream(content)
                val out = FileOutputStream(fileOut)

                IOUtils.copy(inStream, out)
                IOUtils.closeQuietly(inStream)
                IOUtils.closeQuietly(out)

                Observable.just(RemiUtils.Irrelevant.INSTANCE)
            }.observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io())
        }

        fun openOutputFolder(context: Context) {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            val uri = Uri.parse(Environment
                    .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath)
            intent.setDataAndType(uri, "*/*")
            context.startActivity(Intent.createChooser(intent, context.getString(R.string.show_download_folder)))
        }

        fun base64ToImage(base64Image: ByteArray): Bitmap {
            val decoded = Base64.decode(base64Image, Base64.DEFAULT)
            return BitmapFactory.decodeByteArray(decoded, 0, decoded.size)
        }

        fun postFileTransferNotification(context: Context, filename: String?) {

            val notificationManager = context
                    .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                setupNotificationChannelIfNecessary(notificationManager,
                        AppParams.NOTIF_CHANNEL_ID,
                        context.getString(R.string.notification_channel_transfer))
            }

            val icon = BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher)
            val notification = NotificationCompat.Builder(context, AppParams.NOTIF_CHANNEL_ID)
                    .setContentTitle(context.getString(R.string.notification_ft_title))
                    .setContentText(filename)
                    .setSmallIcon(R.drawable.ic_notification_file_transfer)
                    .setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false))
                    .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                    .build()

            notificationManager.notify(NOT_FILE_TRANSFER_ID, notification)
        }

        fun revokeFileTransferNotification(context: Context) {
            val notificationManager = context
                    .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(NOT_FILE_TRANSFER_ID)
        }

        fun getErrorTextForTransferCode(transferCode: FileTransferResponse.TransferCode): Int {

            var errorTextId = 0
            when (transferCode) {

                FileTransferResponse.TransferCode.NO_ACCESS -> errorTextId = R.string.filetransfer_error_no_access
                FileTransferResponse.TransferCode.TOO_BIG -> errorTextId = R.string.filetransfer_error_too_big
                FileTransferResponse.TransferCode.ENCODING_ERROR -> errorTextId = R.string.filetransfer_error_encoding
                FileTransferResponse.TransferCode.PARSE_ERROR -> errorTextId = R.string.filetransfer_error_unintialized
                else -> ""
            }

            return errorTextId
        }

        /**
         * This method cannot be provided by Dagger, because it has
         * to be recreated after every time a new desktop is added at runtime
         *
         * @return fresh created OkHttpClient
         */
        fun getOkHttpClient(securityManager: AndroidSecurityManager,
                            connectedApp: DesktopApp,
                            isSslEnabled: Boolean): OkHttpClient {

            val loggingInterceptor = HttpLoggingInterceptor()
            loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
            val builder = OkHttpClient.Builder()
                    .addInterceptor(loggingInterceptor)

            if (isSslEnabled) {
                val spec = ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                        .tlsVersions(TlsVersion.TLS_1_2)
                        .cipherSuites(
                                CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,
                                CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
                                CipherSuite.TLS_DHE_RSA_WITH_AES_128_GCM_SHA256)
                        .build()
                builder.connectionSpecs(listOf(spec))
                        .hostnameVerifier(securityManager.getHostnameVerifier(connectedApp.ip))
                        .sslSocketFactory(securityManager.sslContext.socketFactory,
                                securityManager.x509TrustManager)
            }

            return builder.build()
        }

        fun eventName(event: RemiClient.ClientEvent): String {
            return event.name.toLowerCase()
        }

        fun eventName(event: RemiClient.ServerEvent): String {
            return event.name.toLowerCase()
        }

        fun eventName(event: DefaultAndroidSecurityManager.KeyExchangeEvent): String {
            return event.name.toLowerCase()
        }

        val phoneName: String
            get() = Build.MODEL

        @RequiresApi(api = Build.VERSION_CODES.O)
        private fun setupNotificationChannelIfNecessary(notificationManager: NotificationManager?,
                                                        channelId: String, channel: String) {
            notificationManager?.createNotificationChannel(
                    NotificationChannel(channelId, channel, NotificationManager.IMPORTANCE_LOW))
        }

        private fun getFileCategory(file: RemiFile): FileCategory {

            if (file.isDirectory) {
                return FOLDER
            }

            val category: FileCategory
            when (file.extension) {

                "aac", "aiff", "flac", "m4p", "mp3", "wav", "wma" -> category = MUSIC
                "webm", "mkv", "flv", "ogg", "avi", "wmv",
                "mov", "mp4", "mpg", "3gp" -> category = VIDEO
                "c", "cpp", "cs", "h", "m", "java", "js", "groovy", "html", "php", "swift",
                "playground", "py", "sh", "rb", "asm", "kt", "gradle", "json", "xml" -> category = CODE
                "zip", "tar", "iso", "bz2", "gz", "7z", "s7z", "dmg", "rar" -> category = ARCHIVE
                "jpg", "jpeg", "tiff", "gif", "bmp", "png" -> category = IMAGE
                "text", "txt" -> category = TEXT
                "jar" -> category = JAR
                "bat", "exe" -> category = EXE
                "pdf" -> category = PDF
                "app" -> category = APP
                "apk" -> category = APK
                "pptx", "ppt" -> category = POWERPOINT
                "xlsm", "xlsx", "xls" -> category = EXCEL
                "odt", "docx", "doc" -> category = WORD

                else -> category = NA
            }
            return category
        }
    }
}
