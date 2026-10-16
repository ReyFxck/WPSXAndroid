package io.github.reyfxck.wpsxandroid.core.capability

/**
 * Pure classifier for read-only capability evidence collected by platform
 * adapters. It does not change radio state or execute privileged commands.
 */
object MonitorBackendClassifier {
    fun classify(
        existingMonitorInterfaces: List<String>,
        genericMonitorAdvertised: Boolean?,
        sameInterfaceHint: String? = null,
    ): List<MonitorBackendCandidate> = buildList {
        if (existingMonitorInterfaces.isNotEmpty()) {
            add(
                MonitorBackendCandidate(
                    id = "existing-monitor-interface",
                    displayName = "Existing monitor interface",
                    state = CapabilityState.AVAILABLE,
                    strategy = MonitorInterfaceStrategy.EXISTING_MONITOR_INTERFACE,
                    priority = 10,
                    detail = "Detected monitor interface(s): ${existingMonitorInterfaces.joinToString()}.",
                ),
            )
        }

        if (!sameInterfaceHint.isNullOrBlank()) {
            add(
                MonitorBackendCandidate(
                    id = "same-interface-hint",
                    displayName = "Same-interface monitor path",
                    state = CapabilityState.UNKNOWN,
                    strategy = MonitorInterfaceStrategy.SAME_INTERFACE,
                    priority = 20,
                    detail = sameInterfaceHint,
                ),
            )
        }

        add(
            when (genericMonitorAdvertised) {
                true -> MonitorBackendCandidate(
                    id = "generic-nl80211",
                    displayName = "Generic monitor support",
                    state = CapabilityState.AVAILABLE,
                    strategy = MonitorInterfaceStrategy.UNKNOWN,
                    priority = 30,
                    detail = "The driver advertises monitor support; the platform layer still has to determine how this device exposes it.",
                )
                false -> MonitorBackendCandidate(
                    id = "generic-nl80211",
                    displayName = "Generic monitor support",
                    state = CapabilityState.UNAVAILABLE,
                    strategy = MonitorInterfaceStrategy.UNKNOWN,
                    priority = 30,
                    detail = "The generic capability report did not advertise monitor support.",
                )
                null -> MonitorBackendCandidate(
                    id = "generic-nl80211",
                    displayName = "Generic monitor support",
                    state = CapabilityState.UNKNOWN,
                    strategy = MonitorInterfaceStrategy.UNKNOWN,
                    priority = 30,
                    detail = "Generic monitor capability could not be determined.",
                )
            },
        )
    }
}
