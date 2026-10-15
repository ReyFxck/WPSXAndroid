package io.github.reyfxck.wpsxandroid.core

import io.github.reyfxck.wpsxandroid.core.model.WifiSecurity
import io.github.reyfxck.wpsxandroid.core.wifi.WifiCapabilityParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WifiCapabilityParserTest {
    @Test
    fun detectsWpa2AndWps() {
        val parsed = WifiCapabilityParser.parse("[WPA2-PSK-CCMP][WPS][ESS]")

        assertTrue(WifiSecurity.WPA2 in parsed.securityModes)
        assertTrue(parsed.wpsAdvertised)
    }

    @Test
    fun detectsWpa3Sae() {
        val parsed = WifiCapabilityParser.parse("[RSN-SAE-CCMP][ESS]")

        assertTrue(WifiSecurity.WPA3 in parsed.securityModes)
        assertFalse(parsed.wpsAdvertised)
    }

    @Test
    fun marksPlainEssAsOpen() {
        val parsed = WifiCapabilityParser.parse("[ESS]")

        assertEquals(setOf(WifiSecurity.OPEN), parsed.securityModes)
    }
}
