package io.github.reyfxck.wpsxandroid.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.reyfxck.wpsxandroid.core.model.WifiNetwork
import io.github.reyfxck.wpsxandroid.core.wifi.ScanResult
import io.github.reyfxck.wpsxandroid.platform.wifi.AndroidWifiScanner
import kotlinx.coroutines.launch

@Composable
fun WpsxApp() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        val context = LocalContext.current
        val scanner = remember(context) { AndroidWifiScanner(context.applicationContext) }
        val scope = rememberCoroutineScope()
        var scanResult by remember { mutableStateOf<ScanResult?>(null) }
        var scanning by remember { mutableStateOf(false) }

        fun runScan() {
            scanning = true
            scope.launch {
                scanResult = scanner.scan()
                scanning = false
            }
        }

        val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
        ) { granted ->
            if (granted) {
                runScan()
            } else {
                scanResult = ScanResult.PermissionRequired(
                    setOf(Manifest.permission.ACCESS_FINE_LOCATION),
                )
            }
        }

        Scaffold { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "WPSXAndroid",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "Wi-Fi / WPS discovery",
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }

                item {
                    Button(
                        onClick = {
                            val granted = context.checkSelfPermission(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                            ) == PackageManager.PERMISSION_GRANTED

                            if (granted) runScan()
                            else permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                        },
                        enabled = !scanning,
                    ) {
                        Text(if (scanning) "Scanning…" else "Scan Wi-Fi")
                    }
                }

                if (scanning) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            CircularProgressIndicator()
                            Text("Waiting for Android Wi-Fi scan results…")
                        }
                    }
                }

                when (val result = scanResult) {
                    null -> item {
                        Text("Tap Scan Wi-Fi to discover nearby access points.")
                    }

                    is ScanResult.PermissionRequired -> item {
                        Text(
                            "Precise location permission is required by Android's Wi-Fi scan API.",
                        )
                    }

                    is ScanResult.Unavailable -> item {
                        Text(result.reason)
                    }

                    is ScanResult.Success -> {
                        item {
                            val freshness = if (result.isFresh) "fresh scan" else "cached/latest available"
                            Text("${result.networks.size} access points • $freshness")
                        }
                        items(
                            items = result.networks,
                            key = { network -> "${network.bssid}-${network.frequencyMhz}" },
                        ) { network ->
                            NetworkCard(network)
                        }
                    }
                }

                item {
                    Text(
                        text = "Use active security tests only on networks you own or have explicit permission to assess.",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun NetworkCard(network: WifiNetwork) {
    val security = network.securityModes.joinToString(" / ") { it.name }
    val ssid = network.ssid.ifBlank { "<hidden SSID>" }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(ssid, fontWeight = FontWeight.SemiBold)
                if (network.wpsAdvertised) {
                    Text("WPS", fontWeight = FontWeight.Bold)
                }
            }
            Text(network.bssid, style = MaterialTheme.typography.bodySmall)
            Text(
                "${network.rssiDbm} dBm • ${network.frequencyMhz} MHz" +
                    (network.channel?.let { " • ch $it" } ?: ""),
            )
            Text(security, style = MaterialTheme.typography.bodySmall)
        }
    }
}
