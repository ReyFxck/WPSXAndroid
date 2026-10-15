package io.github.reyfxck.wpsxandroid.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import io.github.reyfxck.wpsxandroid.core.capability.DeviceCapabilitySnapshot
import io.github.reyfxck.wpsxandroid.core.model.WifiNetwork
import io.github.reyfxck.wpsxandroid.core.wifi.ScanResult
import io.github.reyfxck.wpsxandroid.core.wps.BssidNormalizer
import io.github.reyfxck.wpsxandroid.core.wps.WpsPinAlgorithmRegistry
import io.github.reyfxck.wpsxandroid.core.wps.WpsPinInput
import io.github.reyfxck.wpsxandroid.platform.device.AndroidDeviceCapabilityProbe
import io.github.reyfxck.wpsxandroid.platform.wifi.AndroidWifiScanner
import kotlinx.coroutines.launch

private enum class MainTab { SCANNER, DEVICE }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WpsxApp() {
    WpsxTheme {
        val context = LocalContext.current
        val scanner = remember(context) { AndroidWifiScanner(context.applicationContext) }
        val capabilityProbe = remember { AndroidDeviceCapabilityProbe() }
        val pinRegistry = remember { WpsPinAlgorithmRegistry(emptyList()) }
        val scope = rememberCoroutineScope()

        var selectedTab by remember { mutableStateOf(MainTab.SCANNER) }
        var scanResult by remember { mutableStateOf<ScanResult?>(null) }
        var scanning by remember { mutableStateOf(false) }
        var selectedNetwork by remember { mutableStateOf<WifiNetwork?>(null) }
        var locationRequired by remember { mutableStateOf(false) }
        var capabilitySnapshot by remember { mutableStateOf<DeviceCapabilitySnapshot?>(null) }
        var inspectingCapabilities by remember { mutableStateOf(false) }
        var verifyingRoot by remember { mutableStateOf(false) }

        fun runScan() {
            scanning = true
            selectedNetwork = null
            scope.launch {
                scanResult = scanner.scan()
                locationRequired = !isLocationServiceEnabled(context)
                scanning = false
            }
        }

        fun scanIfLocationReady() {
            if (isLocationServiceEnabled(context)) {
                locationRequired = false
                runScan()
            } else {
                locationRequired = true
            }
        }

        fun inspectDevice() {
            inspectingCapabilities = true
            scope.launch {
                capabilitySnapshot = capabilityProbe.inspectPassive()
                inspectingCapabilities = false
            }
        }

        fun verifyRoot() {
            verifyingRoot = true
            scope.launch {
                capabilitySnapshot = capabilityProbe.verifyRoot()
                verifyingRoot = false
            }
        }

        val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
        ) { granted ->
            if (granted) {
                scanIfLocationReady()
            } else {
                scanResult = ScanResult.PermissionRequired(
                    setOf(Manifest.permission.ACCESS_FINE_LOCATION),
                )
            }
        }

        val locationSettingsLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult(),
        ) {
            scanIfLocationReady()
        }

        fun requestScan() {
            val granted = context.checkSelfPermission(
                Manifest.permission.ACCESS_FINE_LOCATION,
            ) == PackageManager.PERMISSION_GRANTED

            if (granted) scanIfLocationReady()
            else permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        if (selectedNetwork != null) {
            NetworkDetailsScreen(
                network = selectedNetwork!!,
                pinRegistry = pinRegistry,
                onBack = { selectedNetwork = null },
            )
            return@WpsxTheme
        }

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("WPSXAndroid", fontWeight = FontWeight.Bold)
                            Text(
                                "Wi-Fi / WPS diagnostics",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    },
                )
            },
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        selected = selectedTab == MainTab.SCANNER,
                        onClick = { selectedTab = MainTab.SCANNER },
                        icon = { Icon(Icons.Rounded.Wifi, contentDescription = null) },
                        label = { Text("Scanner") },
                    )
                    NavigationBarItem(
                        selected = selectedTab == MainTab.DEVICE,
                        onClick = { selectedTab = MainTab.DEVICE },
                        icon = { Icon(Icons.Rounded.Build, contentDescription = null) },
                        label = { Text("Device") },
                    )
                }
            },
        ) { padding ->
            when (selectedTab) {
                MainTab.SCANNER -> ScannerScreen(
                    modifier = Modifier.padding(padding),
                    scanResult = scanResult,
                    scanning = scanning,
                    locationRequired = locationRequired,
                    onScan = ::requestScan,
                    onOpenLocationSettings = {
                        locationSettingsLauncher.launch(
                            Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS),
                        )
                    },
                    onNetworkSelected = { selectedNetwork = it },
                )

                MainTab.DEVICE -> DeviceScreen(
                    modifier = Modifier.padding(padding),
                    snapshot = capabilitySnapshot,
                    inspecting = inspectingCapabilities,
                    verifyingRoot = verifyingRoot,
                    onInspect = ::inspectDevice,
                    onVerifyRoot = ::verifyRoot,
                )
            }
        }
    }
}

