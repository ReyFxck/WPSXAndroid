package io.github.reyfxck.wpsxandroid.core.wps

data class WpsBeaconInfo(
    val version: String? = null,
    val version2: String? = null,
    val apSetupLocked: Boolean? = null,
    val setupState: Int? = null,
    val manufacturer: String? = null,
    val modelName: String? = null,
    val modelNumber: String? = null,
    val serialNumber: String? = null,
    val deviceName: String? = null,
    val configMethodsMask: Int? = null,
    val configMethods: List<String> = emptyList(),
) {
    val effectiveVersion: String?
        get() = version2 ?: version

    val setupStateLabel: String?
        get() = when (setupState) {
            1 -> "Not configured"
            2 -> "Configured"
            null -> null
            else -> "Unknown"
        }
}
