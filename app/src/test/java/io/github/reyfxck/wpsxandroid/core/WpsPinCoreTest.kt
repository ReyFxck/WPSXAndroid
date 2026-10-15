package io.github.reyfxck.wpsxandroid.core

import io.github.reyfxck.wpsxandroid.core.wps.BssidNormalizer
import io.github.reyfxck.wpsxandroid.core.wps.Confidence
import io.github.reyfxck.wpsxandroid.core.wps.WpsPinAlgorithm
import io.github.reyfxck.wpsxandroid.core.wps.WpsPinAlgorithmRegistry
import io.github.reyfxck.wpsxandroid.core.wps.WpsPinCandidate
import io.github.reyfxck.wpsxandroid.core.wps.WpsPinChecksum
import io.github.reyfxck.wpsxandroid.core.wps.WpsPinInput
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WpsPinCoreTest {
    @Test
    fun normalizesCommonBssidFormats() {
        assertEquals("AA:BB:CC:DD:EE:FF", BssidNormalizer.normalize("aa:bb:cc:dd:ee:ff"))
        assertEquals("AA:BB:CC:DD:EE:FF", BssidNormalizer.normalize("aa-bb-cc-dd-ee-ff"))
        assertEquals("AA:BB:CC:DD:EE:FF", BssidNormalizer.normalize("aabb.ccdd.eeff"))
        assertEquals("AA:BB:CC:DD:EE:FF", BssidNormalizer.normalize("AABBCCDDEEFF"))
    }

    @Test
    fun rejectsMalformedBssid() {
        assertNull(BssidNormalizer.normalize("AA:BB:CC:DD:EE"))
        assertNull(BssidNormalizer.normalize("not-a-bssid"))
    }

    @Test
    fun formatsAndValidatesStandardWpsChecksum() {
        assertEquals(0, WpsPinChecksum.checksumDigit(1_234_567))
        assertEquals("12345670", WpsPinChecksum.format(1_234_567))
        assertEquals("00000017", WpsPinChecksum.format(1))
        assertTrue(WpsPinChecksum.isValid("12345670"))
        assertTrue(WpsPinChecksum.isValid("00000017"))
        assertFalse(WpsPinChecksum.isValid("12345671"))
        assertFalse(WpsPinChecksum.isValid("1234567"))
    }

    @Test
    fun registryNormalizesInputBeforeDispatch() {
        var receivedInput: WpsPinInput? = null
        val algorithm = object : WpsPinAlgorithm {
            override val id = "fixture"
            override val displayName = "Fixture"

            override fun supports(input: WpsPinInput): Boolean {
                receivedInput = input
                return input.bssid == "AA:BB:CC:DD:EE:FF"
            }

            override fun derive(input: WpsPinInput): List<WpsPinCandidate> = listOf(
                WpsPinCandidate(
                    pin = WpsPinChecksum.format(1_234_567),
                    algorithmId = id,
                    confidence = Confidence.EXACT,
                ),
            )
        }

        val candidates = WpsPinAlgorithmRegistry(listOf(algorithm)).derive(
            WpsPinInput(
                bssid = "aa-bb-cc-dd-ee-ff",
                ssid = "  Lab AP  ",
            ),
        )

        assertEquals("AA:BB:CC:DD:EE:FF", receivedInput?.bssid)
        assertEquals("Lab AP", receivedInput?.ssid)
        assertEquals(listOf("12345670"), candidates.map { it.pin })
    }

    @Test
    fun registryRejectsInvalidBssidWithoutCallingAlgorithms() {
        var called = false
        val algorithm = object : WpsPinAlgorithm {
            override val id = "fixture"
            override val displayName = "Fixture"
            override fun supports(input: WpsPinInput): Boolean {
                called = true
                return true
            }
            override fun derive(input: WpsPinInput): List<WpsPinCandidate> = emptyList()
        }

        val candidates = WpsPinAlgorithmRegistry(listOf(algorithm)).derive(
            WpsPinInput(bssid = "invalid"),
        )

        assertTrue(candidates.isEmpty())
        assertFalse(called)
    }
}
