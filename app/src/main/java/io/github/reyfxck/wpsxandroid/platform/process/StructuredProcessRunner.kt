package io.github.reyfxck.wpsxandroid.platform.process

import io.github.reyfxck.wpsxandroid.core.process.ExecutionState
import io.github.reyfxck.wpsxandroid.core.process.ExecutionTrace
import io.github.reyfxck.wpsxandroid.core.process.ProcessArgumentPolicy
import io.github.reyfxck.wpsxandroid.core.process.StructuredExecutionResult
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Executes a fixed argv vector without a shell and records a bounded,
 * structured execution trace.
 */
class StructuredProcessRunner {
    suspend fun runFirstSuccessful(
        label: String,
        executableCandidates: List<String>,
        arguments: List<String>,
        timeoutMillis: Long,
        maxOutputChars: Int = 65_536,
        successExitCodes: Set<Int> = setOf(0),
    ): StructuredExecutionResult = withContext(Dispatchers.IO) {
        require(label.isNotBlank()) { "Execution label must not be blank" }
        require(executableCandidates.isNotEmpty()) { "At least one executable candidate is required" }
        require(executableCandidates.all(ProcessArgumentPolicy::validExecutable)) { "Invalid executable candidate" }
        require(arguments.all(ProcessArgumentPolicy::validArgument)) { "Arguments must not contain NUL characters" }
        require(timeoutMillis in 1..60_000) { "Timeout must be between 1 ms and 60 seconds" }
        require(maxOutputChars in 1..262_144) { "Output cap must be between 1 and 262144 characters" }

        var lastResult: StructuredExecutionResult? = null
        var lastStartError: Throwable? = null

        for (executable in executableCandidates.distinct()) {
            val startedAt = System.nanoTime()
            val process = runCatching {
                ProcessBuilder(listOf(executable) + arguments)
                    .redirectErrorStream(true)
                    .start()
            }.getOrElse { error ->
                lastStartError = error
                return@getOrElse null
            } ?: continue

            val outputFuture = CompletableFuture.supplyAsync {
                process.inputStream.bufferedReader().use { reader ->
                    val buffer = CharArray(4096)
                    val kept = StringBuilder()
                    while (true) {
                        val count = reader.read(buffer)
                        if (count < 0) break
                        if (kept.length < maxOutputChars) {
                            val remaining = maxOutputChars - kept.length
                            kept.append(buffer, 0, minOf(count, remaining))
                        }
                    }
                    kept.toString()
                }
            }

            val finished = runCatching {
                process.waitFor(timeoutMillis, TimeUnit.MILLISECONDS)
            }.getOrDefault(false)

            if (!finished) {
                process.destroyForcibly()
                runCatching { process.waitFor(1, TimeUnit.SECONDS) }
                val output = runCatching { outputFuture.get(1, TimeUnit.SECONDS) }.getOrDefault("")
                return@withContext StructuredExecutionResult(
                    trace = ExecutionTrace(
                        label = label,
                        executable = executable,
                        state = ExecutionState.TIMED_OUT,
                        exitCode = null,
                        durationMs = elapsedMs(startedAt),
                        outputChars = output.length,
                        detail = "Process exceeded ${timeoutMillis} ms and was terminated.",
                    ),
                    output = output,
                )
            }

            val output = runCatching { outputFuture.get(1, TimeUnit.SECONDS) }.getOrDefault("")
            val exitCode = process.exitValue()
            val result = StructuredExecutionResult(
                trace = ExecutionTrace(
                    label = label,
                    executable = executable,
                    state = ExecutionState.COMPLETED,
                    exitCode = exitCode,
                    durationMs = elapsedMs(startedAt),
                    outputChars = output.length,
                    detail = if (exitCode in successExitCodes) {
                        "Process completed with an accepted exit code."
                    } else {
                        "Process completed but returned a non-success exit code."
                    },
                ),
                output = output,
            )

            lastResult = result
            if (exitCode in successExitCodes) return@withContext result
        }

        lastResult ?: StructuredExecutionResult(
            trace = ExecutionTrace(
                label = label,
                executable = null,
                state = ExecutionState.START_FAILED,
                exitCode = null,
                durationMs = 0,
                outputChars = 0,
                detail = "No executable candidate could be started" +
                    (lastStartError?.let { ": ${it.javaClass.simpleName}" } ?: "."),
            ),
            output = "",
        )
    }

    private fun elapsedMs(startedAt: Long): Long =
        TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt)
}
