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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material.icons.rounded.WifiOff
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import io.github.reyfxck.wpsxandroid.core.wifi.estimateDistanceMeters
import io.github.reyfxck.wpsxandroid.core.wifi.wifiBandForFrequency
import io.github.reyfxck.wpsxandroid.core.wps.BssidNormalizer
import io.github.reyfxck.wpsxandroid.core.wps.WpsPinAlgorithmRegistry
import io.github.reyfxck.wpsxandroid.core.wps.WpsPinInput
import io.github.reyfxck.wpsxandroid.platform.device.AndroidDeviceCapabilityProbe
import io.github.reyfxck.wpsxandroid.platform.wifi.AndroidWifiScanner
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private enum class MainTab { SCANNER, CHANNELS, DEVICE }
private enum class NetworkSort { SIGNAL, NAME, CHANNEL }

private val OpenNetworkGreen = Color(0xFF2E7D32)
private val OpenNetworkOnGreen = Color.White

private data class ScannerPreferences(
    val wpsOnly: Boolean = false,
    val hideHidden: Boolean = false,
    val showRawCapabilities: Boolean = false,
    val sort: NetworkSort = NetworkSort.SIGNAL,
    val themeMode: WpsxThemeMode = WpsxThemeMode.SYSTEM,
)

@Composable
fun WpsxApp() {
    val context = LocalContext.current
    var preferences by remember(context) { mutableStateOf(loadScannerPreferences(context)) }

    WpsxTheme(mode = preferences.themeMode) {
        val scanner = remember(context) { AndroidWifiScanner(context.applicationContext) }
        val capabilityProbe = remember { AndroidDeviceCapabilityProbe() }
        val pinRegistry = remember { WpsPinAlgorithmRegistry(emptyList()) }
        val scope = rememberCoroutineScope()

        var selectedTab by remember { mutableStateOf(MainTab.SCANNER) }
        var showSettings by remember { mutableStateOf(false) }
        var scanResult by remember { mutableStateOf<ScanResult?>(null) }
        var scanning by remember { mutableStateOf(false) }
        var selectedNetwork by remember { mutableStateOf<WifiNetwork?>(null) }
        var locationRequired by remember { mutableStateOf(false) }
        var showLocationDialog by remember { mutableStateOf(false) }
        var capabilitySnapshot by remember { mutableStateOf<DeviceCapabilitySnapshot?>(null) }
        var inspectingCapabilities by remember { mutableStateOf(false) }
        var verifyingRoot by remember { mutableStateOf(false) }

        fun updatePreferences(newValue: ScannerPreferences) {
            preferences = newValue
            saveScannerPreferences(context, newValue)
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

        fun scanIfLocationReady(showDialogIfDisabled: Boolean = true) {
            if (isLocationServiceEnabled(context)) {
                locationRequired = false
                showLocationDialog = false
                runScan()
            } else {
                locationRequired = true
                if (showDialogIfDisabled) showLocationDialog = true
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
            scanIfLocationReady(showDialogIfDisabled = false)
        }

        fun openLocationSettings() {
            showLocationDialog = false
            locationSettingsLauncher.launch(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        }

        fun requestScan() {
            val granted = context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
            if (granted) scanIfLocationReady()
            else permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        if (showLocationDialog) {
            AlertDialog(
                onDismissRequest = { showLocationDialog = false },
                icon = { Icon(Icons.Rounded.LocationOn, contentDescription = null) },
                title = { Text("Turn on Location") },
                text = {
                    Text(
                        "Android requires the system Location switch for Wi-Fi scan results. " +
                            "The app permission is controlled separately.",
                    )
                },
                confirmButton = {
                    Button(onClick = ::openLocationSettings) { Text("Open settings") }
                },
                dismissButton = {
                    TextButton(onClick = { showLocationDialog = false }) { Text("Not now") }
                },
            )
        }

        selectedNetwork?.let { network ->
            NetworkDetailsScreen(
                network = network,
                pinRegistry = pinRegistry,
                showRawCapabilities = preferences.showRawCapabilities,
                onBack = { selectedNetwork = null },
            )
            return@WpsxTheme
        }

        val currentNetworks = (scanResult as? ScanResult.Success)?.networks.orEmpty()

        Scaffold(
            topBar = {
                TopAppBar(
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
                    preferences = preferences,
                    onPreferencesChanged = ::updatePreferences,
                )
            } else {
                when (selectedTab) {
                    MainTab.SCANNER -> ScannerScreen(
                        modifier = Modifier.padding(padding),
                        scanResult = scanResult,
                        scanning = scanning,
                        locationRequired = locationRequired,
                        preferences = preferences,
                        onScan = ::requestScan,
                        onOpenLocationSettings = ::openLocationSettings,
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
        applyScannerPreferences((scanResult as? ScanResult.Success)?.networks.orEmpty(), preferences)
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
                Column(modifier = Modifier.fillMaxWidth(0.62f)) {
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
                FilledTonalButton(onClick = onScan, enabled = !scanning) {
                    if (scanning) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Rounded.Wifi, contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                    Spacer(Modifier.width(6.dp))
                    Text(if (scanning) "Scanning" else "Scan")
                }
            }
        }

        if (locationRequired) item { LocationRequiredCard(onOpenLocationSettings) }

        when (val result = scanResult) {
            null -> item { EmptyState("Tap Scan to discover nearby access points.") }
            is ScanResult.PermissionRequired -> item {
                EmptyState("Precise location permission is required by Android's Wi-Fi scan API.")
            }
            is ScanResult.Unavailable -> item { EmptyState(result.reason) }
            is ScanResult.Success -> {
                item {
                    Text(
                        "${visibleNetworks.size} shown • ${result.networks.size} detected • " +
                            if (result.isFresh) "fresh" else "cached",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (visibleNetworks.isEmpty()) {
                    item { EmptyState("No networks match the current scanner filters.") }
                } else {
                    items(
                        items = visibleNetworks,
                        key = { "${it.bssid}-${it.frequencyMhz}" },
                    ) { network ->
                        CompactNetworkCard(network = network, onClick = { onNetworkSelected(network) })
                    }
                }
            }
        }

        item {
            Text(
                "Use active security tests only on networks you own or have explicit permission to assess.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 4.dp),
            )
        }
    }
}

@Composable
private fun CompactNetworkCard(network: WifiNetwork, onClick: () -> Unit) {
    val hidden = network.ssid.isBlank()
    val ssid = network.ssid.ifBlank { "Hidden network" }
    val isOpen = network.securityModes.contains(WifiSecurity.OPEN)
    val band = wifiBandForFrequency(network.frequencyMhz)
    val distance = estimateDistanceMeters(network.rssiDbm)
    val vendor = network.vendorName

    ElevatedCard(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                NetworkAvatar(network = network, hidden = hidden, isOpen = isOpen)
                Column(modifier = Modifier.fillMaxWidth(0.58f)) {
                    Text(
                        ssid,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        buildString {
                            append(network.bssid)
                            if (!vendor.isNullOrBlank()) append(" • $vendor")
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("${network.rssiDbm} dBm", style = MaterialTheme.typography.labelLarge)
                    Text(
                        formatDistance(distance),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                StatusPill(
                    text = securityLabel(network.securityModes),
                    icon = if (isOpen) Icons.Rounded.LockOpen else Icons.Rounded.Lock,
                    containerColor = if (isOpen) OpenNetworkGreen else null,
                    contentColor = if (isOpen) OpenNetworkOnGreen else null,
                )
                StatusPill(text = "${band.label} • ch ${network.channel ?: "?"}")
                WpsStatusPill(network)
            }
        }
    }
}

@Composable
private fun NetworkAvatar(network: WifiNetwork, hidden: Boolean, isOpen: Boolean) {
    val vendor = network.vendorName
    if (vendor.isNullOrBlank()) {
        Icon(
            imageVector = if (hidden) Icons.Rounded.VisibilityOff else Icons.Rounded.Wifi,
            contentDescription = if (hidden) "Hidden SSID" else "Wi-Fi network",
            modifier = Modifier.size(24.dp),
            tint = if (isOpen) OpenNetworkGreen else MaterialTheme.colorScheme.primary,
        )
        return
    }

    Surface(
        modifier = Modifier.size(34.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                vendorInitials(vendor),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun WpsStatusPill(network: WifiNetwork) {
    if (!network.wpsAdvertised) {
        StatusPill(
            text = "WPS —",
            icon = Icons.Rounded.WifiOff,
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
        )
        return
    }

    val version = network.wpsInfo?.effectiveVersion
    val versionSuffix = version?.let { " $it" }.orEmpty()
    when (network.wpsInfo?.apSetupLocked) {
        true -> StatusPill(
            text = "WPS locked",
            icon = Icons.Rounded.Lock,
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
        )
        false -> StatusPill(
            text = "WPS$versionSuffix",
            icon = Icons.Rounded.LockOpen,
            containerColor = OpenNetworkGreen,
            contentColor = OpenNetworkOnGreen,
        )
        null -> StatusPill(text = "WPS$versionSuffix", icon = Icons.Rounded.Wifi)
    }
}

@Composable
private fun StatusPill(
    text: String,
    icon: ImageVector? = null,
    containerColor: Color? = null,
    contentColor: Color? = null,
) {
    Surface(
        color = containerColor ?: MaterialTheme.colorScheme.secondaryContainer,
        contentColor = contentColor ?: MaterialTheme.colorScheme.onSecondaryContainer,
        shape = MaterialTheme.shapes.small,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) Icon(icon, contentDescription = null, modifier = Modifier.size(13.dp))
            Text(text, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun ChannelsScreen(modifier: Modifier, networks: List<WifiNetwork>) {
    val usage = remember(networks) { buildChannelUsage(networks) }
    val grouped = remember(usage) { usage.groupBy { it.band } }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Text("Channel occupancy", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(
                "Counts detected access points per channel — not connected client devices.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        if (networks.isEmpty()) {
            item { EmptyState("Run a Wi-Fi scan first. Channel usage will appear here.") }
        } else if (usage.isEmpty()) {
            item { EmptyState("The current scan did not expose usable channel information.") }
        } else {
            WifiBand.values().forEach { band ->
                val entries = grouped[band].orEmpty()
                if (entries.isNotEmpty()) item(key = band.name) { ChannelBandCard(band, entries) }
            }
        }
    }
}

@Composable
private fun ChannelBandCard(band: WifiBand, entries: List<ChannelUsage>) {
    val minimum = entries.minOfOrNull { it.accessPointCount }
    val leastBusy = entries.filter { it.accessPointCount == minimum }.joinToString(", ") { it.channel.toString() }

    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(band.label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            if (minimum != null) {
                Text(
                    "Lowest count among listed channels: $leastBusy • $minimum AP${if (minimum == 1) "" else "s"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            entries.forEachIndexed { index, usage ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Channel ${usage.channel}")
                    StatusPill(if (usage.accessPointCount == 1) "1 AP" else "${usage.accessPointCount} APs")
                }
                if (index != entries.lastIndex) HorizontalDivider()
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
        item { SectionTitle("Appearance") }
        item {
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Theme", fontWeight = FontWeight.SemiBold)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        WpsxThemeMode.values().forEach { option ->
                            FilterChip(
                                selected = preferences.themeMode == option,
                                onClick = { onPreferencesChanged(preferences.copy(themeMode = option)) },
                                label = {
                                    Text(
                                        when (option) {
                                            WpsxThemeMode.SYSTEM -> "System"
                                            WpsxThemeMode.LIGHT -> "Light"
                                            WpsxThemeMode.DARK -> "Dark"
                                            WpsxThemeMode.OLED -> "OLED"
                                        },
                                    )
                                },
                            )
                        }
                    }
                }
            }
        }

        item { SectionTitle("Scanner") }
        item {
            SettingSwitchRow(
                "WPS networks only",
                "Hide access points that do not advertise WPS in Android scan data.",
                preferences.wpsOnly,
            ) { onPreferencesChanged(preferences.copy(wpsOnly = it)) }
        }
        item {
            SettingSwitchRow(
                "Hide hidden SSIDs",
                "Remove anonymous/hidden networks from the scanner list.",
                preferences.hideHidden,
            ) { onPreferencesChanged(preferences.copy(hideHidden = it)) }
        }
        item {
            SettingSwitchRow(
                "Show raw capabilities",
                "Display Android's raw capability string in AP details.",
                preferences.showRawCapabilities,
            ) { onPreferencesChanged(preferences.copy(showRawCapabilities = it)) }
        }
        item {
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Sort networks", fontWeight = FontWeight.SemiBold)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        NetworkSort.values().forEach { option ->
                            FilterChip(
                                selected = preferences.sort == option,
                                onClick = { onPreferencesChanged(preferences.copy(sort = option)) },
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
            InfoCard(
                title = "WPS metadata",
                lines = listOf(
                    "On Android 11+ the app also parses WPS beacon information elements when the device exposes them.",
                    "That can reveal WPS version, AP Setup Locked state, manufacturer and model without an active WPS exchange.",
                    "If a field is missing, the app reports Unknown instead of guessing.",
                ),
            )
        }
        item {
            InfoCard(
                title = "Vendor lookup",
                lines = listOf(
                    "Manufacturer names prefer WPS beacon metadata when present, then fall back to the local MAC/OUI table.",
                    "The bundled OUI table is intentionally small for now and can be generated from a larger public registry later.",
                ),
            )
        }
        item {
            InfoCard(
                title = "Distance estimate",
                lines = listOf(
                    "Meters are estimated from RSSI. Walls, antennas and transmit power can make real distance very different.",
                ),
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
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
            Column(modifier = Modifier.fillMaxWidth(0.78f)) {
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
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.LocationOn, contentDescription = null)
                Text("Location services are off", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
            Text(
                "Android requires the system Location switch before Wi-Fi scan results are exposed.",
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
                Column(modifier = Modifier.fillMaxWidth(0.62f)) {
                    Text("Device capabilities", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("Root and radio probes", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                FilledTonalButton(onClick = onInspect, enabled = !inspecting && !verifyingRoot) {
                    Icon(Icons.Rounded.Build, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(if (inspecting) "Inspecting" else "Inspect")
                }
            }
        }

        if (snapshot == null) {
            item { EmptyState("Run Inspect to see hardware and root capability status.") }
        } else {
            item { CapabilityCard("Root", snapshot.root.state.name, snapshot.root.detail) }
            item {
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text("Wi-Fi interfaces", fontWeight = FontWeight.SemiBold)
                        if (snapshot.wifiInterfaces.isEmpty()) {
                            Text("None visible to the app", color = MaterialTheme.colorScheme.onSurfaceVariant)
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
            item { CapabilityCard("Monitor mode", snapshot.monitorMode.state.name, snapshot.monitorMode.detail) }
            item { CapabilityCard("Packet injection", snapshot.packetInjection.state.name, snapshot.packetInjection.detail) }
            item {
                FilledTonalButton(onClick = onVerifyRoot, enabled = !inspecting && !verifyingRoot) {
                    Text(if (verifyingRoot) "Verifying root…" else "Verify root access")
                }
            }
        }
    }
}

@Composable
private fun CapabilityCard(title: String, status: String, detail: String) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(title, fontWeight = FontWeight.SemiBold)
                StatusPill(status)
            }
            Text(detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun EmptyState(message: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Text(
            message,
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
    val normalizedBssid = remember(network.bssid) { BssidNormalizer.normalize(network.bssid) }
    val candidates = remember(network.bssid, network.ssid, pinRegistry) {
        pinRegistry.derive(WpsPinInput(bssid = network.bssid, ssid = network.ssid))
    }
    val band = wifiBandForFrequency(network.frequencyMhz)
    val isOpen = network.securityModes.contains(WifiSecurity.OPEN)
    val distance = estimateDistanceMeters(network.rssiDbm)
    val wps = network.wpsInfo

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(ssid, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Bold)
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
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            StatusPill(
                                text = securityLabel(network.securityModes),
                                icon = if (isOpen) Icons.Rounded.LockOpen else Icons.Rounded.Lock,
                                containerColor = if (isOpen) OpenNetworkGreen else null,
                                contentColor = if (isOpen) OpenNetworkOnGreen else null,
                            )
                            StatusPill(band.label)
                            WpsStatusPill(network)
                        }
                        Text("BSSID: ${normalizedBssid ?: network.bssid}")
                        Text("Vendor: ${network.vendorName ?: "Unknown"}")
                        Text("Wi-Fi standard: ${network.wifiStandard.label}")
                        Text("Signal: ${network.rssiDbm} dBm (${signalLabel(network.rssiDbm)})")
                        Text("Approx. distance: ${formatDistance(distance)}")
                        Text("Channel: ${network.channel ?: "unknown"} • ${network.frequencyMhz} MHz")
                    }
                }
            }

            item {
                val lines = mutableListOf<String>()
                if (!network.wpsAdvertised) {
                    lines += "WPS was not advertised by this AP in the beacon data Android exposed."
                } else {
                    lines += "WPS version: ${wps?.effectiveVersion ?: "Unknown"}"
                    lines += "AP Setup Locked: ${when (wps?.apSetupLocked) {
                        true -> "Yes"
                        false -> "No"
                        null -> "Unknown"
                    }}"
                    wps?.manufacturer?.let { lines += "Manufacturer: $it" }
                    wps?.modelName?.let { lines += "Model: $it" }
                    wps?.modelNumber?.let { lines += "Model number: $it" }
                    wps?.deviceName?.let { lines += "Device name: $it" }
                    if (wps == null) {
                        lines += "WPS was advertised, but detailed WPS information elements were not exposed by this device/scan."
                    }
                }
                if (showRawCapabilities) lines += "Raw capabilities: ${network.capabilities.ifBlank { "not reported" }}"
                InfoCard("WPS beacon metadata", lines)
            }

            item {
                val source = when {
                    !wps?.manufacturer.isNullOrBlank() -> "WPS beacon manufacturer attribute"
                    !network.vendorName.isNullOrBlank() -> "local MAC/OUI lookup"
                    else -> "no vendor match"
                }
                InfoCard(
                    "Router identity",
                    listOf(
                        "Vendor source: $source",
                        "Vendor: ${network.vendorName ?: "Unknown"}",
                        "Wi-Fi standard: ${network.wifiStandard.label}",
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
                    "Offline WPS PIN engine",
                    listOf("Registered algorithms: ${pinRegistry.registeredIds.size}", engineStatus),
                )
            }

            item {
                InfoCard(
                    "Active audit backends",
                    listOf(
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
private fun InfoCard(title: String, lines: List<String>) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(title, fontWeight = FontWeight.SemiBold)
            lines.forEach { line ->
                Text(line, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

private fun applyScannerPreferences(
    networks: List<WifiNetwork>,
    preferences: ScannerPreferences,
): List<WifiNetwork> {
    val filtered = networks.filter {
        (!preferences.wpsOnly || it.wpsAdvertised) && (!preferences.hideHidden || it.ssid.isNotBlank())
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

private fun vendorInitials(vendor: String): String {
    val words = vendor.split(' ', '-', '_').filter { it.isNotBlank() }
    return if (words.size >= 2) {
        "${words[0].first()}${words[1].first()}".uppercase()
    } else {
        vendor.filter(Char::isLetterOrDigit).take(2).uppercase().ifBlank { "?" }
    }
}

private fun formatDistance(distanceMeters: Double): String =
    if (distanceMeters < 10.0) {
        val tenths = (distanceMeters * 10.0).roundToInt() / 10.0
        "≈ $tenths m"
    } else {
        "≈ ${distanceMeters.roundToInt()} m"
    }

private fun loadScannerPreferences(context: Context): ScannerPreferences {
    val prefs = context.getSharedPreferences("scanner_preferences", Context.MODE_PRIVATE)
    val sort = runCatching {
        NetworkSort.valueOf(prefs.getString("sort", NetworkSort.SIGNAL.name) ?: NetworkSort.SIGNAL.name)
    }.getOrDefault(NetworkSort.SIGNAL)
    val themeMode = runCatching {
        WpsxThemeMode.valueOf(
            prefs.getString("theme_mode", WpsxThemeMode.SYSTEM.name) ?: WpsxThemeMode.SYSTEM.name,
        )
    }.getOrDefault(WpsxThemeMode.SYSTEM)

    return ScannerPreferences(
        wpsOnly = prefs.getBoolean("wps_only", false),
        hideHidden = prefs.getBoolean("hide_hidden", false),
        showRawCapabilities = prefs.getBoolean("show_raw_capabilities", false),
        sort = sort,
        themeMode = themeMode,
    )
}

private fun saveScannerPreferences(context: Context, preferences: ScannerPreferences) {
    context.getSharedPreferences("scanner_preferences", Context.MODE_PRIVATE)
        .edit()
        .putBoolean("wps_only", preferences.wpsOnly)
        .putBoolean("hide_hidden", preferences.hideHidden)
        .putBoolean("show_raw_capabilities", preferences.showRawCapabilities)
        .putString("sort", preferences.sort.name)
        .putString("theme_mode", preferences.themeMode.name)
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
