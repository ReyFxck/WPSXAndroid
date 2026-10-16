package io.github.reyfxck.wpsxandroid.core.wifi

/**
 * Small offline OUI resolver used when beacon metadata does not expose a manufacturer string.
 *
 * This intentionally starts compact. The resolver is isolated so a generated IEEE OUI dataset
 * can replace/extend it later without coupling vendor lookup to scanner or PIN logic.
 */
object MacVendorResolver {
    private val vendors = mapOf(
        "546CAC" to "Intelbras",
        "982A0A" to "Intelbras",
        "D44F67" to "Huawei",
        "84A329" to "Arcadyan",
        "E8458B" to "MitraStar",
        "D8380D" to "IP-COM",
        "7CAADE" to "AltoBeam",
        "0CF0B4" to "Globalsat",
    )

    fun resolve(mac: String): String? {
        val normalized = mac.filter(Char::isLetterOrDigit).uppercase()
        if (normalized.length < 6) return null
        return vendors[normalized.take(6)]
    }
}
