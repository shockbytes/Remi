package at.shockbytes.remote.network.model.text

/**
 * @author Martin Macheiner
 * Date: 10.10.2017.
 */

abstract class RemiKeyEvent constructor(val displayString: String, val keyCode: Int) {

    abstract fun layout(): Int

    abstract fun capsLockSensitive(): Boolean

    abstract fun rowSpan() : Int

    override fun equals(other: Any?): Boolean {

        if (other is RemiKeyEvent) {
            val o = other as RemiKeyEvent?
            return keyCode == o!!.keyCode
        }
        return super.equals(other)
    }

    override fun hashCode(): Int {
        var result = displayString.hashCode()
        result = 31 * result + keyCode
        return result
    }
}
