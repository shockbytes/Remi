package at.shockbytes.remote.network.model.text;

/**
 * @author Martin Macheiner
 *         Date: 10.10.2017.
 */

public abstract class RemiKeyEvent {

    private int keyCode;
    private String display;

    public RemiKeyEvent(String display, int keyCode) {
        this.display = display;
        this.keyCode = keyCode;
    }

    public int getKeyCode() {
        return keyCode;
    }

    public String getDisplayString() {
        return display;
    }

    public abstract int layout();

    public abstract int getRowSpan();

    public abstract boolean capsLockSensitive();

    @Override
    public boolean equals(Object obj) {

        if (obj instanceof RemiKeyEvent) {
            RemiKeyEvent o = (RemiKeyEvent) obj;
            return keyCode == o.keyCode;
        }
        return super.equals(obj);
    }
}
