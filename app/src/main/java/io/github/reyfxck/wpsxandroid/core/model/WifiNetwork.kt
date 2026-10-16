package io.github.reyfxck.wpsxandroid.core.model

import io.github.reyfxck.wpsxandroid.core.wps.WpsBeaconInfo

data class WifiNetwork(
    val ssid: String,
    val bssid: String,
    val rssiDbm: Int,
    val frequencyMhz: Int,
    val channel: Int?,
    val capabilities: String,
    val securityModes: Set<WifiSecurity>,
    val wpsAdvertised: Boolean,
    val wpsInfo: WpsBeaconInfo? = null,
    val vendorName: String? = null,
    val wifiStandard: WifiStandard = WifiStandard.UNKNOWN,
)

enum class WifiSecurity {
    OPEN,
    WEP,
    WPA,
    WPA2,
    WPA3,
    OWE,
    UNKNOWN,
}

enum class WifiStandard(val label: String) {
    LEGACY("Legacy"),
    WIFI_4("Wi-Fi 4 / 802.11n"),
    WIFI_5("Wi-Fi 5 / 802.11ac"),
    WIFI_6("Wi-Fi 6 / 802.11ax"),
    WIFI_60_GHZ("WiGig / 802.11ad"),
    WIFI_7("Wi-Fi 7 / 802.11be"),
    UNKNOWN("Unknown"),
}
