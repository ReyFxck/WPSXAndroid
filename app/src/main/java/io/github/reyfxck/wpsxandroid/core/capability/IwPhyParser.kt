package io.github.reyfxck.wpsxandroid.core.capability

/** Parses the read-only output of `iw phy` without making hardware changes. */
object IwPhyParser {
    fun reportsMonitorMode(output: String): Boolean? {
        var inSupportedModes = false
        var sawModeEntry = false

        for (rawLine in output.lineSequence()) {
            val line = rawLine.trim()

            if (!inSupportedModes) {
                if (line.equals("Supported interface modes:", ignoreCase = true)) {
                    inSupportedModes = true
                }
                continue
            }

            if (line.startsWith("*")) {
                sawModeEntry = true
                val mode = line.removePrefix("*").trim()
                if (mode.equals("monitor", ignoreCase = true)) return true
                continue
            }

            if (line.endsWith(":")) break
        }

        return if (inSupportedModes && sawModeEntry) false else null
    }
}
