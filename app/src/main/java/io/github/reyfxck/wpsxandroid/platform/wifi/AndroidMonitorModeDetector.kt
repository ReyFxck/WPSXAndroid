package io.github.reyfxck.wpsxandroid.platform.wifi

import io.github.reyfxck.wpsxandroid.core.capability.CapabilityState
import io.github.reyfxck.wpsxandroid.core.capability.CapabilityStatus
import io.github.reyfxck.wpsxandroid.core.capability.IwPhyParser
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Read-only monitor-mode capability probe.
 *
 * This class never changes interface type. It only asks `iw phy` which
 * interface modes the driver reports as supported.
 */
class AndroidMonitorModeDetector {
    private val commands = listOf(
        listOf("iw", "phy"),
        listOf("/system/bin/iw", "phy"),
        listOf("/vendor/bin/iw", "phy"),
        listOf("/system/xbin/iw", "phy"),
    )

    suspend fun inspect(): CapabilityStatus = withContext(Dispatchers.IO) {
        var startedAny = false

        for (command in commands) {
            val process = runCatching {
                ProcessBuilder(command)
                    .redirectErrorStream(true)
                    .start()
            }.getOrNull() ?: continue

            startedAny = true
            try {
                val finished = process.waitFor(5, TimeUnit.SECONDS)
                if (!finished) {
                    process.destroyForcibly()
                    continue
                }

                val output = process.inputStream.bufferedReader().use { reader ->
                    reader.readText().take(65_536)
                }
                if (process.exitValue() != 0) continue

                return@withContext when (IwPhyParser.reportsMonitorMode(output)) {
                    true -> CapabilityStatus(
                        state = CapabilityState.AVAILABLE,
                        detail = "Driver reports monitor as a supported interface mode via read-only `iw phy`.",
                    )
                    false -> CapabilityStatus(
                        state = CapabilityState.UNAVAILABLE,
                        detail = "`iw phy` reported supported interface modes but did not include monitor.",
                    )
                    null -> CapabilityStatus(
                        state = CapabilityState.UNKNOWN,
                        detail = "`iw phy` ran, but its output did not contain a parseable supported-interface-modes section.",
                    )
                }
            } finally {
                if (process.isAlive) process.destroyForcibly()
            }
        }

        CapabilityStatus(
            state = CapabilityState.UNKNOWN,
            detail = if (startedAny) {
                "An `iw` command was found but did not return usable capability data."
            } else {
                "`iw` is not visible to the app, so monitor-mode support cannot be determined yet."
            },
        )
    }
}
