package at.shockbytes.remote.network.model;

/**
 * @author Martin Macheiner
 *         Date: 26.09.2017.
 */

public class FileTransferResponse {

    private final String filename;
    private final byte[] content;
    private final String exception;

    public FileTransferResponse() {
        this(null, null, null);
    }

    public FileTransferResponse(String filename, byte[] content, String exception) {
        this.filename = filename;
        this.content = content;
        this.exception = exception;
    }

    public String getFilename() {
        return filename;
    }

    public byte[] getContent() {
        return content;
    }

    public String getException() {
        return exception;
    }

    public boolean isEmpty() {
        return content == null;
    }

    @Override
    public String toString() {
        int size = content == null ? -1 : content.length;
        return "Filename: " + filename + "\nException: " + exception + "\nSize: " + size;
    }
}
