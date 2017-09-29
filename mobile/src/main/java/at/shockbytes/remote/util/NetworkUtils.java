package at.shockbytes.remote.util;

/**
 * @author Martin Macheiner
 *         Date: 27.09.2017.
 */

public class NetworkUtils {

    public static String createUrlFromIp(String ip, int port, boolean useHttps) {
        String scheme = useHttps ? "https://" : "http://";
        return scheme + ip + ":" + port;
    }

}
