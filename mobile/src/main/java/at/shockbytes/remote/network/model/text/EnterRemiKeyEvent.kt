package at.shockbytes.remote.network.model.text

import at.shockbytes.remote.R

/**
 * @author Martin Macheiner
 * Date: 11.10.2017.
 */

class EnterRemiKeyEvent(display: String, keyCode: Int) : RemiKeyEvent(display, keyCode) {

    override fun layout(): Int {
        return R.layout.keyboard_item_enter
    }

    override fun rowSpan(): Int {
        return 2
    }

    override fun capsLockSensitive(): Boolean {
        return false
    }
}
