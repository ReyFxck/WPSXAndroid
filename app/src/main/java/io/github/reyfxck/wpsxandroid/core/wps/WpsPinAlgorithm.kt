package io.github.reyfxck.wpsxandroid.core.wps

/**
 * Contract for deterministic, offline WPS PIN derivation algorithms.
 *
 * Implementations must be backed by public documentation or independently
 * verifiable test vectors before being marked as verified in STATUS.md.
 */
interface WpsPinAlgorithm {
    val id: String
    val displayName: String

    fun supports(input: WpsPinInput): Boolean
    fun derive(input: WpsPinInput): List<WpsPinCandidate>
}

data class WpsPinInput(
    val bssid: String,
    val ssid: String? = null,
)

data class WpsPinCandidate(
    val pin: String,
    val algorithmId: String,
    val confidence: Confidence,
)

enum class Confidence {
    EXACT,
    HEURISTIC,
}
