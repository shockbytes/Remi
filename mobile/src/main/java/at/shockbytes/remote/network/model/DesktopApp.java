package at.shockbytes.remote.network.model;

import at.shockbytes.remote.R;

/**
 * @author Martin Macheiner
 *         Date: 27.09.2017.
 */

public class DesktopApp {

    public enum OperatingSystem {
        WINDOWS, LINUX, MAC_OS
    }

    private String name;
    private String ip;
    private OperatingSystem os;

    public DesktopApp(String name, String ip, OperatingSystem os) {
        this.name = name;
        this.ip = ip;
        this.os = os;
    }

    public String getName() {
        return name;
    }

    public String getIp() {
        return ip;
    }

    public OperatingSystem getOperatingSystem() {
        return os;
    }

    public int getOperatingSystemIcon() {

        int icon = 0;
        switch (os) {

            case WINDOWS:
                icon = R.drawable.ic_os_windows;
                break;

            case LINUX:
                icon = R.drawable.ic_os_linux;
                break;

            case MAC_OS:
                icon = R.drawable.ic_os_apple;
                break;
        }
        return icon;
    }

}
