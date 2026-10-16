package io.github.reyfxck.wpsxandroid.ui

import android.content.Context
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.DialogProperties

private const val DISCLAIMER_PREFS = "wpsx_legal"
private const val DISCLAIMER_VERSION_KEY = "accepted_disclaimer_version"
private const val DISCLAIMER_VERSION = 1

/**
 * First-run authorized-use notice.
 *
 * Acceptance is versioned so a materially revised notice can be shown again
 * without presenting the same dialog on every launch.
 */
@Composable
fun AppDisclaimerGate(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val preferences = remember(context) {
        context.getSharedPreferences(DISCLAIMER_PREFS, Context.MODE_PRIVATE)
    }
    var accepted by remember(preferences) {
        mutableStateOf(preferences.getInt(DISCLAIMER_VERSION_KEY, 0) >= DISCLAIMER_VERSION)
    }

    if (accepted) {
        content()
        return
    }

    AlertDialog(
        onDismissRequest = { /* Explicit acknowledgement is required. */ },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
        ),
        title = { Text("Uso autorizado e responsabilidade") },
        text = {
            Text(
                "O WPSXAndroid é destinado a pesquisa, aprendizado, interoperabilidade e auditorias de segurança em redes e dispositivos que você possui ou tem autorização explícita para testar. " +
                    "Recursos ativos podem interromper conexões, acionar proteções do roteador e gerar consequências legais ou contratuais quando usados sem autorização. " +
                    "Ao continuar, você declara que compreende essas condições e assume responsabilidade pelo uso do aplicativo.",
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    preferences.edit().putInt(DISCLAIMER_VERSION_KEY, DISCLAIMER_VERSION).apply()
                    accepted = true
                },
            ) {
                Text("Compreendo")
            }
        },
    )
}
