package at.shockbytes.remote.debug

/**
 * @author Martin Macheiner
 * Date: 06.11.2017.
 */

class DebugOptions {

    enum class DebugAction {
        FAKE_LOGIN, FAKE_DEVICES, REGENERATE_KEYS, FORCE_UNAUTHORIZED_CONNECTION
    }

    interface OnDebugOptionSelectedListener {

        fun onDebugOptionSelected(action : DebugAction)

    }


}