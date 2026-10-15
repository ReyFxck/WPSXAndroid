@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.reyfxck.wpsxandroid.core.capability.DeviceCapabilitySnapshot
import io.github.reyfxck.wpsxandroid.core.model.WifiNetwork
import io.github.reyfxck.wpsxandroid.core.model.WifiSecurity
import io.github.reyfxck.wpsxandroid.core.wifi.ChannelUsage
import io.github.reyfxck.wpsxandroid.core.wifi.ScanResult
import io.github.reyfxck.wpsxandroid.core.wifi.WifiBand
import io.github.reyfxck.wpsxandroid.core.wifi.buildChannelUsage
import io.github.reyfxck.wpsxandroid.core.wifi.wifiBandForFrequency
import io.github.reyfxck.wpsxandroid.core.wps.BssidNormalizer
import io.github.reyfxck.wpsxandroid.core.wps.WpsPinAlgorithmRegistry
import io.github.reyfxck.wpsxandroid.core.wps.WpsPinInput
import io.github.reyfxck.wpsxandroid.platform.device.AndroidDeviceCapabilityProbe
import io.github.reyfxck.wpsxandroid.platform.wifi.AndroidWifiScanner
import kotlinx.coroutines.launch

private enum class MainTab { SCANNER, CHANNELS, DEVICE }
private enum class NetworkSort { SIGNAL, NAME, CHANNEL }

private data class ScannerPreferences(
    val wpsOnly: Boolean = false,
    val hideHidden: Boolean = false,
    val showRawCapabilities: Boolean = false,
    val sort: NetworkSort = NetworkSort.SIGNAL,
)

