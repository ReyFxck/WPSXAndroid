package io.github.reyfxck.wpsxandroid.core.wps

/**
 * Parses WPS data carried inside vendor-specific 802.11 information elements.
 *
 * Android exposes beacon information elements on API 30+. The vendor-specific WPS IE starts
 * with 00:50:F2:04, followed by big-endian WPS attributes.
 */
object WpsInformationElementParser {
    private val WPS_OUI_TYPE = byteArrayOf(0x00, 0x50, 0xF2.toByte(), 0x04)
    private val WFA_VENDOR_ID = byteArrayOf(0x00, 0x37, 0x2A)

    private const val ATTR_VERSION = 0x104A
    private const val ATTR_VENDOR_EXTENSION = 0x1049
    private const val ATTR_AP_SETUP_LOCKED = 0x1057
    private const val ATTR_MANUFACTURER = 0x1021
    private const val ATTR_MODEL_NAME = 0x1023
    private const val ATTR_MODEL_NUMBER = 0x1024
    private const val ATTR_DEVICE_NAME = 0x1011

    fun parseVendorSpecificElements(elements: List<ByteArray>): WpsBeaconInfo? {
        var merged: WpsBeaconInfo? = null
        elements.forEach { body ->
            val parsed = parseVendorSpecificElement(body) ?: return@forEach
            merged = merge(merged, parsed)
        }
        return merged
    }

    fun parseVendorSpecificElement(body: ByteArray): WpsBeaconInfo? {
        if (body.size < WPS_OUI_TYPE.size || !body.startsWith(WPS_OUI_TYPE)) return null

        var offset = WPS_OUI_TYPE.size
        var version: String? = null
        var version2: String? = null
        var apSetupLocked: Boolean? = null
        var manufacturer: String? = null
        var modelName: String? = null
        var modelNumber: String? = null
        var deviceName: String? = null

        while (offset + 4 <= body.size) {
            val type = u16(body, offset)
            val length = u16(body, offset + 2)
            offset += 4
            if (length < 0 || offset + length > body.size) break

            val value = body.copyOfRange(offset, offset + length)
            when (type) {
                ATTR_VERSION -> if (value.isNotEmpty()) version = formatVersion(value[0])
                ATTR_AP_SETUP_LOCKED -> if (value.isNotEmpty()) apSetupLocked = value[0].toInt() != 0
                ATTR_MANUFACTURER -> manufacturer = ascii(value)
                ATTR_MODEL_NAME -> modelName = ascii(value)
                ATTR_MODEL_NUMBER -> modelNumber = ascii(value)
                ATTR_DEVICE_NAME -> deviceName = ascii(value)
                ATTR_VENDOR_EXTENSION -> version2 = parseVersion2(value) ?: version2
            }
            offset += length
        }

        return WpsBeaconInfo(
            version = version,
            version2 = version2,
            apSetupLocked = apSetupLocked,
            manufacturer = manufacturer,
            modelName = modelName,
            modelNumber = modelNumber,
            deviceName = deviceName,
        )
    }

    private fun parseVersion2(value: ByteArray): String? {
        if (value.size < WFA_VENDOR_ID.size || !value.startsWith(WFA_VENDOR_ID)) return null
        var offset = WFA_VENDOR_ID.size
        while (offset + 2 <= value.size) {
            val id = value[offset].toInt() and 0xFF
            val length = value[offset + 1].toInt() and 0xFF
            offset += 2
            if (offset + length > value.size) return null
            if (id == 0x00 && length >= 1) return formatVersion(value[offset])
            offset += length
        }
        return null
    }

    private fun merge(a: WpsBeaconInfo?, b: WpsBeaconInfo): WpsBeaconInfo = WpsBeaconInfo(
        version = b.version ?: a?.version,
        version2 = b.version2 ?: a?.version2,
        apSetupLocked = b.apSetupLocked ?: a?.apSetupLocked,
        manufacturer = b.manufacturer ?: a?.manufacturer,
        modelName = b.modelName ?: a?.modelName,
        modelNumber = b.modelNumber ?: a?.modelNumber,
        deviceName = b.deviceName ?: a?.deviceName,
    )

    private fun formatVersion(raw: Byte): String {
        val value = raw.toInt() and 0xFF
        return "${value ushr 4}.${value and 0x0F}"
    }

    private fun ascii(bytes: ByteArray): String? = bytes
        .toString(Charsets.UTF_8)
        .trim('\u0000', ' ', '\t', '\r', '\n')
        .takeIf { it.isNotBlank() }

    private fun u16(bytes: ByteArray, offset: Int): Int =
        ((bytes[offset].toInt() and 0xFF) shl 8) or (bytes[offset + 1].toInt() and 0xFF)

    private fun ByteArray.startsWith(prefix: ByteArray): Boolean =
        size >= prefix.size && prefix.indices.all { this[it] == prefix[it] }
}
