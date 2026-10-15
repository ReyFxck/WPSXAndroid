package io.github.reyfxck.wpsxandroid.platform.wifi

import io.github.reyfxck.wpsxandroid.core.capability.CapabilityState
import io.github.reyfxck.wpsxandroid.core.capability.CapabilityStatus
import io.github.reyfxck.wpsxandroid.core.capability.IwPhyParser
import io.github.reyfxck.wpsxandroid.core.process.ExecutionTrace
import io.github.reyfxck.wpsxandroid.platform.process.StructuredProcessRunner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Read-only monitor-mode capability probe.
 *
 * This class never changes interface type. It only asks `iw phy` which
 * interface modes the driver reports as supported.
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

    suspend fun inspect(): CapabilityStatus = withContext(Dispatchers.IO) {
        val execution = processRunner.runFirstSuccessful(
            label = "iw-phy",
            executableCandidates = executableCandidates,
            arguments = listOf("phy"),
            timeoutMillis = 5_000,
            maxOutputChars = 65_536,
        )
        val trace = execution.trace
        val traceText = traceSummary(trace)

        if (trace.exitCode != 0) {
            return@withContext CapabilityStatus(
                state = CapabilityState.UNKNOWN,
                detail = "Could not obtain usable read-only `iw phy` data. $traceText",
            )
        }

        when (IwPhyParser.reportsMonitorMode(execution.output)) {
            true -> CapabilityStatus(
                state = CapabilityState.AVAILABLE,
                detail = "Driver reports monitor as a supported interface mode via read-only `iw phy`. $traceText",
            )
            false -> CapabilityStatus(
                state = CapabilityState.UNAVAILABLE,
                detail = "`iw phy` reported supported interface modes but did not include monitor. $traceText",
            )
            null -> CapabilityStatus(
                state = CapabilityState.UNKNOWN,
                detail = "`iw phy` ran, but its output did not contain a parseable supported-interface-modes section. $traceText",
            )
        }
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
