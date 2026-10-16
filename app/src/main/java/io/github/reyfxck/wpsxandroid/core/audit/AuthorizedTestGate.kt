package io.github.reyfxck.wpsxandroid.core.audit

import io.github.reyfxck.wpsxandroid.core.model.WifiNetwork
import io.github.reyfxck.wpsxandroid.core.wps.BssidNormalizer

data class AuthorizedTargetCheck(
    val allowed: Boolean,
    val reason: String,
)

/**
 * Keeps the in-app active-test entry point scoped to an AP the device is already connected to.
 * This avoids turning the nearby-network list into a one-tap attack launcher.
 */
object AuthorizedTestGate {
    fun evaluate(network: WifiNetwork, connectedBssid: String?): AuthorizedTargetCheck {
        val target = BssidNormalizer.normalize(network.bssid)
            ?: return AuthorizedTargetCheck(false, "The selected AP BSSID is invalid.")
        val connected = connectedBssid?.let(BssidNormalizer::normalize)
            ?: return AuthorizedTargetCheck(
                false,
                "Connect this device to the AP first. Authorized test mode only unlocks for the currently connected network.",
            )

        if (target != connected) {
            return AuthorizedTargetCheck(
                false,
                "This AP is not the network currently connected on the device.",
            )
        }
        if (!network.wpsAdvertised) {
            return AuthorizedTargetCheck(false, "The selected AP does not advertise WPS in the available scan metadata.")
        }

        return AuthorizedTargetCheck(
            true,
            "Selected AP matches the device's current Wi-Fi connection and advertises WPS.",
        )
    }
}
