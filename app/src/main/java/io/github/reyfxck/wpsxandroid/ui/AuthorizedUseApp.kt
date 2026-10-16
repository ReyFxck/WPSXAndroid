package io.github.reyfxck.wpsxandroid.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

/**
 * Informational responsible-use notice shown at app startup.
 *
 * This notice does not replace the active-test target/capability checks. Those
 * remain enforced separately before any root/radio backend is allowed to run.
 */
@Composable
fun AuthorizedUseApp() {
    var acknowledged by rememberSaveable { mutableStateOf(false) }

    WpsxAppNext()

    if (!acknowledged) {
        AlertDialog(
            onDismissRequest = { /* explicit acknowledgement only */ },
            title = { Text("Uso autorizado") },
            text = {
                Text(
                    text = "WPSXAndroid é uma ferramenta de pesquisa, diagnóstico e auditoria de segurança Wi-Fi/WPS. " +
                        "Use somente em redes e equipamentos seus ou para os quais você tenha autorização explícita. " +
                        "Não use os modos ativos para testar redes de terceiros sem permissão. " +
                        "Sempre que possível, prefira um ambiente de laboratório ou teste controlado.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            confirmButton = {
                Button(onClick = { acknowledged = true }) {
                    Text("Compreendo")
                }
            },
        )
    }
}
