package at.shockbytes.remote.network.model;

/**
 * @author Martin Macheiner
 *         Date: 09.10.2017.
 */

public class ConnectionConfig {

    private String desktopOS;
    private ConnectionPermissions permissions;

    public ConnectionConfig() {
        this("", new ConnectionPermissions());
    }

    private ConnectionConfig(String desktopOS, ConnectionPermissions permissions) {
        this.desktopOS = desktopOS;
        this.permissions = permissions;
    }

    public String getDesktopOS() {
        return desktopOS;
    }

    public ConnectionConfig setDesktopOS(String desktopOS) {
        this.desktopOS = desktopOS;
        return this;
    }

    public ConnectionPermissions getPermissions() {
        return permissions;
    }

    public ConnectionConfig setPermissions(ConnectionPermissions permissions) {
        this.permissions = permissions;
        return this;
    }

    public static class ConnectionPermissions {

        private boolean hasMousePermission;
        private boolean hasAppsPermission;
        private boolean hasFilesPermission;
        private boolean hasFileTransferPermission;

        public ConnectionPermissions() {
            this(true, true, true, true);
        }

        public ConnectionPermissions(boolean hasMousePermission, boolean hasAppsPermission,
                              boolean hasFilesPermission, boolean hasFileTransferPermission) {
            this.hasMousePermission = hasMousePermission;
            this.hasAppsPermission = hasAppsPermission;
            this.hasFilesPermission = hasFilesPermission;
            this.hasFileTransferPermission = hasFileTransferPermission;
        }

        public boolean hasMousePermission() {
            return hasMousePermission;
        }

        public ConnectionPermissions setHasMousePermission(boolean hasMousePermission) {
            this.hasMousePermission = hasMousePermission;
            return this;
        }

        public boolean hasAppsPermission() {
            return hasAppsPermission;
        }

        public ConnectionPermissions setHasAppsPermission(boolean hasAppsPermission) {
            this.hasAppsPermission = hasAppsPermission;
            return this;
        }

        public boolean hasFilesPermission() {
            return hasFilesPermission;
        }

        public ConnectionPermissions setHasFilesPermission(boolean hasFilesPermission) {
            this.hasFilesPermission = hasFilesPermission;
            return this;
        }

        public boolean hasFileTransferPermission() {
            return hasFileTransferPermission;
        }

        public ConnectionPermissions setHasFileTransferPermission(boolean hasFileTransferPermission) {
            this.hasFileTransferPermission = hasFileTransferPermission;
            return this;
        }

    }

}
