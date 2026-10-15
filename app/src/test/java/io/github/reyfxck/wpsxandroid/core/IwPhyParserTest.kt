package io.github.reyfxck.wpsxandroid.core

import io.github.reyfxck.wpsxandroid.core.capability.IwPhyParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class IwPhyParserTest {
    @Test
    fun detectsMonitorInSupportedModes() {
        val output = """
            Wiphy phy0
            	Supported interface modes:
            		 * managed
            		 * AP
            		 * monitor
            	Band 1:
        """.trimIndent()

        assertEquals(true, IwPhyParser.reportsMonitorMode(output))
    }

    @Test
    fun reportsFalseWhenSupportedModesArePresentWithoutMonitor() {
        val output = """
            Wiphy phy0
            	Supported interface modes:
            		 * managed
            		 * AP
            	software interface modes (can always be added):
        """.trimIndent()

        assertEquals(false, IwPhyParser.reportsMonitorMode(output))
    }

    @Test
    fun reportsUnknownForUnparseableOutput() {
        assertNull(IwPhyParser.reportsMonitorMode("permission denied"))
    }
}
