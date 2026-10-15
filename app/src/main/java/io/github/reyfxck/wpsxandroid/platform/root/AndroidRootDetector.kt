package io.github.reyfxck.wpsxandroid.platform.root

import io.github.reyfxck.wpsxandroid.core.capability.RootAccessStatus
import io.github.reyfxck.wpsxandroid.core.capability.RootIdentityParser
import io.github.reyfxck.wpsxandroid.core.capability.RootState
import io.github.reyfxck.wpsxandroid.core.process.ExecutionTrace
import io.github.reyfxck.wpsxandroid.platform.process.StructuredProcessRunner
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AndroidRootDetector(
    private val processRunner: StructuredProcessRunner = StructuredProcessRunner(),
) {
    private val knownSuPaths = listOf(
        "/system/bin/su",
        "/system/xbin/su",
        "/sbin/su",
        "/su/bin/su",
        "/vendor/bin/su",
        "/product/bin/su",
    )

    fun inspectPassive(): RootAccessStatus {
        val found = knownSuPaths.firstOrNull { path -> runCatching { File(path).exists() }.getOrDefault(false) }
        return if (found != null) {
            RootAccessStatus(
                state = RootState.SU_BINARY_FOUND,
                detail = "Potential su binary found at $found. Root is not verified yet.",
            )
        } else {
            RootAccessStatus(
                state = RootState.NOT_DETECTED,
                detail = "No su binary was found in the app's visible common paths. This is not proof that the device is unrooted.",
            )
        }
    }

    suspend fun verify(): RootAccessStatus = withContext(Dispatchers.IO) {
        val execution = processRunner.runFirstSuccessful(
            label = "root-id",
            executableCandidates = listOf("su"),
            arguments = listOf("-c", "id"),
            timeoutMillis = 10_000,
            maxOutputChars = 4_096,
        )
        val trace = execution.trace
        val traceText = traceSummary(trace)

        if (
            trace.exitCode == 0 &&
            RootIdentityParser.isRootIdentity(execution.output)
        ) {
            RootAccessStatus(
                state = RootState.GRANTED,
                detail = "Root verified with the fixed `su -c id` check. $traceText",
            )
        } else {
            RootAccessStatus(
                state = RootState.DENIED_OR_FAILED,
                detail = "Root could not be verified. $traceText",
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
