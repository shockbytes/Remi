package at.shockbytes.remote.util;

import android.support.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

import at.shockbytes.remote.R;
import at.shockbytes.remote.network.RemiClient;
import at.shockbytes.remote.network.model.RemiFile;
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

    public enum Irrelevant { INSTANCE }

    enum FileCategory {
        FOLDER, TEXT, CODE, PDF, EXE, JAR, APP, APK, IMAGE,
        MUSIC, VIDEO, ARCHIVE, POWERPOINT, WORD, EXCEL, NA
    }

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

    public static int getDrawableResourceForFiletype(RemiFile file) {
        return fileExtensionMap.get(getFileCategory(file));
    }

    private static FileCategory getFileCategory(RemiFile file) {

        if (file.isDirectory()) {
            return FOLDER;
        }

        FileCategory category;
        switch (file.getExtension()) {

            // TODO Music

            // TODO Video

            // TODO Code

            // TODO Archive

            case "text":
                // Fall through
            case "txt":
                category = TEXT;
                break;

            case "jar":
                category = JAR;
                break;

            case "bat":
                // Fall through
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
                // Fall through
            case "ppt":
                category = POWERPOINT;
                break;

            case "xlsx":
                // Fall through
            case "xls":
                category = EXCEL;
                break;

            case "odt":
                // Fall through
            case "docx":
                // Fall through
            case "doc":
                category = WORD;
                break;

            default:
                category = NA;
                break;

        }
        return category;
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
