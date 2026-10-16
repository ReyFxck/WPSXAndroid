package io.github.reyfxck.wpsxandroid.platform.wifi

import io.github.reyfxck.wpsxandroid.core.capability.CapabilityState
import io.github.reyfxck.wpsxandroid.core.capability.CapabilityStatus
import io.github.reyfxck.wpsxandroid.core.capability.IwDevParser
import io.github.reyfxck.wpsxandroid.core.capability.IwPhyParser
import io.github.reyfxck.wpsxandroid.core.capability.MonitorBackendClassifier
import io.github.reyfxck.wpsxandroid.core.capability.MonitorBackendSelector
import io.github.reyfxck.wpsxandroid.core.process.ExecutionTrace
import io.github.reyfxck.wpsxandroid.core.process.StructuredExecutionResult
import io.github.reyfxck.wpsxandroid.platform.process.StructuredProcessRunner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Read-only monitor-mode capability probe.
 *
 * This class never changes interface type. It first checks whether Android or
 * the driver already exposes a monitor interface, then falls back to the
 * generic supported-interface-modes report. This avoids assuming every device
 * must expose monitor mode in the same way.
 */
class AndroidMonitorModeDetector(
    private val processRunner: StructuredProcessRunner = StructuredProcessRunner(),
) {
    private val executableCandidates = listOf(
        "iw",
        "/system/bin/iw",
        "/vendor/bin/iw",
        "/system/xbin/iw",
    )

    suspend fun inspect(rootVerified: Boolean = false): CapabilityStatus = withContext(Dispatchers.IO) {
        val devExecution = runIw(
            label = "iw-dev",
            arguments = listOf("dev"),
            rootVerified = rootVerified,
        )
        val existingMonitorInterfaces = if (devExecution.trace.exitCode == 0) {
            IwDevParser.monitorInterfaces(devExecution.output)
        } else {
            emptyList()
        }

        val phyExecution = runIw(
            label = "iw-phy",
            arguments = listOf("phy"),
            rootVerified = rootVerified,
        )
        val genericMonitorAdvertised = if (phyExecution.trace.exitCode == 0) {
            IwPhyParser.reportsMonitorMode(phyExecution.output)
        } else {
            null
        }

        val candidates = MonitorBackendClassifier.classify(
            existingMonitorInterfaces = existingMonitorInterfaces,
            genericMonitorAdvertised = genericMonitorAdvertised,
        )
        val selected = MonitorBackendSelector.select(candidates)

        val traces = "${traceSummary(devExecution.trace)}; ${traceSummary(phyExecution.trace)}"
        if (selected == null) {
            return@withContext CapabilityStatus(
                state = CapabilityState.UNKNOWN,
                detail = "No monitor-mode backend evidence could be classified. $traces",
            )
        }

        CapabilityStatus(
            state = selected.state,
            detail = "Backend: ${selected.displayName}; strategy: ${selected.strategy.name}. ${selected.detail} $traces",
        )
    }

    private suspend fun runIw(
        label: String,
        arguments: List<String>,
        rootVerified: Boolean,
    ): StructuredExecutionResult {
        val direct = processRunner.runFirstSuccessful(
            label = label,
            executableCandidates = executableCandidates,
            arguments = arguments,
            timeoutMillis = 5_000,
            maxOutputChars = 65_536,
        )
        if (direct.trace.exitCode == 0 || !rootVerified) return direct

        val fixedCommands = executableCandidates.map { executable ->
            (listOf(executable) + arguments).joinToString(" ")
        }

        var last = direct
        fixedCommands.forEach { command ->
            val rooted = processRunner.runFirstSuccessful(
                label = "root-$label",
                executableCandidates = listOf("su"),
                arguments = listOf("-c", command),
                timeoutMillis = 5_000,
                maxOutputChars = 65_536,
            )
            last = rooted
            if (rooted.trace.exitCode == 0) return rooted
        }
        return last
    }

    private fun traceSummary(trace: ExecutionTrace): String = buildString {
        append("Probe ")
        append(trace.state.name)
        trace.executable?.let { append(" via $it") }
        trace.exitCode?.let { append(" (exit $it)") }
        append(" in ${trace.durationMs} ms")
        append("; ${trace.detail}")
    }
}
