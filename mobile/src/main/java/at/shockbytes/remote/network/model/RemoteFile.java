package at.shockbytes.remote.network.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author Martin Macheiner
 *         Date: 17.04.2015
 */
public class RemoteFile implements Parcelable {

    private final String filename;
    private String fileExtension;
    private final boolean isDirectory;
    private final boolean isExecutable;

    public RemoteFile(String filename, boolean isDirectory, boolean isExecutable) {
        this.filename = filename;
        this.isDirectory = isDirectory;
        this.isExecutable = isExecutable;

        getFileExtensionFromName();
    }

    protected RemoteFile(Parcel in) {
        filename = in.readString();
        isDirectory = in.readByte() != 0;
        isExecutable = in.readByte() != 0;
        fileExtension = in.readString();
    }

    public static final Creator<RemoteFile> CREATOR = new Creator<RemoteFile>() {
        @Override
        public RemoteFile createFromParcel(Parcel in) {
            return new RemoteFile(in);
        }

        @Override
        public RemoteFile[] newArray(int size) {
            return new RemoteFile[size];
        }
    };

    private void getFileExtensionFromName() {

        if (isDirectory()) {
            fileExtension = "directory";
            return;
        }

        int idx = filename.lastIndexOf(".");
        fileExtension = (idx > 0)
                ? filename.substring(idx + 1, filename.length()).toLowerCase()
                : "";
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public String getFilename() {
        return filename;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public boolean isExecutable() {
        return isExecutable;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(filename);
        dest.writeByte((byte) (isDirectory ? 1 : 0));
        dest.writeByte((byte) (isExecutable ? 1 : 0));
        dest.writeString(fileExtension);
    }

}
