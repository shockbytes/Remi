package at.shockbytes.remote.util;

import java.util.HashMap;
import java.util.Map;

import at.shockbytes.remote.R;
import at.shockbytes.remote.network.model.RemiFile;
import at.shockbytes.util.ResourceManager;

/**
 * @author Martin Macheiner
 *         Date: 30.09.2017.
 */

public class RemiUtils extends ResourceManager {

    private enum FileCategory{
        FOLDER, TEXT, CODE, PDF, EXE, JAR, APP, APK, IMAGE,
        MUSIC, VIDEO, ARCHIVE, POWERPOINT, WORD, EXCEL
    }

    private static Map<FileCategory, Integer> fileExtensionMap;

    static {
        fileExtensionMap = new HashMap<>();
        fileExtensionMap.put(FileCategory.FOLDER, R.drawable.ic_tab_files);
    }

    public static String createUrlFromIp(String ip, int port, boolean useHttps) {
        String scheme = useHttps ? "https://" : "http://";
        return scheme + ip + ":" + port;
    }

    public static int getDrawableResourceForFiletype(RemiFile file) {
        return fileExtensionMap.get(getFileCategory(file));
    }

    private static FileCategory getFileCategory(RemiFile file) {
        // TODO
        return FileCategory.FOLDER;
    }

}
