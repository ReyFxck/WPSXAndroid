package io.github.reyfxck.wpsxandroid.core.wps

/**
 * Canonicalizes a BSSID without making vendor-specific assumptions.
 *
 * Accepted inputs may use ':', '-' or '.' separators. Output is always the
 * upper-case colon-delimited form used by the rest of the core.
 */
object BssidNormalizer {
    private val compactPattern = Regex("^[0-9A-Fa-f]{12}$")

    fun normalize(value: String): String? {
        val compact = value
            .trim()
            .replace(":", "")
            .replace("-", "")
            .replace(".", "")

        if (!compactPattern.matches(compact)) return null

        return compact
            .uppercase()
            .chunked(2)
            .joinToString(":")
    }
}
