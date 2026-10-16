package io.github.reyfxck.wpsxandroid.core.wps

data class WpsBeaconInfo(
    val version: String? = null,
    val version2: String? = null,
    val apSetupLocked: Boolean? = null,
    val manufacturer: String? = null,
    val modelName: String? = null,
    val modelNumber: String? = null,
    val deviceName: String? = null,
) {
    val effectiveVersion: String?
        get() = version2 ?: version
}
