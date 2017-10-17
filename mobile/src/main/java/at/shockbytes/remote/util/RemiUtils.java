package at.shockbytes.remote.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Base64;

import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import at.shockbytes.remote.R;
import at.shockbytes.remote.network.RemiClient;
import at.shockbytes.remote.network.model.RemiFile;
import at.shockbytes.remote.network.model.text.BackspaceRemiKeyEvent;
import at.shockbytes.remote.network.model.text.EnterRemiKeyEvent;
import at.shockbytes.remote.network.model.text.RemiKeyEvent;
import at.shockbytes.remote.network.model.text.SpaceRemiKeyEvent;
import at.shockbytes.remote.network.model.text.StandardRemiKeyEvent;
import at.shockbytes.util.ResourceManager;

import static at.shockbytes.remote.util.RemiUtils.FileCategory.APK;
import static at.shockbytes.remote.util.RemiUtils.FileCategory.APP;
import static at.shockbytes.remote.util.RemiUtils.FileCategory.ARCHIVE;
import static at.shockbytes.remote.util.RemiUtils.FileCategory.CODE;
import static at.shockbytes.remote.util.RemiUtils.FileCategory.EXCEL;
import static at.shockbytes.remote.util.RemiUtils.FileCategory.EXE;
import static at.shockbytes.remote.util.RemiUtils.FileCategory.FOLDER;
import static at.shockbytes.remote.util.RemiUtils.FileCategory.IMAGE;
import static at.shockbytes.remote.util.RemiUtils.FileCategory.JAR;
import static at.shockbytes.remote.util.RemiUtils.FileCategory.MUSIC;
import static at.shockbytes.remote.util.RemiUtils.FileCategory.NA;
import static at.shockbytes.remote.util.RemiUtils.FileCategory.PDF;
import static at.shockbytes.remote.util.RemiUtils.FileCategory.POWERPOINT;
import static at.shockbytes.remote.util.RemiUtils.FileCategory.TEXT;
import static at.shockbytes.remote.util.RemiUtils.FileCategory.VIDEO;
import static at.shockbytes.remote.util.RemiUtils.FileCategory.WORD;


/**
 * @author Martin Macheiner
 *         Date: 30.09.2017.
 */

public class RemiUtils extends ResourceManager {

    public enum Irrelevant {INSTANCE}

    enum FileCategory {
        FOLDER, TEXT, CODE, PDF, EXE, JAR, APP, APK, IMAGE,
        MUSIC, VIDEO, ARCHIVE, POWERPOINT, WORD, EXCEL, NA
    }

    public enum ArrowDirection {
        UP, LEFT, DOWN, RIGHT
    }

    public static final int KEYCODE_BACK = 37;
    public static final int KEYCODE_NEXT = 39;

    private static Map<FileCategory, Integer> fileExtensionMap;

    static {
        fileExtensionMap = new HashMap<>();
        fileExtensionMap.put(FOLDER, R.drawable.ic_file_folder);
        fileExtensionMap.put(CODE, R.drawable.ic_file_code);
        fileExtensionMap.put(IMAGE, R.drawable.ic_file_image);
        fileExtensionMap.put(TEXT, R.drawable.ic_file_text);
        fileExtensionMap.put(ARCHIVE, R.drawable.ic_file_archive);
        fileExtensionMap.put(PDF, R.drawable.ic_file_pdf);
        fileExtensionMap.put(EXE, R.drawable.ic_file_exe);
        fileExtensionMap.put(MUSIC, R.drawable.ic_file_music);
        fileExtensionMap.put(VIDEO, R.drawable.ic_file_video);
        fileExtensionMap.put(POWERPOINT, R.drawable.ic_file_powerpoint);
        fileExtensionMap.put(EXCEL, R.drawable.ic_file_excel);
        fileExtensionMap.put(WORD, R.drawable.ic_file_word);
        fileExtensionMap.put(APK, R.drawable.ic_file_apk);
        fileExtensionMap.put(APP, R.drawable.ic_file_app);
        fileExtensionMap.put(JAR, R.drawable.ic_file_jar);
        fileExtensionMap.put(NA, R.drawable.ic_file_unknown);
    }

