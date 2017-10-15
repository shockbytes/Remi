package at.shockbytes.remote.network.model.text;

import at.shockbytes.remote.R;

/**
 * @author Martin Macheiner
 *         Date: 11.10.2017.
 */

public class StandardRemiKeyEvent extends RemiKeyEvent {

    public StandardRemiKeyEvent(String display, int keyCode) {
        super(display, keyCode);
    }

    @Override
    public int layout() {
        return R.layout.keyboard_item_key;
    }

    @Override
    public int getRowSpan() {
        return 1;
    }

    @Override
    public boolean capsLockSensitive() {
        return true;
    }
}
