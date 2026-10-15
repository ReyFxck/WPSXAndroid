package io.github.reyfxck.wpsxandroid.core.wifi

import kotlin.math.pow

/**
 * Estimates AP distance from RSSI using a log-distance path-loss model.
 *
 * This is intentionally presented as an approximation: Android scan results do not expose
 * enough information to calculate physical distance accurately because AP transmit power,
 * antenna gain, obstacles and reflections are unknown.
 */
fun estimateDistanceMeters(
    rssiDbm: Int,
    referenceRssiAtOneMeter: Int = -40,
    pathLossExponent: Double = 2.7,
): Double {
    require(pathLossExponent > 0.0) { "pathLossExponent must be positive" }

    val exponent = (referenceRssiAtOneMeter - rssiDbm) / (10.0 * pathLossExponent)
    return 10.0.pow(exponent).coerceIn(0.1, 999.0)
}
