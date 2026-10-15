package io.github.reyfxck.wpsxandroid.core.capability

/** Pure parser for the fixed `id` command used by the root adapter. */
object RootIdentityParser {
    private val uidZero = Regex("(?:^|\\s)uid=0(?:\\(|\\s|$)")

    fun isRootIdentity(output: String): Boolean = uidZero.containsMatchIn(output)
}
