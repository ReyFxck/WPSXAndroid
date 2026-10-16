package io.github.reyfxck.wpsxandroid.core.capability

/**
 * Describes how a device exposes monitor mode. The app must select by observed
 * capability instead of assuming every driver creates wlan0mon.
 */
enum class MonitorInterfaceStrategy {
    EXISTING_MONITOR_INTERFACE,
    SAME_INTERFACE,
    DEDICATED_INTERFACE,
    VENDOR_DRIVER_PARAMETER,
    UNKNOWN,
}

data class MonitorBackendCandidate(
    val id: String,
    val displayName: String,
    val state: CapabilityState,
    val strategy: MonitorInterfaceStrategy,
    val priority: Int,
    val detail: String,
)

object MonitorBackendSelector {
    /**
     * Prefer a verified available backend. If none are available, return the
     * strongest unknown candidate so the UI can explain what still needs to be
     * verified instead of collapsing to a generic unsupported result.
     */
    fun select(candidates: List<MonitorBackendCandidate>): MonitorBackendCandidate? {
        if (candidates.isEmpty()) return null

        return candidates
            .filter { it.state == CapabilityState.AVAILABLE }
            .minByOrNull { it.priority }
            ?: candidates
                .filter { it.state == CapabilityState.UNKNOWN }
                .minByOrNull { it.priority }
            ?: candidates.minByOrNull { it.priority }
    }
}
