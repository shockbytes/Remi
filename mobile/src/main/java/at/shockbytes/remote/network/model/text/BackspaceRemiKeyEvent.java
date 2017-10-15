package at.shockbytes.remote.network.model.text;

import at.shockbytes.remote.R;

/**
 * @author Martin Macheiner
 *         Date: 11.10.2017.
 */

public class BackspaceRemiKeyEvent extends RemiKeyEvent {

    public BackspaceRemiKeyEvent(String display, int keyCode) {
        super(display, keyCode);
    }

    @Override
    public int layout() {
        return R.layout.keyboard_item_backspace;
    }

    @Override
    public int getRowSpan() {
        return 2;
    }

    @Override
    public boolean capsLockSensitive() {
        return false;
    }
}