@Composable
fun WpsxApp() {
    WpsxTheme {
        val context = LocalContext.current
        val scanner = remember(context) { AndroidWifiScanner(context.applicationContext) }
        val capabilityProbe = remember { AndroidDeviceCapabilityProbe() }
        val pinRegistry = remember { WpsPinAlgorithmRegistry(emptyList()) }
        val scope = rememberCoroutineScope()

        var selectedTab by remember { mutableStateOf(MainTab.SCANNER) }
        var showSettings by remember { mutableStateOf(false) }
        var scannerPreferences by remember(context) { mutableStateOf(loadScannerPreferences(context)) }
        var scanResult by remember { mutableStateOf<ScanResult?>(null) }
        var scanning by remember { mutableStateOf(false) }
        var selectedNetwork by remember { mutableStateOf<WifiNetwork?>(null) }
        var locationRequired by remember { mutableStateOf(false) }
        var capabilitySnapshot by remember { mutableStateOf<DeviceCapabilitySnapshot?>(null) }
        var inspectingCapabilities by remember { mutableStateOf(false) }
        var verifyingRoot by remember { mutableStateOf(false) }

        fun updatePreferences(newPreferences: ScannerPreferences) {
            scannerPreferences = newPreferences
            saveScannerPreferences(context, newPreferences)
        }

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
                showRawCapabilities = scannerPreferences.showRawCapabilities,
                onBack = { selectedNetwork = null },
            )
            return@WpsxTheme
        }

        val currentNetworks = (scanResult as? ScanResult.Success)?.networks.orEmpty()

        Scaffold(
            topBar = {
                SmallTopAppBar(
                    title = {
                        Text(
                            if (showSettings) "Settings" else "WPSXAndroid",
                            fontWeight = FontWeight.Bold,
                        )
                    },
                    navigationIcon = {
                        if (showSettings) {
                            IconButton(onClick = { showSettings = false }) {
                                Icon(Icons.Rounded.ArrowBack, contentDescription = "Back")
                            }
                        }
                    },
                    actions = {
                        if (!showSettings) {
                            IconButton(onClick = { showSettings = true }) {
                                Icon(Icons.Rounded.Settings, contentDescription = "Settings")
                            }
                        }
                    },
                )
            },
            bottomBar = {
                if (!showSettings) {
                    NavigationBar {
                        NavigationBarItem(
                            selected = selectedTab == MainTab.SCANNER,
                            onClick = { selectedTab = MainTab.SCANNER },
                            icon = { Icon(Icons.Rounded.Wifi, contentDescription = null) },
                            label = { Text("Scanner") },
                        )
                        NavigationBarItem(
                            selected = selectedTab == MainTab.CHANNELS,
                            onClick = { selectedTab = MainTab.CHANNELS },
                            icon = { Icon(Icons.Rounded.BarChart, contentDescription = null) },
                            label = { Text("Channels") },
                        )
                        NavigationBarItem(
                            selected = selectedTab == MainTab.DEVICE,
                            onClick = { selectedTab = MainTab.DEVICE },
                            icon = { Icon(Icons.Rounded.Build, contentDescription = null) },
                            label = { Text("Device") },
                        )
                    }
                }
            },
        ) { padding ->
            if (showSettings) {
                SettingsScreen(
                    modifier = Modifier.padding(padding),
                    preferences = scannerPreferences,
                    onPreferencesChanged = ::updatePreferences,
                )
            } else {
                when (selectedTab) {
                    MainTab.SCANNER -> ScannerScreen(
                        modifier = Modifier.padding(padding),
                        scanResult = scanResult,
                        scanning = scanning,
                        locationRequired = locationRequired,
                        preferences = scannerPreferences,
                        onScan = ::requestScan,
                        onOpenLocationSettings = {
                            locationSettingsLauncher.launch(
                                Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS),
                            )
                        },
                        onNetworkSelected = { selectedNetwork = it },
                    )

                    MainTab.CHANNELS -> ChannelsScreen(
                        modifier = Modifier.padding(padding),
                        networks = currentNetworks,
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
}

@Composable
private fun ScannerScreen(
    modifier: Modifier,
    scanResult: ScanResult?,
    scanning: Boolean,
    locationRequired: Boolean,
    preferences: ScannerPreferences,
    onScan: () -> Unit,
    onOpenLocationSettings: () -> Unit,
    onNetworkSelected: (WifiNetwork) -> Unit,
) {
    val visibleNetworks = remember(scanResult, preferences) {
        val networks = (scanResult as? ScanResult.Success)?.networks.orEmpty()
        applyScannerPreferences(networks, preferences)
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Nearby networks",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        "Tap an AP for details",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                FilledTonalButton(
                    onClick = onScan,
                    enabled = !scanning,
                ) {
                    if (scanning) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Icon(
                            Icons.Rounded.Wifi,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                    Spacer(Modifier.width(6.dp))
                    Text(if (scanning) "Scanning" else "Scan")
                }
            }
        }

        if (locationRequired) {
            item { LocationRequiredCard(onOpenLocationSettings) }
        }

        when (val result = scanResult) {
            null -> item {
                EmptyState("Tap Scan to discover nearby access points.")
            }

            is ScanResult.PermissionRequired -> item {
                EmptyState("Precise location permission is required by Android's Wi-Fi scan API.")
            }

            is ScanResult.Unavailable -> item {
                EmptyState(result.reason)
            }

            is ScanResult.Success -> {
                item {
                    val freshness = if (result.isFresh) "fresh" else "cached"
                    Text(
                        "${visibleNetworks.size} shown • ${result.networks.size} detected • $freshness",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                if (visibleNetworks.isEmpty()) {
                    item { EmptyState("No networks match the current scanner filters.") }
                } else {
                    items(
                        items = visibleNetworks,
                        key = { network -> "${network.bssid}-${network.frequencyMhz}" },
                    ) { network ->
                        CompactNetworkCard(
                            network = network,
                            onClick = { onNetworkSelected(network) },
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = "Use active security tests only on networks you own or have explicit permission to assess.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 4.dp),
            )
        }
    }
}

@Composable
private fun CompactNetworkCard(
    network: WifiNetwork,
    onClick: () -> Unit,
) {
    val hidden = network.ssid.isBlank()
    val ssid = network.ssid.ifBlank { "Hidden network" }
    val securityText = securityLabel(network.securityModes)
    val isOpen = network.securityModes.contains(WifiSecurity.OPEN)
    val band = wifiBandForFrequency(network.frequencyMhz)

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = if (hidden) Icons.Rounded.VisibilityOff else Icons.Rounded.Wifi,
                    contentDescription = if (hidden) "Hidden SSID" else "Wi-Fi network",
                    modifier = Modifier.size(22.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        ssid,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        network.bssid,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    "${network.rssiDbm} dBm",
                    style = MaterialTheme.typography.labelLarge,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                StatusPill(
                    text = securityText,
                    icon = if (isOpen) Icons.Rounded.LockOpen else Icons.Rounded.Lock,
                )
                StatusPill(text = band.label)
                if (network.wpsAdvertised) {
                    StatusPill(text = "WPS ?", icon = Icons.Rounded.Wifi)
                }
                Spacer(Modifier.weight(1f))
                Text(
                    buildString {
                        append("ch ")
                        append(network.channel ?: "?")
                        append(" • ")
                        append(signalLabel(network.rssiDbm))
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun StatusPill(
    text: String,
    icon: ImageVector? = null,
) {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        shape = MaterialTheme.shapes.small,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(13.dp))
            }
            Text(text, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun ChannelsScreen(
    modifier: Modifier,
    networks: List<WifiNetwork>,
) {
    val usage = remember(networks) { buildChannelUsage(networks) }
    val grouped = remember(usage) { usage.groupBy { it.band } }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    "Channel occupancy",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    "Counts detected access points per channel — not connected client devices.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        if (networks.isEmpty()) {
            item { EmptyState("Run a Wi-Fi scan first. Channel usage will appear here.") }
        } else if (usage.isEmpty()) {
            item { EmptyState("The current scan did not expose usable channel information.") }
        } else {
            WifiBand.values().forEach { band ->
                val entries = grouped[band].orEmpty()
                if (entries.isNotEmpty()) {
                    item(key = band.name) {
                        ChannelBandCard(band = band, entries = entries)
                    }
                }
            }
        }
    }
}

@Composable
private fun ChannelBandCard(
    band: WifiBand,
    entries: List<ChannelUsage>,
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                band.label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            entries.forEachIndexed { index, usage ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Channel ${usage.channel}")
                    StatusPill(
                        text = if (usage.accessPointCount == 1) "1 AP" else "${usage.accessPointCount} APs",
                    )
                }
                if (index != entries.lastIndex) {
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun SettingsScreen(
    modifier: Modifier,
    preferences: ScannerPreferences,
    onPreferencesChanged: (ScannerPreferences) -> Unit,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(
                "Scanner",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
        }
        item {
            SettingSwitchRow(
                title = "WPS networks only",
                description = "Hide access points that do not advertise WPS in Android scan data.",
                checked = preferences.wpsOnly,
                onCheckedChange = {
                    onPreferencesChanged(preferences.copy(wpsOnly = it))
                },
            )
        }
        item {
            SettingSwitchRow(
                title = "Hide hidden SSIDs",
                description = "Remove anonymous/hidden networks from the scanner list.",
                checked = preferences.hideHidden,
                onCheckedChange = {
                    onPreferencesChanged(preferences.copy(hideHidden = it))
                },
            )
        }
        item {
            SettingSwitchRow(
                title = "Show raw capabilities",
                description = "Display Android's raw capability string in the AP details screen.",
                checked = preferences.showRawCapabilities,
                onCheckedChange = {
                    onPreferencesChanged(preferences.copy(showRawCapabilities = it))
                },
            )
        }
        item {
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text("Sort networks", fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        NetworkSort.values().forEach { option ->
                            FilterChip(
                                selected = preferences.sort == option,
                                onClick = {
                                    onPreferencesChanged(preferences.copy(sort = option))
                                },
                                label = {
                                    Text(
                                        when (option) {
                                            NetworkSort.SIGNAL -> "Signal"
                                            NetworkSort.NAME -> "Name"
                                            NetworkSort.CHANNEL -> "Channel"
                                        },
                                    )
                                },
                            )
                        }
                    }
                }
            }
        }
        item {
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text("WPS state", fontWeight = FontWeight.SemiBold)
                    Text(
                        "The scanner can currently tell whether WPS is advertised, but it does not yet know whether the registrar is locked. Cards therefore show WPS ? instead of guessing locked/unlocked.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingSwitchRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold)
                Text(
                    description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}

@Composable
private fun LocationRequiredCard(onOpenLocationSettings: () -> Unit) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
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
                "Android requires the system Location switch to be enabled before Wi-Fi scan results are exposed.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(onClick = onOpenLocationSettings) {
                Icon(Icons.Rounded.Settings, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
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
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Device capabilities",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        "Root and radio probes",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                FilledTonalButton(
                    onClick = onInspect,
                    enabled = !inspecting && !verifyingRoot,
                ) {
                    Icon(Icons.Rounded.Build, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(if (inspecting) "Inspecting" else "Inspect")
                }
            }
        }

        if (snapshot == null) {
            item { EmptyState("Run Inspect to see hardware and root capability status.") }
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
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(5.dp),
                    ) {
                        Text("Wi-Fi interfaces", fontWeight = FontWeight.SemiBold)
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
                                    style = MaterialTheme.typography.bodySmall,
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
                FilledTonalButton(
                    onClick = onVerifyRoot,
                    enabled = !inspecting && !verifyingRoot,
                ) {
                    Text(if (verifyingRoot) "Verifying root…" else "Verify root access")
                }
            }
        }
    }
}

@Composable
private fun CapabilityCard(
    title: String,
    status: String,
    detail: String,
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(title, fontWeight = FontWeight.SemiBold)
                StatusPill(status)
            }
            Text(
                detail,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun EmptyState(message: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = message,
            modifier = Modifier.padding(14.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun NetworkDetailsScreen(
    network: WifiNetwork,
    pinRegistry: WpsPinAlgorithmRegistry,
    showRawCapabilities: Boolean,
    onBack: () -> Unit,
) {
    val ssid = network.ssid.ifBlank { "Hidden network" }
    val security = securityLabel(network.securityModes)
    val normalizedBssid = remember(network.bssid) { BssidNormalizer.normalize(network.bssid) }
    val candidates = remember(network.bssid, network.ssid, pinRegistry) {
        pinRegistry.derive(WpsPinInput(bssid = network.bssid, ssid = network.ssid))
    }
    val band = wifiBandForFrequency(network.frequencyMhz)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        ssid,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Back to scan")
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(7.dp),
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            StatusPill(
                                text = security,
                                icon = if (network.securityModes.contains(WifiSecurity.OPEN)) {
                                    Icons.Rounded.LockOpen
                                } else {
                                    Icons.Rounded.Lock
                                },
                            )
                            StatusPill(band.label)
                            if (network.wpsAdvertised) {
                                StatusPill("WPS ?", Icons.Rounded.Wifi)
                            }
                        }
                        Text("BSSID: ${normalizedBssid ?: network.bssid}")
                        Text("Signal: ${network.rssiDbm} dBm (${signalLabel(network.rssiDbm)})")
                        Text("Channel: ${network.channel ?: "unknown"} • ${network.frequencyMhz} MHz")
                    }
                }
            }

            item {
                val lines = mutableListOf<String>()
                lines += if (network.wpsAdvertised) {
                    "WPS is advertised by this access point. Registrar lock state is currently unknown."
                } else {
                    "WPS was not advertised in Android scan capabilities. This alone does not prove WPS is disabled."
                }
                if (showRawCapabilities) {
                    lines += "Raw: ${network.capabilities.ifBlank { "not reported" }}"
                }
                InfoCard(title = "WPS discovery", lines = lines)
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
                        "Pixie Dust: planned",
                        "Reaver: planned",
                        "Session logging will live here once the active backend layer is wired.",
                    ),
                )
            }
        }
    }
}

@Composable
private fun InfoCard(
    title: String,
    lines: List<String>,
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Text(title, fontWeight = FontWeight.SemiBold)
            lines.forEach { line ->
                Text(
                    line,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private fun applyScannerPreferences(
    networks: List<WifiNetwork>,
    preferences: ScannerPreferences,
): List<WifiNetwork> {
    val filtered = networks.filter { network ->
        (!preferences.wpsOnly || network.wpsAdvertised) &&
            (!preferences.hideHidden || network.ssid.isNotBlank())
    }

    return when (preferences.sort) {
        NetworkSort.SIGNAL -> filtered.sortedByDescending { it.rssiDbm }
        NetworkSort.NAME -> filtered.sortedBy { it.ssid.ifBlank { "~" }.lowercase() }
        NetworkSort.CHANNEL -> filtered.sortedWith(
            compareBy<WifiNetwork>(
                { wifiBandForFrequency(it.frequencyMhz).ordinal },
                { it.channel ?: Int.MAX_VALUE },
                { -it.rssiDbm },
            ),
        )
    }
}

private fun securityLabel(modes: Set<WifiSecurity>): String {
    if (modes.isEmpty()) return "UNKNOWN"
    val order = listOf(
        WifiSecurity.OPEN,
        WifiSecurity.WEP,
        WifiSecurity.WPA,
        WifiSecurity.WPA2,
        WifiSecurity.WPA3,
        WifiSecurity.OWE,
        WifiSecurity.UNKNOWN,
    )
    return order.filter(modes::contains).joinToString("/") { it.name }
}

private fun loadScannerPreferences(context: Context): ScannerPreferences {
    val prefs = context.getSharedPreferences("scanner_preferences", Context.MODE_PRIVATE)
    val sort = runCatching {
        NetworkSort.valueOf(prefs.getString("sort", NetworkSort.SIGNAL.name) ?: NetworkSort.SIGNAL.name)
    }.getOrDefault(NetworkSort.SIGNAL)

    return ScannerPreferences(
        wpsOnly = prefs.getBoolean("wps_only", false),
        hideHidden = prefs.getBoolean("hide_hidden", false),
        showRawCapabilities = prefs.getBoolean("show_raw_capabilities", false),
        sort = sort,
    )
}

private fun saveScannerPreferences(
    context: Context,
    preferences: ScannerPreferences,
) {
    context.getSharedPreferences("scanner_preferences", Context.MODE_PRIVATE)
        .edit()
        .putBoolean("wps_only", preferences.wpsOnly)
        .putBoolean("hide_hidden", preferences.hideHidden)
        .putBoolean("show_raw_capabilities", preferences.showRawCapabilities)
        .putString("sort", preferences.sort.name)
        .apply()
}

private fun isLocationServiceEnabled(context: Context): Boolean =
    context.getSystemService(LocationManager::class.java)?.isLocationEnabled == true

private fun signalLabel(rssiDbm: Int): String = when {
    rssiDbm >= -50 -> "excellent"
    rssiDbm >= -60 -> "good"
    rssiDbm >= -70 -> "fair"
    else -> "weak"
}
