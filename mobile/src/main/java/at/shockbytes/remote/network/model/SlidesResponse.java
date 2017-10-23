package at.shockbytes.remote.network.model;

import java.util.List;

/**
 * @author Martin Macheiner
 *         Date: 15.10.2017.
 */

public class SlidesResponse {

    private final String name;
    private final int errorCode;
    private final List<SlidesEntry> slides;
    private final int slideAmount;

    public SlidesResponse(String name, int errorCode, List<SlidesEntry> slides, int slideAmount) {
        this.name = name;
        this.errorCode = errorCode;
        this.slides = slides;
        this.slideAmount = slideAmount;
    }

    public String getName() {
        return name;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public int getSlideAmount() {
        return slideAmount;
    }

    public List<SlidesEntry> getSlides() {
        return slides;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("Name: ").append(getName()).append("\n");
        sb.append("Error code: ").append(getErrorCode()).append("\n");
        sb.append("Slide amount: ").append(getSlideAmount()).append("\n");
        for (SlidesEntry e : slides) {
            sb.append(e.toString());
        }
        return sb.toString();
    }

    public class SlidesEntry {

        private final byte[] base64Image;
        private final String notes;
        private final int slideNumber;

        public SlidesEntry(byte[] base64Image, String notes, int slideNumber) {
            this.base64Image = base64Image;
            this.notes = notes;
            this.slideNumber = slideNumber;
        }

        public byte[] getBase64Image() {
            return base64Image;
        }

        public String getNotes() {
            return notes;
        }

        public int getSlideNumber() {
            return slideNumber;
        }


        @Override
        public String toString() {
            return "\tBase64 length: " + (base64Image != null ? base64Image.length : -1)
                    + "\n\tNotes: " + notes
                    + "\n\tSlide number: " + slideNumber + "\n-------------\n";
        }
    }

}

