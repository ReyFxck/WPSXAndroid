package io.github.reyfxck.wpsxandroid.core.process

data class ExecutionTrace(
    val label: String,
    val executable: String?,
    val state: ExecutionState,
    val exitCode: Int?,
    val durationMs: Long,
    val outputChars: Int,
    val detail: String,
)

enum class ExecutionState {
    COMPLETED,
    TIMED_OUT,
    START_FAILED,
}

data class StructuredExecutionResult(
    val trace: ExecutionTrace,
    val output: String,
)

/**
 * Validation for direct ProcessBuilder arguments.
 *
 * Shell punctuation is intentionally allowed because ProcessBuilder receives
 * an argument vector and does not invoke a shell. NUL characters are rejected
 * because they are invalid in native argv strings.
 */
object ProcessArgumentPolicy {
    fun validExecutable(value: String): Boolean = value.isNotBlank() && '\u0000' !in value

    fun validArgument(value: String): Boolean = '\u0000' !in value
}
