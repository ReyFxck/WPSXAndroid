package io.github.reyfxck.wpsxandroid.core.audit

import io.github.reyfxck.wpsxandroid.core.capability.CapabilityState
import io.github.reyfxck.wpsxandroid.core.capability.DeviceCapabilitySnapshot
import io.github.reyfxck.wpsxandroid.core.capability.RootState
import io.github.reyfxck.wpsxandroid.core.model.WifiNetwork

/**
 * Pinned references selected after comparing the currently maintained Pixiewps/Reaver forks.
 *
 * These revisions are documentation/provenance anchors only. The app does not bundle or execute
 * either third-party project yet.
 */
object SelectedAuditBackends {
    const val PIXIEWPS_REPOSITORY = "kimocoder/pixiewps"
    const val PIXIEWPS_REVISION = "8e8062eda97b0309e3f6baaa90eb6fa33423c37c"
    const val PIXIEWPS_LICENSE = "GPL-3.0"

    const val REAVER_REPOSITORY = "t6x/reaver-wps-fork-t6x"
    const val REAVER_REVISION = "f9b5e150f913a23d34c8098d598b90aaa08261cc"
    const val REAVER_LICENSE_NOTE = "GPLv2-era project with bundled dual-licensed components; review before redistribution"
}

enum class ReadinessLevel {
    PASS,
    WARNING,
    BLOCKED,
}

data class ReadinessCheck(
    val name: String,
    val level: ReadinessLevel,
    val detail: String,
)

data class PixieReadinessReport(
    val canProceedToLabBackend: Boolean,
    val summary: String,
    val checks: List<ReadinessCheck>,
)

/**
 * Read-only readiness evaluator for the future Pixie Dust backend.
 *
 * It never transmits frames, changes interface mode, invokes Reaver/Pixiewps, or attempts to
 * recover a PIN/passphrase. It only combines already-known AP metadata with device capabilities.
 */
object PixieReadinessEvaluator {
    fun evaluate(
        network: WifiNetwork,
        snapshot: DeviceCapabilitySnapshot?,
    ): PixieReadinessReport {
        val checks = mutableListOf<ReadinessCheck>()

        checks += if (network.wpsAdvertised) {
            ReadinessCheck("WPS", ReadinessLevel.PASS, "Target advertises WPS in scan metadata.")
        } else {
            ReadinessCheck("WPS", ReadinessLevel.BLOCKED, "Target does not advertise WPS in the available scan metadata.")
        }

        checks += when (network.wpsInfo?.apSetupLocked) {
            true -> ReadinessCheck("WPS lock", ReadinessLevel.BLOCKED, "AP reports WPS setup locked.")
            false -> ReadinessCheck("WPS lock", ReadinessLevel.PASS, "AP reports WPS setup unlocked.")
            null -> ReadinessCheck("WPS lock", ReadinessLevel.WARNING, "Lock state is not exposed by this scan/AP.")
        }

        val root = snapshot?.root
        checks += when (root?.state) {
            RootState.GRANTED -> ReadinessCheck("Root", ReadinessLevel.PASS, root.detail)
            RootState.SU_BINARY_FOUND -> ReadinessCheck("Root", ReadinessLevel.WARNING, "su exists but root has not been verified yet.")
            RootState.NOT_CHECKED, null -> ReadinessCheck("Root", ReadinessLevel.WARNING, "Root has not been verified.")
            RootState.NOT_DETECTED -> ReadinessCheck("Root", ReadinessLevel.BLOCKED, root.detail)
            RootState.DENIED_OR_FAILED -> ReadinessCheck("Root", ReadinessLevel.BLOCKED, root.detail)
        }

        val interfaces = snapshot?.wifiInterfaces.orEmpty()
        checks += if (interfaces.isNotEmpty()) {
            ReadinessCheck(
                "Wi-Fi interface",
                ReadinessLevel.PASS,
                interfaces.joinToString { it.name },
            )
        } else {
            ReadinessCheck("Wi-Fi interface", ReadinessLevel.BLOCKED, "No Wi-Fi interface is visible to the capability layer.")
        }

        checks += capabilityCheck("Monitor mode", snapshot?.monitorMode?.state, snapshot?.monitorMode?.detail)
        checks += capabilityCheck("Packet injection", snapshot?.packetInjection?.state, snapshot?.packetInjection?.detail)

        checks += ReadinessCheck(
            "Pixie engine",
            ReadinessLevel.WARNING,
            "Selected reference: ${SelectedAuditBackends.PIXIEWPS_REPOSITORY}@${SelectedAuditBackends.PIXIEWPS_REVISION.take(12)}; not bundled in this build.",
        )
        checks += ReadinessCheck(
            "WPS transport",
            ReadinessLevel.WARNING,
            "Selected reference: ${SelectedAuditBackends.REAVER_REPOSITORY}@${SelectedAuditBackends.REAVER_REVISION.take(12)}; not bundled in this build.",
        )

        val canProceed = network.wpsAdvertised &&
            network.wpsInfo?.apSetupLocked != true &&
            root?.state == RootState.GRANTED &&
            interfaces.isNotEmpty() &&
            snapshot?.monitorMode?.state == CapabilityState.AVAILABLE &&
            snapshot?.packetInjection?.state == CapabilityState.AVAILABLE

        return PixieReadinessReport(
            canProceedToLabBackend = canProceed,
            summary = if (canProceed) {
                "Hardware prerequisites look ready for a controlled-lab backend once the pinned executables are reproducibly bundled."
            } else {
                "One or more prerequisites are missing or unknown. Review the checks below before any controlled-lab backend work."
            },
            checks = checks,
        )
    }

    private fun capabilityCheck(
        name: String,
        state: CapabilityState?,
        detail: String?,
    ): ReadinessCheck = when (state) {
        CapabilityState.AVAILABLE -> ReadinessCheck(name, ReadinessLevel.PASS, detail.orEmpty())
        CapabilityState.UNAVAILABLE -> ReadinessCheck(name, ReadinessLevel.BLOCKED, detail.orEmpty())
        CapabilityState.UNKNOWN, null -> ReadinessCheck(name, ReadinessLevel.WARNING, detail ?: "Capability is unknown.")
    }
}
