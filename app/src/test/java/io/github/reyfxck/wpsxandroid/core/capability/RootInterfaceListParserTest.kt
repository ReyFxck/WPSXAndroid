package io.github.reyfxck.wpsxandroid.core.capability

import org.junit.Assert.assertEquals
import org.junit.Test

class RootInterfaceListParserTest {
    @Test
    fun keepsSafeUniqueKernelInterfaceNames() {
        val parsed = RootInterfaceListParser.parse(
            """
            lo
            wlan0
            p2p0
            rmnet_data0
            wlan0
            bad name
            """.trimIndent(),
        )

        assertEquals(listOf("lo", "p2p0", "rmnet_data0", "wlan0"), parsed)
    }
}
