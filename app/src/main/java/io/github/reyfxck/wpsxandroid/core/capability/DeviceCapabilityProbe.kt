package io.github.reyfxck.wpsxandroid.core.capability

interface DeviceCapabilityProbe {
    suspend fun inspectPassive(): DeviceCapabilitySnapshot
    suspend fun verifyRoot(): DeviceCapabilitySnapshot
}

data class DeviceCapabilitySnapshot(
    val root: RootAccessStatus,
    val wifiInterfaces: List<WifiInterfaceInfo>,
    val monitorMode: CapabilityStatus,
    val packetInjection: CapabilityStatus,
)

data class RootAccessStatus(
    val state: RootState,
    val detail: String,
)

enum class RootState {
    NOT_CHECKED,
    NOT_DETECTED,
    SU_BINARY_FOUND,
    GRANTED,
    DENIED_OR_FAILED,
}

data class WifiInterfaceInfo(
    val name: String,
    val kind: WifiInterfaceKind,
    val isUp: Boolean,
    val sysfsWireless: Boolean,
)

enum class WifiInterfaceKind {
    WIFI,
    P2P,
    ACCESS_POINT,
}

data class CapabilityStatus(
    val state: CapabilityState,
    val detail: String,
)

enum class CapabilityState {
    UNKNOWN,
    AVAILABLE,
    UNAVAILABLE,
}
