package io.github.reyfxck.wpsxandroid.core.wifi

import io.github.reyfxck.wpsxandroid.core.model.WifiSecurity

data class ParsedWifiCapabilities(
    val securityModes: Set<WifiSecurity>,
    val wpsAdvertised: Boolean,
)

object WifiCapabilityParser {
    fun parse(rawCapabilities: String): ParsedWifiCapabilities {
        val raw = rawCapabilities.uppercase()
        val security = linkedSetOf<WifiSecurity>()

        if ("WEP" in raw) security += WifiSecurity.WEP
        if ("WPA-" in raw || "[WPA]" in raw || "WPA-PSK" in raw) security += WifiSecurity.WPA
        if ("WPA2" in raw || "RSN-PSK" in raw || "PSK" in raw) security += WifiSecurity.WPA2
        if ("WPA3" in raw || "SAE" in raw) security += WifiSecurity.WPA3
        if ("OWE" in raw) security += WifiSecurity.OWE

        if (security.isEmpty()) {
            security += if (raw.isBlank() || "ESS" in raw) {
                WifiSecurity.OPEN
            } else {
                WifiSecurity.UNKNOWN
            }
        }

        return ParsedWifiCapabilities(
            securityModes = security,
            wpsAdvertised = "WPS" in raw,
        )
    }
}
