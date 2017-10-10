package at.shockbytes.remote.network.model;

/**
 * @author Martin Macheiner
 *         Date: 27.09.2017.
 */

public class DesktopApp {

    private String name;
    private String ip;
    private String os;
    private String signature;

    public DesktopApp(String name, String ip, String os, String signature) {
        this.name = name;
        this.ip = ip;
        this.os = os;
        this.signature = signature;
    }

    public String getName() {
        return name;
    }

    public String getIp() {
        return ip;
    }

    public String getOs() {
        if (os == null) {
            os = "NA";
        }
        return os;
    }

    public String getSignature() {
        return signature;
    }

    @Override
    public String toString() {
        return "Name: " + name +"\nIP: " + ip +"\nOS: " + os + "\nSignature: " + signature;
    }

    @Override
    public boolean equals(Object obj) {

        if (obj instanceof DesktopApp) {
            DesktopApp o = (DesktopApp) obj;
            return name.equals(o.name) && ip.equals(o.ip)
                    && os.equals(o.os) && signature.equals(o.signature);
        }

        return false;
    }
}
