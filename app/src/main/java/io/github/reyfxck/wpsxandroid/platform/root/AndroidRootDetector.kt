package io.github.reyfxck.wpsxandroid.platform.root

import io.github.reyfxck.wpsxandroid.core.capability.RootAccessStatus
import io.github.reyfxck.wpsxandroid.core.capability.RootIdentityParser
import io.github.reyfxck.wpsxandroid.core.capability.RootState
import java.io.File
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AndroidRootDetector {
    private val knownSuPaths = listOf(
        "/system/bin/su",
        "/system/xbin/su",
        "/sbin/su",
        "/su/bin/su",
        "/vendor/bin/su",
        "/product/bin/su",
    )

    fun inspectPassive(): RootAccessStatus {
        val found = knownSuPaths.firstOrNull { path -> runCatching { File(path).exists() }.getOrDefault(false) }
        return if (found != null) {
            RootAccessStatus(
                state = RootState.SU_BINARY_FOUND,
                detail = "Potential su binary found at $found. Root is not verified yet.",
            )
        } else {
            RootAccessStatus(
                state = RootState.NOT_DETECTED,
                detail = "No su binary was found in the app's visible common paths. This is not proof that the device is unrooted.",
            )
        }
    }

    suspend fun verify(): RootAccessStatus = withContext(Dispatchers.IO) {
        val process = runCatching {
            ProcessBuilder("su", "-c", "id")
                .redirectErrorStream(true)
                .start()
        }.getOrElse { error ->
            return@withContext RootAccessStatus(
                state = RootState.DENIED_OR_FAILED,
                detail = "Could not start su: ${error.javaClass.simpleName}",
            )
        }

        try {
            val finished = process.waitFor(10, TimeUnit.SECONDS)
            if (!finished) {
                process.destroyForcibly()
                return@withContext RootAccessStatus(
                    state = RootState.DENIED_OR_FAILED,
                    detail = "Root verification timed out. A root manager prompt may have been denied or left unanswered.",
                )
            }

            val output = process.inputStream.bufferedReader().use { reader ->
                reader.readText().take(4096)
            }

            if (process.exitValue() == 0 && RootIdentityParser.isRootIdentity(output)) {
                RootAccessStatus(
                    state = RootState.GRANTED,
                    detail = "Root verified with a fixed `su -c id` check.",
                )
            } else {
                RootAccessStatus(
                    state = RootState.DENIED_OR_FAILED,
                    detail = "su did not return a verified uid=0 identity.",
                )
            }
        } finally {
            if (process.isAlive) process.destroyForcibly()
        }
    }
}
