package io.github.reyfxck.wpsxandroid.core.capability

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PacketInjectionReadinessTest {
    private val wlan0 = WifiInterfaceInfo(
        name = "wlan0",
        kind = WifiInterfaceKind.WIFI,
        isUp = true,
        sysfsWireless = true,
    )

    @Test
    fun monitorUnavailableMakesReadinessUnavailable() {
        val result = PacketInjectionReadiness.assess(
            root = RootAccessStatus(RootState.GRANTED, "ok"),
            wifiInterfaces = listOf(wlan0),
            monitorMode = CapabilityStatus(CapabilityState.UNAVAILABLE, "no monitor"),
        )

        assertEquals(CapabilityState.UNAVAILABLE, result.state)
    }

    @Test
    fun promisingPrerequisitesStillDoNotClaimInjectionAvailable() {
        val result = PacketInjectionReadiness.assess(
            root = RootAccessStatus(RootState.GRANTED, "ok"),
            wifiInterfaces = listOf(wlan0),
            monitorMode = CapabilityStatus(CapabilityState.AVAILABLE, "monitor"),
        )

        assertEquals(CapabilityState.UNKNOWN, result.state)
        assertTrue(result.detail.contains("NOT verified"))
    }
}