@Composable
private fun ScannerScreen(
    modifier: Modifier,
    scanResult: ScanResult?,
    scanning: Boolean,
    locationRequired: Boolean,
    onScan: () -> Unit,
    onOpenLocationSettings: () -> Unit,
    onNetworkSelected: (WifiNetwork) -> Unit,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "Nearby networks",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    "Passive discovery first. Tap a network for WPS and radio details.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        if (locationRequired) {
            item {
                LocationRequiredCard(onOpenLocationSettings)
            }
        }

        item {
            FilledTonalButton(
                onClick = onScan,
                enabled = !scanning,
            ) {
                Icon(Icons.Rounded.Wifi, contentDescription = null)
                Spacer(Modifier.width(8.dp))
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
                EmptyState("Tap Scan Wi-Fi to discover nearby access points.")
            }

            is ScanResult.PermissionRequired -> item {
                EmptyState("Precise location permission is required by Android's Wi-Fi scan API.")
            }

            is ScanResult.Unavailable -> item {
                EmptyState(result.reason)
            }

            is ScanResult.Success -> {
                item {
                    val freshness = if (result.isFresh) "fresh scan" else "cached/latest available"
                    Text(
                        "${result.networks.size} access points • $freshness",
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
                items(
                    items = result.networks,
                    key = { network -> "${network.bssid}-${network.frequencyMhz}" },
                ) { network ->
                    NetworkCard(network = network, onClick = { onNetworkSelected(network) })
                }
            }
        }

        item {
            Text(
                text = "Use active security tests only on networks you own or have explicit permission to assess.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

@Composable
private fun LocationRequiredCard(onOpenLocationSettings: () -> Unit) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Rounded.LocationOn, contentDescription = null)
                Text(
                    "Location services are off",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Text(
                "Android requires the system Location switch to be enabled before Wi-Fi scan results are exposed. Permission alone is not enough.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(onClick = onOpenLocationSettings) {
                Icon(Icons.Rounded.Settings, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Open location settings")
            }
        }
    }
}

@Composable
private fun DeviceScreen(
    modifier: Modifier,
    snapshot: DeviceCapabilitySnapshot?,
    inspecting: Boolean,
    verifyingRoot: Boolean,
    onInspect: () -> Unit,
    onVerifyRoot: () -> Unit,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "Device capabilities",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    "Root, Wi-Fi interfaces and read-only radio capability probes live here instead of cluttering the scanner.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        item {
            FilledTonalButton(
                onClick = onInspect,
                enabled = !inspecting && !verifyingRoot,
            ) {
                Icon(Icons.Rounded.Build, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(if (inspecting) "Inspecting…" else "Inspect device")
            }
        }

        if (snapshot == null) {
            item { EmptyState("Run Inspect device to see hardware and root capability status.") }
        } else {
            item {
                CapabilityCard(
                    title = "Root",
                    status = snapshot.root.state.name,
                    detail = snapshot.root.detail,
                )
            }
            item {
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            "Wi-Fi interfaces",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        if (snapshot.wifiInterfaces.isEmpty()) {
                            Text(
                                "None visible to the app",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        } else {
                            snapshot.wifiInterfaces.forEach { info ->
                                Text(
                                    "${info.name} • ${info.kind.name} • ${if (info.isUp) "up" else "down"}" +
                                        if (info.sysfsWireless) " • sysfs wireless" else "",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
            item {
                CapabilityCard(
                    title = "Monitor mode",
                    status = snapshot.monitorMode.state.name,
                    detail = snapshot.monitorMode.detail,
                )
            }
            item {
                CapabilityCard(
                    title = "Packet injection",
                    status = snapshot.packetInjection.state.name,
                    detail = snapshot.packetInjection.detail,
                )
            }
            item {
                Button(
                    onClick = onVerifyRoot,
                    enabled = !inspecting && !verifyingRoot,
                ) {
                    Text(if (verifyingRoot) "Verifying root…" else "Verify root access")
                }
            }
            item {
                Text(
                    "Root verification may show your root manager prompt. The adapter only runs the fixed command `su -c id`.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun CapabilityCard(title: String, status: String, detail: String) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(status, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
            Text(detail, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun EmptyState(message: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = message,
            modifier = Modifier.padding(18.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun NetworkCard(network: WifiNetwork, onClick: () -> Unit) {
    val security = network.securityModes.joinToString(" / ") { it.name }
    val ssid = network.ssid.ifBlank { "<hidden SSID>" }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    ssid,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                if (network.wpsAdvertised) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        shape = MaterialTheme.shapes.small,
                    ) {
                        Text(
                            "WPS",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
            Text(
                network.bssid,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                "${network.rssiDbm} dBm • ${signalLabel(network.rssiDbm)} • ${network.frequencyMhz} MHz" +
                    (network.channel?.let { " • ch $it" } ?: ""),
            )
            Text(security.ifBlank { "UNKNOWN" }, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun NetworkDetailsScreen(
    network: WifiNetwork,
    pinRegistry: WpsPinAlgorithmRegistry,
    onBack: () -> Unit,
) {
    val ssid = network.ssid.ifBlank { "<hidden SSID>" }
    val security = network.securityModes.joinToString(" / ") { it.name }.ifBlank { "UNKNOWN" }
    val normalizedBssid = remember(network.bssid) { BssidNormalizer.normalize(network.bssid) }
    val candidates = remember(network.bssid, network.ssid, pinRegistry) {
        pinRegistry.derive(WpsPinInput(bssid = network.bssid, ssid = network.ssid))
    }

    Scaffold { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                FilledTonalButton(onClick = onBack) { Text("Back to scan") }
            }
            item {
                Text(
                    ssid,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    "Access point details",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            item {
                InfoCard(
                    title = "Radio / security",
                    lines = listOf(
                        "BSSID: ${normalizedBssid ?: network.bssid}",
                        "Signal: ${network.rssiDbm} dBm (${signalLabel(network.rssiDbm)})",
                        "Frequency: ${network.frequencyMhz} MHz",
                        "Channel: ${network.channel?.toString() ?: "unknown"}",
                        "Security: $security",
                    ),
                )
            }
            item {
                val wpsSummary = if (network.wpsAdvertised) {
                    "Android scan capabilities advertise WPS for this AP."
                } else {
                    "Android scan capabilities did not advertise WPS. This alone does not prove WPS is disabled."
                }
                InfoCard(
                    title = "WPS discovery",
                    lines = listOf(
                        wpsSummary,
                        "Raw capabilities: ${network.capabilities.ifBlank { "not reported" }}",
                    ),
                )
            }
            item {
                val engineStatus = when {
                    normalizedBssid == null -> "BSSID could not be normalized, so offline analysis is unavailable."
                    pinRegistry.registeredIds.isEmpty() -> "Engine ready. No vendor-specific PIN algorithms are bundled."
                    candidates.isEmpty() -> "No registered offline algorithm produced a candidate for this AP."
                    else -> "${candidates.size} offline candidate(s) produced by registered algorithms."
                }
                InfoCard(
                    title = "Offline WPS PIN engine",
                    lines = listOf(
                        "Registered algorithms: ${pinRegistry.registeredIds.size}",
                        engineStatus,
                    ),
                )
            }
            item {
                InfoCard(
                    title = "Active audit backends",
                    lines = listOf(
                        "Pixie Dust: not implemented",
                        "Reaver: not implemented",
                        "Active tests will require an authorized lab plus compatible root/kernel/driver capabilities.",
                    ),
                )
            }
        }
    }
}

@Composable
private fun InfoCard(title: String, lines: List<String>) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            lines.forEach { line ->
                Text(line, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

private fun isLocationServiceEnabled(context: Context): Boolean =
    context.getSystemService(LocationManager::class.java)?.isLocationEnabled == true

private fun signalLabel(rssiDbm: Int): String = when {
    rssiDbm >= -50 -> "excellent"
    rssiDbm >= -60 -> "good"
    rssiDbm >= -70 -> "fair"
    else -> "weak"
}
