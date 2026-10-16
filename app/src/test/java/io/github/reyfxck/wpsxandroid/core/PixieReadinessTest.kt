package io.github.reyfxck.wpsxandroid.core

import io.github.reyfxck.wpsxandroid.core.audit.PixieReadinessEvaluator
import io.github.reyfxck.wpsxandroid.core.audit.ReadinessLevel
import io.github.reyfxck.wpsxandroid.core.capability.CapabilityState
import io.github.reyfxck.wpsxandroid.core.capability.CapabilityStatus
import io.github.reyfxck.wpsxandroid.core.capability.DeviceCapabilitySnapshot
import io.github.reyfxck.wpsxandroid.core.capability.RootAccessStatus
import io.github.reyfxck.wpsxandroid.core.capability.RootState
import io.github.reyfxck.wpsxandroid.core.capability.WifiInterfaceInfo
import io.github.reyfxck.wpsxandroid.core.capability.WifiInterfaceKind
import io.github.reyfxck.wpsxandroid.core.model.WifiNetwork
import io.github.reyfxck.wpsxandroid.core.model.WifiSecurity
import io.github.reyfxck.wpsxandroid.core.wps.WpsBeaconInfo
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PixieReadinessTest {
    @Test
    fun `ready hardware passes controlled lab readiness`() {
        val report = PixieReadinessEvaluator.evaluate(
            network = network(wps = true, locked = false),
            snapshot = readySnapshot(),
        )

        assertTrue(report.canProceedToLabBackend)
        assertTrue(report.checks.any { it.name == "Root" && it.level == ReadinessLevel.PASS })
        assertTrue(report.checks.any { it.name == "Monitor mode" && it.level == ReadinessLevel.PASS })
    }

    @Test
    fun `locked WPS blocks readiness`() {
        val report = PixieReadinessEvaluator.evaluate(
            network = network(wps = true, locked = true),
            snapshot = readySnapshot(),
        )

        assertFalse(report.canProceedToLabBackend)
        assertTrue(report.checks.any { it.name == "WPS lock" && it.level == ReadinessLevel.BLOCKED })
    }

    @Test
    fun `missing WPS and capabilities block readiness`() {
        val report = PixieReadinessEvaluator.evaluate(
            network = network(wps = false, locked = null),
            snapshot = null,
        )

        assertFalse(report.canProceedToLabBackend)
        assertTrue(report.checks.any { it.name == "WPS" && it.level == ReadinessLevel.BLOCKED })
        assertTrue(report.checks.any { it.name == "Root" && it.level == ReadinessLevel.WARNING })
    }

    private fun network(wps: Boolean, locked: Boolean?) = WifiNetwork(
        ssid = "Lab AP",
        bssid = "00:11:22:33:44:55",
        rssiDbm = -45,
        frequencyMhz = 2412,
        channel = 1,
        capabilities = if (wps) "[WPA2-PSK-CCMP][WPS]" else "[WPA2-PSK-CCMP]",
        securityModes = setOf(WifiSecurity.WPA2),
        wpsAdvertised = wps,
        wpsInfo = if (wps) WpsBeaconInfo(apSetupLocked = locked) else null,
    )

    private fun readySnapshot() = DeviceCapabilitySnapshot(
        root = RootAccessStatus(RootState.GRANTED, "uid=0 verified"),
        wifiInterfaces = listOf(
            WifiInterfaceInfo(
                name = "wlan0",
                kind = WifiInterfaceKind.WIFI,
                isUp = true,
                sysfsWireless = true,
            ),
        ),
        monitorMode = CapabilityStatus(CapabilityState.AVAILABLE, "monitor mode advertised"),
        packetInjection = CapabilityStatus(CapabilityState.AVAILABLE, "readiness prerequisites met"),
    )
}
