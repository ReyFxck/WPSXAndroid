package io.github.reyfxck.wpsxandroid.core.wps

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WpsInformationElementParserTest {
    @Test
    fun `parses version manufacturer model and lock state`() {
        val body = byteArrayOf(
            0x00, 0x50, 0xF2.toByte(), 0x04,
            0x10, 0x4A, 0x00, 0x01, 0x10,
            0x10, 0x57, 0x00, 0x01, 0x01,
            0x10, 0x21, 0x00, 0x08,
            'I'.code.toByte(), 'n'.code.toByte(), 't'.code.toByte(), 'e'.code.toByte(),
            'l'.code.toByte(), 'b'.code.toByte(), 'r'.code.toByte(), 'a'.code.toByte(),
            0x10, 0x23, 0x00, 0x03,
            'X'.code.toByte(), '1'.code.toByte(), '0'.code.toByte(),
        )

        val parsed = WpsInformationElementParser.parseVendorSpecificElement(body)
        assertNotNull(parsed)
        assertEquals("1.0", parsed?.version)
        assertTrue(parsed?.apSetupLocked == true)
        assertEquals("Intelbra", parsed?.manufacturer)
        assertEquals("X10", parsed?.modelName)
    }

    @Test
    fun `parses setup state serial device name and config methods`() {
        val body = byteArrayOf(
            0x00, 0x50, 0xF2.toByte(), 0x04,
            0x10, 0x44, 0x00, 0x01, 0x02,
            0x10, 0x42, 0x00, 0x07,
            '1'.code.toByte(), '2'.code.toByte(), '3'.code.toByte(), '4'.code.toByte(),
            '5'.code.toByte(), '6'.code.toByte(), '7'.code.toByte(),
            0x10, 0x11, 0x00, 0x06,
            'R'.code.toByte(), 'o'.code.toByte(), 'u'.code.toByte(), 't'.code.toByte(),
            'e'.code.toByte(), 'r'.code.toByte(),
            0x10, 0x08, 0x00, 0x02, 0x01, 0x88.toByte(),
        )

        val parsed = WpsInformationElementParser.parseVendorSpecificElement(body)
        assertNotNull(parsed)
        assertEquals(2, parsed?.setupState)
        assertEquals("Configured", parsed?.setupStateLabel)
        assertEquals("1234567", parsed?.serialNumber)
        assertEquals("Router", parsed?.deviceName)
        assertEquals(0x0188, parsed?.configMethodsMask)
        assertEquals(listOf("Display", "PBC", "Keypad"), parsed?.configMethods)
    }

    @Test
    fun `parses WPS 2 specific virtual config methods without duplicate base labels`() {
        val body = byteArrayOf(
            0x00, 0x50, 0xF2.toByte(), 0x04,
            0x10, 0x08, 0x00, 0x02, 0x22, 0x88.toByte(),
        )

        val parsed = WpsInformationElementParser.parseVendorSpecificElement(body)
        assertEquals(listOf("Virtual Display", "Virtual PBC"), parsed?.configMethods)
    }

    @Test
    fun `parses WPS 2 version from WFA vendor extension`() {
        val body = byteArrayOf(
            0x00, 0x50, 0xF2.toByte(), 0x04,
            0x10, 0x49, 0x00, 0x06,
            0x00, 0x37, 0x2A,
            0x00, 0x01, 0x20,
        )

        val parsed = WpsInformationElementParser.parseVendorSpecificElement(body)
        assertEquals("2.0", parsed?.effectiveVersion)
    }

    @Test
    fun `returns null for non WPS vendor element`() {
        val body = byteArrayOf(0x00, 0x11, 0x22, 0x33, 0x01)
        assertEquals(null, WpsInformationElementParser.parseVendorSpecificElement(body))
    }

    @Test
    fun `parses unlocked AP setup flag`() {
        val body = byteArrayOf(
            0x00, 0x50, 0xF2.toByte(), 0x04,
            0x10, 0x57, 0x00, 0x01, 0x00,
        )
        val parsed = WpsInformationElementParser.parseVendorSpecificElement(body)
        assertFalse(parsed?.apSetupLocked ?: true)
    }
}
