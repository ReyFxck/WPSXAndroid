package io.github.reyfxck.wpsxandroid.core.capability

import org.junit.Assert.assertEquals
import org.junit.Test

class IwDevParserTest {
    @Test
    fun `parses mixed interface types`() {
        val output = """
            phy#0
            	Interface wlan0
            		ifindex 7
            		type managed
            	Interface mon0
            		ifindex 9
            		type monitor
        """.trimIndent()

        assertEquals(listOf("mon0"), IwDevParser.monitorInterfaces(output))
    }

    @Test
    fun `returns empty list when no monitor interface exists`() {
        val output = """
            phy#0
            	Interface wlan0
            		type managed
        """.trimIndent()

        assertEquals(emptyList<String>(), IwDevParser.monitorInterfaces(output))
    }
}
