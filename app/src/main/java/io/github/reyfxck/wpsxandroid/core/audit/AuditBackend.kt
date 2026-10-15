package io.github.reyfxck.wpsxandroid.core.audit

/**
 * Boundary for active authorized-audit engines such as Pixie Dust or Reaver.
 *
 * UI code must never assume a backend is usable. Hardware, driver, kernel,
 * root, monitor-mode and injection requirements are exposed as capabilities.
 */
interface AuditBackend {
    val id: String
    val displayName: String

    suspend fun capabilities(): AuditCapabilities
}

data class AuditCapabilities(
    val available: Boolean,
    val rootAvailable: Boolean,
    val monitorModeAvailable: Boolean,
    val injectionAvailable: Boolean,
    val notes: List<String> = emptyList(),
)
