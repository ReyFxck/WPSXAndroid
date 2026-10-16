package io.github.reyfxck.wpsxandroid.core.audit

import io.github.reyfxck.wpsxandroid.core.model.WifiNetwork
import io.github.reyfxck.wpsxandroid.core.model.WifiSecurity
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthorizedTestGateTest {
    private val network = WifiNetwork(
        ssid = "Lab",
        bssid = "54:6c:ac:06:9a:34",
        rssiDbm = -45,
        frequencyMhz = 2412,
        channel = 1,
        capabilities = "[WPA2-PSK][WPS]",
        securityModes = setOf(WifiSecurity.WPA2),
        wpsAdvertised = true,
    )

    @Test
    fun allowsCurrentlyConnectedWpsAp() {
        assertTrue(AuthorizedTestGate.evaluate(network, "54:6C:AC:06:9A:34").allowed)
    }

    @Test
    fun blocksDifferentNearbyAp() {
        assertFalse(AuthorizedTestGate.evaluate(network, "00:11:22:33:44:55").allowed)
    }

    @Test
    fun blocksWhenConnectionIdentityIsUnavailable() {
        assertFalse(AuthorizedTestGate.evaluate(network, null).allowed)
    }
}
