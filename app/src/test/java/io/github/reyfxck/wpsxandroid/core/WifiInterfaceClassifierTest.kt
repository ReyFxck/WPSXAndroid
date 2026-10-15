package io.github.reyfxck.wpsxandroid.core

import io.github.reyfxck.wpsxandroid.core.capability.WifiInterfaceClassifier
import io.github.reyfxck.wpsxandroid.core.capability.WifiInterfaceKind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WifiInterfaceClassifierTest {
    @Test
    fun classifiesCommonAndroidWifiInterfaces() {
        assertEquals(WifiInterfaceKind.WIFI, WifiInterfaceClassifier.classify("wlan0", false))
        assertEquals(WifiInterfaceKind.P2P, WifiInterfaceClassifier.classify("p2p0", false))
        assertEquals(WifiInterfaceKind.ACCESS_POINT, WifiInterfaceClassifier.classify("ap0", false))
    }

    @Test
    fun sysfsWirelessCanIdentifyNonStandardName() {
        assertEquals(WifiInterfaceKind.WIFI, WifiInterfaceClassifier.classify("mlan0", true))
    }

    @Test
    fun rejectsUnsafeOrUnrelatedNames() {
        assertNull(WifiInterfaceClassifier.classify("rmnet_data0", false))
        assertNull(WifiInterfaceClassifier.classify("../wlan0", true))
    }
}
