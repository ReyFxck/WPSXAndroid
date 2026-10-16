package io.github.reyfxck.wpsxandroid.core.capability

data class IwInterfaceMode(
    val name: String,
    val type: String,
)

object IwDevParser {
    fun parse(output: String): List<IwInterfaceMode> {
        val results = mutableListOf<IwInterfaceMode>()
        var currentName: String? = null

        output.lineSequence().forEach { raw ->
            val line = raw.trim()
            when {
                line.startsWith("Interface ") -> {
                    currentName = line.removePrefix("Interface ").trim().takeIf { it.isNotEmpty() }
                }
                line.startsWith("type ") -> {
                    val name = currentName
                    val type = line.removePrefix("type ").trim()
                    if (name != null && type.isNotEmpty()) {
                        results += IwInterfaceMode(name, type)
                    }
                }
            }
        }

        return results
    }

    fun monitorInterfaces(output: String): List<String> =
        parse(output)
            .filter { it.type.equals("monitor", ignoreCase = true) }
            .map { it.name }
}
