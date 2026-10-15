package io.github.reyfxck.wpsxandroid.core.wifi

import io.github.reyfxck.wpsxandroid.core.model.WifiNetwork

interface WifiScanner {
    suspend fun scan(): ScanResult
}

sealed interface ScanResult {
    data class Success(
        val networks: List<WifiNetwork>,
        val isFresh: Boolean,
    ) : ScanResult

    data class PermissionRequired(val permissions: Set<String>) : ScanResult
    data class Unavailable(val reason: String) : ScanResult
}
