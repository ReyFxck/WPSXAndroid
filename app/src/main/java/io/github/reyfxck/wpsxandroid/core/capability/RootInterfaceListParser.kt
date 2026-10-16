package io.github.reyfxck.wpsxandroid.core.capability

/** Parses the fixed `ls -1 /sys/class/net` fallback used after root is verified. */
object RootInterfaceListParser {
    fun parse(output: String): List<String> = output
        .lineSequence()
        .map(String::trim)
        .filter(String::isNotBlank)
        .filter(WifiInterfaceClassifier::isSafeKernelInterfaceName)
        .distinct()
        .sorted()
        .toList()
}
