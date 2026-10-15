package io.github.reyfxck.wpsxandroid.core.wps

/**
 * Standard WPS PIN checksum helper.
 *
 * This only formats and validates an already-known seven-digit base PIN. It
 * does not derive router/vendor credentials from network identifiers.
 */
object WpsPinChecksum {
    private val fullPinPattern = Regex("^\\d{8}$")

    fun checksumDigit(basePin: Int): Int {
        require(basePin in 0..9_999_999) {
            "WPS base PIN must contain at most seven decimal digits"
        }

        var remaining = basePin
        var accumulator = 0

        while (remaining != 0) {
            accumulator += 3 * (remaining % 10)
            remaining /= 10
            accumulator += remaining % 10
            remaining /= 10
        }

        return (10 - (accumulator % 10)) % 10
    }

    fun format(basePin: Int): String {
        val base = basePin.toString().padStart(7, '0')
        return base + checksumDigit(basePin)
    }

    fun isValid(pin: String): Boolean {
        if (!fullPinPattern.matches(pin)) return false

        val basePin = pin.substring(0, 7).toInt()
        val suppliedChecksum = pin.last().digitToInt()
        return checksumDigit(basePin) == suppliedChecksum
    }
}
