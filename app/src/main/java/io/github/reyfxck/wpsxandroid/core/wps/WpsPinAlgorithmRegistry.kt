package io.github.reyfxck.wpsxandroid.core.wps

/**
 * Registry/dispatcher for deterministic offline WPS PIN algorithms.
 *
 * The registry owns input normalization so individual algorithms receive a
 * stable canonical BSSID representation.
 */
class WpsPinAlgorithmRegistry(
    algorithms: List<WpsPinAlgorithm>,
) {
    private val algorithms = algorithms.toList()

    init {
        require(this.algorithms.none { it.id.isBlank() }) {
            "WPS algorithm IDs must not be blank"
        }
        require(this.algorithms.map { it.id }.distinct().size == this.algorithms.size) {
            "WPS algorithm IDs must be unique"
        }
    }

    val registeredIds: List<String>
        get() = algorithms.map { it.id }

    fun findById(id: String): WpsPinAlgorithm? = algorithms.firstOrNull { it.id == id }

    fun derive(input: WpsPinInput): List<WpsPinCandidate> {
        val normalizedBssid = BssidNormalizer.normalize(input.bssid) ?: return emptyList()
        val normalizedInput = input.copy(
            bssid = normalizedBssid,
            ssid = input.ssid?.trim()?.takeIf { it.isNotEmpty() },
        )

        return algorithms
            .asSequence()
            .filter { it.supports(normalizedInput) }
            .flatMap { algorithm ->
                algorithm.derive(normalizedInput).asSequence().map { candidate ->
                    require(candidate.algorithmId == algorithm.id) {
                        "Algorithm ${algorithm.id} returned candidate for ${candidate.algorithmId}"
                    }
                    candidate
                }
            }
            .distinct()
            .toList()
    }
}