    public static String createUrlFromIp(String ip, int port, boolean useHttps) {
        String scheme = useHttps ? "https://" : "http://";
        return scheme + ip + ":" + port;
    }

    public static int getDrawableResourceForFileType(RemiFile file) {
        return fileExtensionMap.get(getFileCategory(file));
    }

    private static FileCategory getFileCategory(RemiFile file) {

        if (file.isDirectory()) {
            return FOLDER;
        }

        FileCategory category;
        switch (file.getExtension()) {

            case "aac":
            case "aiff":
            case "flac":
            case "m4p":
            case "mp3":
            case "wav":
            case "wma":
                category = MUSIC;
                break;

            case "webm":
            case "mkv":
            case "flv":
            case "ogg":
            case "avi":
            case "wmv":
            case "mov":
            case "mp4":
            case "mpg":
            case "3gp":
                category = VIDEO;
                break;

            case "c":
            case "cpp":
            case "cs":
            case "h":
            case "m":
            case "java":
            case "js":
            case "groovy":
            case "html":
            case "php":
            case "swift":
            case "playground":
            case "py":
            case "sh":
            case "rb":
            case "asm":
            case "kt":
            case "gradle":
            case "json":
            case "xml":
                category = CODE;
                break;

            case "zip":
            case "tar":
            case "iso":
            case "bz2":
            case "gz":
            case "7z":
            case "s7z":
            case "dmg":
            case "rar":
                category = ARCHIVE;
                break;

            case "jpg":
            case "jpeg":
            case "tiff":
            case "gif":
            case "bmp":
            case "png":
                category = IMAGE;
                break;

            case "text":
            case "txt":
                category = TEXT;
                break;

            case "jar":
                category = JAR;
                break;

            case "bat":
            case "exe":
                category = EXE;
                break;

            case "pdf":
                category = PDF;
                break;

            case "app":
                category = APP;
                break;

            case "apk":
                category = APK;
                break;

            case "pptx":
            case "ppt":
                category = POWERPOINT;
                break;

            case "xlsm":
            case "xlsx":
            case "xls":
                category = EXCEL;
                break;

            case "odt":
            case "docx":
            case "doc":
                category = WORD;
                break;

            default:
                category = NA;
                break;

        }
        return category;
    }

