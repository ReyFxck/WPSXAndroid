package io.github.reyfxck.wpsxandroid.core.wifi

/** Converts common Wi-Fi center frequencies to IEEE 802.11 channel numbers. */
fun wifiChannelFromFrequency(frequencyMhz: Int): Int? = when {
    frequencyMhz == 2484 -> 14
    frequencyMhz in 2412..2472 && (frequencyMhz - 2407) % 5 == 0 ->
        (frequencyMhz - 2407) / 5

    frequencyMhz in 5000..5895 && (frequencyMhz - 5000) % 5 == 0 ->
        (frequencyMhz - 5000) / 5

    // 5935 MHz is the special 6 GHz channel 2 allocation.
    frequencyMhz == 5935 -> 2
    frequencyMhz in 5955..7115 && (frequencyMhz - 5950) % 5 == 0 ->
        (frequencyMhz - 5950) / 5

    // 802.11ad/ay 60 GHz channels.
    frequencyMhz in 58320..70200 && (frequencyMhz - 56160) % 2160 == 0 ->
        (frequencyMhz - 56160) / 2160

    else -> null
}
