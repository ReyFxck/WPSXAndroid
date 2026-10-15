package io.github.reyfxck.wpsxandroid.core

import io.github.reyfxck.wpsxandroid.core.wifi.wifiChannelFromFrequency
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WifiChannelTest {
    @Test
    fun mapsCommon24GhzChannels() {
        assertEquals(1, wifiChannelFromFrequency(2412))
        assertEquals(11, wifiChannelFromFrequency(2462))
        assertEquals(14, wifiChannelFromFrequency(2484))
    }

    @Test
    fun maps5GhzAnd6GhzChannels() {
        assertEquals(36, wifiChannelFromFrequency(5180))
        assertEquals(149, wifiChannelFromFrequency(5745))
        assertEquals(2, wifiChannelFromFrequency(5935))
        assertEquals(1, wifiChannelFromFrequency(5955))
        assertEquals(233, wifiChannelFromFrequency(7115))
    }

    @Test
    fun rejectsUnknownFrequency() {
        assertNull(wifiChannelFromFrequency(1234))
    }
}
