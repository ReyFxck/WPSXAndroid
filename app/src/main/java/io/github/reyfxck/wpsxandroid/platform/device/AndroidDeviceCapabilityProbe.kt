package io.github.reyfxck.wpsxandroid.platform.device

import io.github.reyfxck.wpsxandroid.core.capability.DeviceCapabilityProbe
import io.github.reyfxck.wpsxandroid.core.capability.DeviceCapabilitySnapshot
import io.github.reyfxck.wpsxandroid.core.capability.PacketInjectionReadiness
import io.github.reyfxck.wpsxandroid.core.capability.RootAccessStatus
import io.github.reyfxck.wpsxandroid.platform.root.AndroidRootDetector
import io.github.reyfxck.wpsxandroid.platform.wifi.AndroidMonitorModeDetector
import io.github.reyfxck.wpsxandroid.platform.wifi.AndroidWifiInterfaceDetector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AndroidDeviceCapabilityProbe(
    private val rootDetector: AndroidRootDetector = AndroidRootDetector(),
    private val wifiInterfaceDetector: AndroidWifiInterfaceDetector = AndroidWifiInterfaceDetector(),
    private val monitorModeDetector: AndroidMonitorModeDetector = AndroidMonitorModeDetector(),
) : DeviceCapabilityProbe {

    override suspend fun inspectPassive(): DeviceCapabilitySnapshot = withContext(Dispatchers.IO) {
        snapshot(rootDetector.inspectPassive())
    }

    override suspend fun verifyRoot(): DeviceCapabilitySnapshot = withContext(Dispatchers.IO) {
        snapshot(rootDetector.verify())
    }

    private suspend fun snapshot(root: RootAccessStatus): DeviceCapabilitySnapshot {
        val interfaces = wifiInterfaceDetector.discover()
        val monitorMode = monitorModeDetector.inspect()
        return DeviceCapabilitySnapshot(
            root = root,
            wifiInterfaces = interfaces,
            monitorMode = monitorMode,
            packetInjection = PacketInjectionReadiness.assess(
                root = root,
                wifiInterfaces = interfaces,
                monitorMode = monitorMode,
            ),
        )
    }
}
