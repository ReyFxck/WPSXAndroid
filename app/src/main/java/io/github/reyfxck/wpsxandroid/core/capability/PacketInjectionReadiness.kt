package io.github.reyfxck.wpsxandroid.core.capability

/**
 * Read-only assessment of prerequisites for a future packet-injection test.
 *
 * This never reports injection as AVAILABLE because no frame is transmitted.
 */
object PacketInjectionReadiness {
    fun assess(
        root: RootAccessStatus,
        wifiInterfaces: List<WifiInterfaceInfo>,
        monitorMode: CapabilityStatus,
    ): CapabilityStatus {
        if (monitorMode.state == CapabilityState.UNAVAILABLE) {
            return CapabilityStatus(
                state = CapabilityState.UNAVAILABLE,
                detail = "Injection readiness failed because the driver did not report monitor-mode support.",
            )
        }

        if (wifiInterfaces.isEmpty()) {
            return CapabilityStatus(
                state = CapabilityState.UNKNOWN,
                detail = "No Wi-Fi interface is visible to the app, so injection readiness cannot be assessed.",
            )
        }

        if (monitorMode.state != CapabilityState.AVAILABLE) {
            return CapabilityStatus(
                state = CapabilityState.UNKNOWN,
                detail = "Monitor-mode support is unresolved. Injection cannot be inferred until that prerequisite is known.",
            )
        }

        return if (root.state == RootState.GRANTED) {
            CapabilityStatus(
                state = CapabilityState.UNKNOWN,
                detail = "Read-only prerequisites look promising: root is verified, a Wi-Fi interface is visible, and the driver advertises monitor mode. Actual packet injection is NOT verified because no frame was transmitted.",
            )
        } else {
            CapabilityStatus(
                state = CapabilityState.UNKNOWN,
                detail = "The driver advertises monitor mode, but root is not verified. Actual packet injection remains untested.",
            )
        }
    }
}
