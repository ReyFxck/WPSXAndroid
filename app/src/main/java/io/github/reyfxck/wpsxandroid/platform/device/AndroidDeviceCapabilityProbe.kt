package io.github.reyfxck.wpsxandroid.platform.device

import io.github.reyfxck.wpsxandroid.core.capability.CapabilityState
import io.github.reyfxck.wpsxandroid.core.capability.CapabilityStatus
import io.github.reyfxck.wpsxandroid.core.capability.DeviceCapabilityProbe
import io.github.reyfxck.wpsxandroid.core.capability.DeviceCapabilitySnapshot
import io.github.reyfxck.wpsxandroid.core.capability.RootAccessStatus
import io.github.reyfxck.wpsxandroid.platform.root.AndroidRootDetector
import io.github.reyfxck.wpsxandroid.platform.wifi.AndroidWifiInterfaceDetector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AndroidDeviceCapabilityProbe(
    private val rootDetector: AndroidRootDetector = AndroidRootDetector(),
    private val wifiInterfaceDetector: AndroidWifiInterfaceDetector = AndroidWifiInterfaceDetector(),
) : DeviceCapabilityProbe {

    override suspend fun inspectPassive(): DeviceCapabilitySnapshot = withContext(Dispatchers.IO) {
        snapshot(rootDetector.inspectPassive())
    }

    override suspend fun verifyRoot(): DeviceCapabilitySnapshot = withContext(Dispatchers.IO) {
        snapshot(rootDetector.verify())
    }

    private fun snapshot(root: RootAccessStatus): DeviceCapabilitySnapshot {
        val interfaces = wifiInterfaceDetector.discover()
        return DeviceCapabilitySnapshot(
            root = root,
            wifiInterfaces = interfaces,
            monitorMode = CapabilityStatus(
                state = CapabilityState.UNKNOWN,
                detail = "Not probed yet. Root or a visible Wi-Fi interface does not prove monitor-mode support.",
            ),
            packetInjection = CapabilityStatus(
                state = CapabilityState.UNKNOWN,
                detail = "Not probed yet. Injection must be verified separately for the kernel, driver, firmware, and interface.",
            ),
        )
    }
}
