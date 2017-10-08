package at.shockbytes.remote.network.model;

/**
 * @author Martin Macheiner
 *         Date: 17.04.2015
 */
public class RemiFile {

    private final String name;
    private final String path;
    private String extension;
    private final boolean isDirectory;
    private final boolean isExecutable;

    public RemiFile(String name, String path, boolean isDirectory, boolean isExecutable) {
        this.name = name;
        this.path = path;
        this.isDirectory = isDirectory;
        this.isExecutable = isExecutable;

        getFileExtensionFromName();
    }

    private void getFileExtensionFromName() {

        if (isDirectory()) {
            extension = "directory";
        } else {
            int idx = name.lastIndexOf(".");
            extension = (idx > 0)
                    ? name.substring(idx + 1, name.length()).toLowerCase()
                    : "";
        }
    }

    public String getExtension() {
        return extension;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public boolean isExecutable() {
        return isExecutable;
    }

}