    public static List<RemiKeyEvent> getKeyboard() {

        List<RemiKeyEvent> keyboard = new ArrayList<>();

        keyboard.add(new StandardRemiKeyEvent("1", 49));
        keyboard.add(new StandardRemiKeyEvent("2", 50));
        keyboard.add(new StandardRemiKeyEvent("3", 51));
        keyboard.add(new StandardRemiKeyEvent("4", 52));
        keyboard.add(new StandardRemiKeyEvent("5", 53));
        keyboard.add(new StandardRemiKeyEvent("6", 54));
        keyboard.add(new StandardRemiKeyEvent("7", 55));
        keyboard.add(new StandardRemiKeyEvent("8", 56));
        keyboard.add(new StandardRemiKeyEvent("9", 57));
        keyboard.add(new StandardRemiKeyEvent("0", 48));

        keyboard.add(new StandardRemiKeyEvent("q", 81));
        keyboard.add(new StandardRemiKeyEvent("w", 87));
        keyboard.add(new StandardRemiKeyEvent("e", 69));
        keyboard.add(new StandardRemiKeyEvent("r", 82));
        keyboard.add(new StandardRemiKeyEvent("t", 84));
        keyboard.add(new StandardRemiKeyEvent("y", 89));
        keyboard.add(new StandardRemiKeyEvent("u", 85));
        keyboard.add(new StandardRemiKeyEvent("i", 73));
        keyboard.add(new StandardRemiKeyEvent("o", 79));
        keyboard.add(new StandardRemiKeyEvent("p", 80));

        keyboard.add(new StandardRemiKeyEvent("a", 65));
        keyboard.add(new StandardRemiKeyEvent("s", 83));
        keyboard.add(new StandardRemiKeyEvent("d", 68));
        keyboard.add(new StandardRemiKeyEvent("f", 70));
        keyboard.add(new StandardRemiKeyEvent("g", 71));
        keyboard.add(new StandardRemiKeyEvent("h", 72));
        keyboard.add(new StandardRemiKeyEvent("j", 74));
        keyboard.add(new StandardRemiKeyEvent("k", 75));
        keyboard.add(new StandardRemiKeyEvent("l", 76));
        keyboard.add(new StandardRemiKeyEvent("/", 111));

        keyboard.add(new StandardRemiKeyEvent("*", 106));
        keyboard.add(new StandardRemiKeyEvent("z", 90));
        keyboard.add(new StandardRemiKeyEvent("x", 88));
        keyboard.add(new StandardRemiKeyEvent("c", 67));
        keyboard.add(new StandardRemiKeyEvent("v", 86));
        keyboard.add(new StandardRemiKeyEvent("b", 66));
        keyboard.add(new StandardRemiKeyEvent("n", 78));
        keyboard.add(new StandardRemiKeyEvent("m", 77));
        keyboard.add(new BackspaceRemiKeyEvent("", 8));

        keyboard.add(new StandardRemiKeyEvent("+", 521));
        keyboard.add(new StandardRemiKeyEvent(",", 44));
        keyboard.add(new SpaceRemiKeyEvent("SPACE", 32));
        keyboard.add(new StandardRemiKeyEvent(".", 46));
        keyboard.add(new EnterRemiKeyEvent("", 10));

        // On a german keyboard, let's switch Z and Y
        String language = Locale.getDefault().getLanguage();
        if (language.equals("de")) {
            RemiKeyEvent eventZ = keyboard.get(31);
            keyboard.set(31, keyboard.get(15));
            keyboard.set(15, eventZ);
        }
        return keyboard;
    }

    public static RemiKeyEvent getArrowKeyEvent(ArrowDirection direction) {

        switch (direction) {

            case UP:
                return new StandardRemiKeyEvent("UP", 38);

            case LEFT:
                return new StandardRemiKeyEvent("LEFT", 37);

            case DOWN:
                return new StandardRemiKeyEvent("DOWN", 40);

            case RIGHT:
                return new StandardRemiKeyEvent("RIGHT", 39);

            default:
                return new StandardRemiKeyEvent("", -1);
        }
    }

    public static String getConnectionErrorByResultCode(Context context, int resultCode) {

        String error;
        if (resultCode == RemiClient.CONNECTION_RESULT_ERROR_ALREADY_CONNECTED) {
            error = context.getString(R.string.connection_error_already_connected);
        } else if (resultCode == RemiClient.CONNECTION_RESULT_ERROR_NETWORK) {
            error = context.getString(R.string.connection_error_network);
        } else {
            error = context.getString(R.string.connection_error_unknown);
        }
        return error;
    }

    public static int getOperatingSystemIcon(String os) {

        int icon = 0;
        switch (os) {

            case "Windows":
                icon = R.drawable.ic_os_windows;
                break;

            case "Linux":
                icon = R.drawable.ic_os_linux;
                break;

            case "Mac":
                icon = R.drawable.ic_os_apple;
                break;

            case "NA":
                icon = R.drawable.ic_os_na;
                break;
        }
        return icon;
    }

    public static void copyFileToDownloadsFolder(byte[] content, String filename) throws IOException {
        File fileOut = new File(Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), filename);
        ByteArrayInputStream inStream = new ByteArrayInputStream(content);

        FileOutputStream out = new FileOutputStream(fileOut);
        IOUtils.copy(inStream, out);
        IOUtils.closeQuietly(inStream);
        IOUtils.closeQuietly(out);
    }

    public static Bitmap base64ToImage(byte[] base64Image) {
        byte[] decoded = Base64.decode(base64Image, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
    }

    @NonNull
    public static String eventName(RemiClient.ClientEvent event) {
        return event.name().toLowerCase();
    }

    @NonNull
    public static String eventName(RemiClient.ServerEvent event) {
        return event.name().toLowerCase();
    }


}
