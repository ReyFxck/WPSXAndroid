package io.github.reyfxck.wpsxandroid.core.wifi

import io.github.reyfxck.wpsxandroid.core.model.WifiNetwork

enum class WifiBand(val label: String) {
    GHZ_2_4("2.4 GHz"),
    GHZ_5("5 GHz"),
    GHZ_6("6 GHz"),
    GHZ_60("60 GHz"),
    UNKNOWN("Unknown band"),
}

data class ChannelUsage(
    val band: WifiBand,
    val channel: Int,
    val accessPointCount: Int,
)

fun wifiBandForFrequency(frequencyMhz: Int): WifiBand = when (frequencyMhz) {
    in 2_400..2_500 -> WifiBand.GHZ_2_4
    in 4_900..5_895 -> WifiBand.GHZ_5
    in 5_925..7_125 -> WifiBand.GHZ_6
    in 57_000..71_000 -> WifiBand.GHZ_60
    else -> WifiBand.UNKNOWN
}

fun buildChannelUsage(networks: List<WifiNetwork>): List<ChannelUsage> =
    networks
        .mapNotNull { network ->
            network.channel?.let { channel -> wifiBandForFrequency(network.frequencyMhz) to channel }
        }
        .groupingBy { it }
        .eachCount()
        .map { (key, count) ->
            ChannelUsage(
                band = key.first,
                channel = key.second,
                accessPointCount = count,
            )
        }
        .sortedWith(
            compareBy<ChannelUsage>({ it.band.ordinal }, { it.channel }),
        )
