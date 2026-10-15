package io.github.reyfxck.wpsxandroid.core.wifi

import io.github.reyfxck.wpsxandroid.core.model.WifiNetwork
import io.github.reyfxck.wpsxandroid.core.model.WifiSecurity
import org.junit.Assert.assertEquals
import org.junit.Test

class WifiBandTest {
    @Test
    fun mapsCommonFrequenciesToBands() {
        assertEquals(WifiBand.GHZ_2_4, wifiBandForFrequency(2412))
        assertEquals(WifiBand.GHZ_5, wifiBandForFrequency(5180))
        assertEquals(WifiBand.GHZ_6, wifiBandForFrequency(5975))
        assertEquals(WifiBand.GHZ_60, wifiBandForFrequency(60480))
        assertEquals(WifiBand.UNKNOWN, wifiBandForFrequency(1000))
    }

    @Test
    fun groupsAccessPointsByBandAndChannel() {
        val networks = listOf(
            network("a", 2412, 1),
            network("b", 2412, 1),
            network("c", 2437, 6),
            network("d", 5180, 36),
        )

        assertEquals(
            listOf(
                ChannelUsage(WifiBand.GHZ_2_4, 1, 2),
                ChannelUsage(WifiBand.GHZ_2_4, 6, 1),
                ChannelUsage(WifiBand.GHZ_5, 36, 1),
            ),
            buildChannelUsage(networks),
        )
    }

    private fun network(id: String, frequency: Int, channel: Int): WifiNetwork = WifiNetwork(
        ssid = id,
        bssid = "00:11:22:33:44:${id.codePointAt(0).toString(16).padStart(2, '0')}",
        rssiDbm = -60,
        frequencyMhz = frequency,
        channel = channel,
        capabilities = "[WPA2-PSK-CCMP][ESS]",
        securityModes = setOf(WifiSecurity.WPA2),
        wpsAdvertised = false,
    )
}
