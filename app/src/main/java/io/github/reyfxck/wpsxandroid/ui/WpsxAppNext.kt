@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package io.github.reyfxck.wpsxandroid.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.rounded.WifiOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.reyfxck.wpsxandroid.core.audit.AuthorizedTestGate
import io.github.reyfxck.wpsxandroid.core.audit.PixieReadinessEvaluator
import io.github.reyfxck.wpsxandroid.core.audit.PixieReadinessReport
import io.github.reyfxck.wpsxandroid.core.audit.ReadinessLevel
import io.github.reyfxck.wpsxandroid.core.audit.SelectedAuditBackends
import io.github.reyfxck.wpsxandroid.core.capability.DeviceCapabilityProbe
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
import io.github.reyfxck.wpsxandroid.platform.wifi.AndroidWifiConnectionInspector
import io.github.reyfxck.wpsxandroid.platform.wifi.AndroidWifiScanner
import kotlinx.coroutines.launch
import kotlin.math.min
import kotlin.math.roundToInt

private enum class MainTabNext { SCANNER, CHANNELS, DEVICE }
private enum class NetworkSortNext { SIGNAL, NAME, CHANNEL }

private val OpenGreenNext = Color(0xFF2E7D32)
private val OpenOnGreenNext = Color.White

private data class ScannerPreferencesNext(
    val wpsOnly: Boolean = false,
    val hideHidden: Boolean = false,
    val showRawCapabilities: Boolean = false,
    val sort: NetworkSortNext = NetworkSortNext.SIGNAL,
    val themeMode: WpsxThemeMode = WpsxThemeMode.SYSTEM,
)

@Composable
fun WpsxAppNext() {
    val context = LocalContext.current
    var preferences by remember(context) { mutableStateOf(loadScannerPreferencesNext(context)) }

    WpsxTheme(mode = preferences.themeMode) {
        val scanner = remember(context) { AndroidWifiScanner(context.applicationContext) }
        val capabilityProbe = remember { AndroidDeviceCapabilityProbe() }
        val connectionInspector = remember(context) { AndroidWifiConnectionInspector(context.applicationContext) }
        val pinRegistry = remember { WpsPinAlgorithmRegistry(emptyList()) }
        val scope = rememberCoroutineScope()

        var selectedTab by remember { mutableStateOf(MainTabNext.SCANNER) }
        var showSettings by remember { mutableStateOf(false) }
        var scanResult by remember { mutableStateOf<ScanResult?>(null) }
        var scanning by remember { mutableStateOf(false) }
        var selectedNetwork by remember { mutableStateOf<WifiNetwork?>(null) }
        var locationRequired by remember { mutableStateOf(false) }
        var wifiRequired by remember { mutableStateOf(false) }
        var showLocationDialog by remember { mutableStateOf(false) }
        var showWifiDialog by remember { mutableStateOf(false) }
        var capabilitySnapshot by remember { mutableStateOf<DeviceCapabilitySnapshot?>(null) }
        var inspectingCapabilities by remember { mutableStateOf(false) }
        var verifyingRoot by remember { mutableStateOf(false) }

        fun updatePreferences(newValue: ScannerPreferencesNext) {
            preferences = newValue
            saveScannerPreferencesNext(context, newValue)
        }

        fun runScan() {
            scanning = true
            selectedNetwork = null
            scope.launch {
                scanResult = scanner.scan()
                wifiRequired = !isWifiEnabledNext(context)
                locationRequired = !isLocationServiceEnabledNext(context)
                scanning = false
            }
        }

        fun scanIfSystemReady(showDialogIfDisabled: Boolean = true) {
            if (!isWifiEnabledNext(context)) {
                wifiRequired = true
                locationRequired = false
                if (showDialogIfDisabled) showWifiDialog = true
                return
            }
            wifiRequired = false

            if (!isLocationServiceEnabledNext(context)) {
                locationRequired = true
                if (showDialogIfDisabled) showLocationDialog = true
                return
            }
            locationRequired = false
            showWifiDialog = false
            showLocationDialog = false
            runScan()
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
            contract = ActivityResultContracts.RequestMultiplePermissions(),
        ) { permissions ->
            if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
                scanIfSystemReady()
            } else {
                scanResult = ScanResult.PermissionRequired(setOf(Manifest.permission.ACCESS_FINE_LOCATION))
            }
        }

        val locationSettingsLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult(),
        ) {
            scanIfSystemReady(showDialogIfDisabled = false)
        }

        val wifiSettingsLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult(),
        ) {
            scanIfSystemReady(showDialogIfDisabled = false)
        }

        fun openLocationSettings() {
            showLocationDialog = false
            locationSettingsLauncher.launch(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        }

        fun openWifiSettings() {
            showWifiDialog = false
            val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                Intent(Settings.Panel.ACTION_WIFI)
            } else {
                Intent(Settings.ACTION_WIFI_SETTINGS)
            }
            wifiSettingsLauncher.launch(intent)
        }

        fun openAppSettings() {
            context.startActivity(
                Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:${context.packageName}"),
                ),
            )
        }

        fun requestScan() {
            val preciseGranted = context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
            if (preciseGranted) {
                scanIfSystemReady()
            } else {
                permissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                    ),
                )
            }
        }

        if (showWifiDialog) {
            AlertDialog(
                onDismissRequest = { showWifiDialog = false },
                icon = { Icon(Icons.Rounded.WifiOff, contentDescription = null) },
                title = { Text("Wi-Fi is off") },
                text = { Text("Turn Wi-Fi on before scanning nearby networks.") },
                confirmButton = { Button(onClick = ::openWifiSettings) { Text("Open Wi-Fi") } },
                dismissButton = { TextButton(onClick = { showWifiDialog = false }) { Text("Not now") } },
            )
        }

        if (showLocationDialog) {
            AlertDialog(
                onDismissRequest = { showLocationDialog = false },
                icon = { Icon(Icons.Rounded.LocationOn, contentDescription = null) },
                title = { Text("Turn on Location") },
                text = {
                    Text(
                        "Android requires the system Location switch for Wi-Fi scan results. " +
                            "The Exact/Approximate selector is controlled by Android.",
                    )
                },
                confirmButton = { Button(onClick = ::openLocationSettings) { Text("Open settings") } },
                dismissButton = { TextButton(onClick = { showLocationDialog = false }) { Text("Not now") } },
            )
        }

        selectedNetwork?.let { network ->
            NetworkDetailsScreenNext(
                network = network,
                pinRegistry = pinRegistry,
                capabilityProbe = capabilityProbe,
                connectionInspector = connectionInspector,
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
                            selected = selectedTab == MainTabNext.SCANNER,
                            onClick = { selectedTab = MainTabNext.SCANNER },
                            icon = { Icon(Icons.Rounded.Wifi, contentDescription = null) },
                            label = { Text("Scanner") },
                        )
                        NavigationBarItem(
                            selected = selectedTab == MainTabNext.CHANNELS,
                            onClick = { selectedTab = MainTabNext.CHANNELS },
                            icon = { Icon(Icons.Rounded.BarChart, contentDescription = null) },
                            label = { Text("Channels") },
                        )
                        NavigationBarItem(
                            selected = selectedTab == MainTabNext.DEVICE,
                            onClick = { selectedTab = MainTabNext.DEVICE },
                            icon = { Icon(Icons.Rounded.Build, contentDescription = null) },
                            label = { Text("Device") },
                        )
                    }
                }
            },
        ) { padding ->
            if (showSettings) {
                SettingsScreenNext(
                    modifier = Modifier.padding(padding),
                    preferences = preferences,
                    onPreferencesChanged = ::updatePreferences,
                )
            } else {
                when (selectedTab) {
                    MainTabNext.SCANNER -> ScannerScreenNext(
                        modifier = Modifier.padding(padding),
                        scanResult = scanResult,
                        scanning = scanning,
                        locationRequired = locationRequired,
                        wifiRequired = wifiRequired,
                        preferences = preferences,
                        onScan = ::requestScan,
                        onOpenLocationSettings = ::openLocationSettings,
                        onOpenWifiSettings = ::openWifiSettings,
                        onOpenAppSettings = ::openAppSettings,
                        onNetworkSelected = { selectedNetwork = it },
                    )
                    MainTabNext.CHANNELS -> ChannelsScreenNext(
                        modifier = Modifier.padding(padding),
                        networks = currentNetworks,
                    )
                    MainTabNext.DEVICE -> DeviceScreenNext(
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
private fun ScannerScreenNext(
    modifier: Modifier,
    scanResult: ScanResult?,
    scanning: Boolean,
    locationRequired: Boolean,
    wifiRequired: Boolean,
    preferences: ScannerPreferencesNext,
    onScan: () -> Unit,
    onOpenLocationSettings: () -> Unit,
    onOpenWifiSettings: () -> Unit,
    onOpenAppSettings: () -> Unit,
    onNetworkSelected: (WifiNetwork) -> Unit,
) {
    val visibleNetworks = remember(scanResult, preferences) {
        applyScannerPreferencesNext((scanResult as? ScanResult.Success)?.networks.orEmpty(), preferences)
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
                    Text("Nearby networks", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
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

        if (wifiRequired) item { WifiRequiredCardNext(onOpenWifiSettings) }
        if (locationRequired) item { LocationRequiredCardNext(onOpenLocationSettings) }

        when (val result = scanResult) {
            null -> item { EmptyStateNext("Tap Scan to discover nearby access points.") }
            is ScanResult.PermissionRequired -> item {
                PermissionRequiredCardNext(onOpenAppSettings)
            }
            is ScanResult.Unavailable -> item { EmptyStateNext(result.reason) }
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
                    item { EmptyStateNext("No networks match the current scanner filters.") }
                } else {
                    items(
                        items = visibleNetworks,
                        key = { "${it.bssid}-${it.frequencyMhz}" },
                    ) { network ->
                        CompactNetworkCardNext(network = network, onClick = { onNetworkSelected(network) })
                    }
                }
            }
        }
    }
}

@Composable
private fun CompactNetworkCardNext(network: WifiNetwork, onClick: () -> Unit) {
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
                SignalStrengthIconNext(network.rssiDbm, hidden, isOpen)
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
                        formatDistanceNext(distance),
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
                StatusPillNext(
                    text = securityLabelNext(network.securityModes),
                    icon = if (isOpen) Icons.Rounded.LockOpen else Icons.Rounded.Lock,
                    containerColor = if (isOpen) OpenGreenNext else null,
                    contentColor = if (isOpen) OpenOnGreenNext else null,
                )
                StatusPillNext(text = "${band.label} • ch ${network.channel ?: "?"}")
                WpsStatusPillNext(network)
            }
        }
    }
}

@Composable
private fun SignalStrengthIconNext(rssiDbm: Int, hidden: Boolean, isOpen: Boolean) {
    if (hidden) {
        Icon(
            imageVector = Icons.Rounded.VisibilityOff,
            contentDescription = "Hidden SSID",
            modifier = Modifier.size(30.dp),
            tint = if (isOpen) OpenGreenNext else MaterialTheme.colorScheme.primary,
        )
        return
    }

    val level = signalLevelNext(rssiDbm)
    val activeColor = if (isOpen) OpenGreenNext else MaterialTheme.colorScheme.primary
    val inactiveColor = activeColor.copy(alpha = 0.18f)

    Canvas(modifier = Modifier.size(34.dp)) {
        val d = min(size.width, size.height)
        val center = Offset(size.width / 2f, size.height * 0.82f)
        val stroke = Stroke(width = d * 0.105f, cap = StrokeCap.Round)
        val radii = listOf(d * 0.19f, d * 0.30f, d * 0.42f)

        drawCircle(color = activeColor, radius = d * 0.065f, center = center)
        radii.forEachIndexed { index, radius ->
            drawArc(
                color = if (level >= index + 2) activeColor else inactiveColor,
                startAngle = 225f,
                sweepAngle = 90f,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2f, radius * 2f),
                style = stroke,
            )
        }
    }
}

@Composable
private fun WpsStatusPillNext(network: WifiNetwork) {
    if (!network.wpsAdvertised) {
        StatusPillNext(
            text = "WPS —",
            icon = Icons.Rounded.WifiOff,
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
        )
        return
    }

    val versionSuffix = network.wpsInfo?.effectiveVersion?.let { " $it" }.orEmpty()
    when (network.wpsInfo?.apSetupLocked) {
        true -> StatusPillNext(
            text = "WPS locked",
            icon = Icons.Rounded.Lock,
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
        )
        false -> StatusPillNext(
            text = "WPS$versionSuffix",
            icon = Icons.Rounded.LockOpen,
            containerColor = OpenGreenNext,
            contentColor = OpenOnGreenNext,
        )
        null -> StatusPillNext(text = "WPS$versionSuffix", icon = Icons.Rounded.Wifi)
    }
}

@Composable
private fun StatusPillNext(
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
private fun NetworkDetailsScreenNext(
    network: WifiNetwork,
    pinRegistry: WpsPinAlgorithmRegistry,
    capabilityProbe: DeviceCapabilityProbe,
    connectionInspector: AndroidWifiConnectionInspector,
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
    val scope = rememberCoroutineScope()

    var showWpsDetails by remember(network.bssid) { mutableStateOf(false) }
    var showTechnical by remember(network.bssid) { mutableStateOf(false) }
    var showAuthorizationDialog by remember(network.bssid) { mutableStateOf(false) }
    var authorizationChecked by remember(network.bssid) { mutableStateOf(false) }
    var testing by remember(network.bssid) { mutableStateOf(false) }
    var testStatus by remember(network.bssid) { mutableStateOf<String?>(null) }
    var testReport by remember(network.bssid) { mutableStateOf<PixieReadinessReport?>(null) }

    fun requestAuthorizedTest() {
        val gate = AuthorizedTestGate.evaluate(network, connectionInspector.connectedBssid())
        testStatus = gate.reason
        if (gate.allowed) {
            authorizationChecked = false
            showAuthorizationDialog = true
        }
    }

    fun runAuthorizedPreflight() {
        if (testing) return
        showAuthorizationDialog = false
        testing = true
        testReport = null
        scope.launch {
            val snapshot = capabilityProbe.verifyRoot()
            val report = PixieReadinessEvaluator.evaluate(network, snapshot)
            testReport = report
            testStatus = if (report.canProceedToLabBackend) {
                "Radio prerequisites look ready. Native Pixie/Reaver execution is still not bundled in this APK."
            } else {
                "Preflight found blockers or unknown capabilities. See the checks below."
            }
            testing = false
        }
    }

    if (showAuthorizationDialog) {
        AlertDialog(
            onDismissRequest = { showAuthorizationDialog = false },
            title = { Text("Authorized WPS test") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "This mode is restricted to the AP currently connected on this device. " +
                            "Use it only on a network you own or have explicit permission to assess.",
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = authorizationChecked,
                            onCheckedChange = { authorizationChecked = it },
                        )
                        Text(
                            "I own this network or have explicit permission.",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = ::runAuthorizedPreflight, enabled = authorizationChecked) {
                    Text("Continue")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAuthorizationDialog = false }) { Text("Cancel") }
            },
        )
    }

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
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            StatusPillNext(
                                text = securityLabelNext(network.securityModes),
                                icon = if (isOpen) Icons.Rounded.LockOpen else Icons.Rounded.Lock,
                                containerColor = if (isOpen) OpenGreenNext else null,
                                contentColor = if (isOpen) OpenOnGreenNext else null,
                            )
                            StatusPillNext(band.label)
                            WpsStatusPillNext(network)
                        }
                        CompactValueRowNext("BSSID", normalizedBssid ?: network.bssid)
                        CompactValueRowNext("Router", network.vendorName ?: "Unknown")
                        CompactValueRowNext(
                            "Radio",
                            "${network.wifiStandard.label} • ${network.rssiDbm} dBm • ${formatDistanceNext(distance)}",
                        )
                        CompactValueRowNext(
                            "Channel",
                            "${network.channel ?: "?"} • ${network.frequencyMhz} MHz",
                        )
                    }
                }
            }

            item {
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(5.dp),
                    ) {
                        Text("WPS", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                        Text(
                            buildString {
                                append(wps?.effectiveVersion?.let { "Version $it" } ?: if (network.wpsAdvertised) "Version unknown" else "Not advertised")
                                append(" • ")
                                append(
                                    when (wps?.apSetupLocked) {
                                        true -> "Locked"
                                        false -> "Unlocked"
                                        null -> "Lock unknown"
                                    },
                                )
                                wps?.setupStateLabel?.let { append(" • $it") }
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        val model = listOfNotNull(wps?.modelName, wps?.modelNumber)
                            .filter { it.isNotBlank() }
                            .joinToString(" • ")
                        if (model.isNotBlank()) {
                            Text(
                                model,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        TextButton(onClick = { showWpsDetails = !showWpsDetails }) {
                            Text(if (showWpsDetails) "Less details" else "More WPS details")
                        }
                        if (showWpsDetails) {
                            CompactValueRowNext("Manufacturer", wps?.manufacturer ?: network.vendorName ?: "Unknown")
                            wps?.serialNumber?.let { CompactValueRowNext("Serial", it) }
                            wps?.deviceName?.let { CompactValueRowNext("Device", it) }
                            if (!wps?.configMethods.isNullOrEmpty()) {
                                CompactValueRowNext("Methods", wps?.configMethods.orEmpty().joinToString(", "))
                            }
                        }
                    }
                }
            }

            item {
                AuthorizedTestCardNext(
                    testing = testing,
                    status = testStatus,
                    report = testReport,
                    onStart = ::requestAuthorizedTest,
                    onShowTechnical = { showTechnical = !showTechnical },
                    showTechnical = showTechnical,
                )
            }

            item {
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.fillMaxWidth(0.72f)) {
                            Text("Offline PIN engine", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                            Text(
                                when {
                                    normalizedBssid == null -> "Invalid BSSID"
                                    pinRegistry.registeredIds.isEmpty() -> "No vendor algorithms bundled yet"
                                    candidates.isEmpty() -> "No candidate from registered algorithms"
                                    else -> "${candidates.size} candidate(s) from ${pinRegistry.registeredIds.size} algorithm(s)"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        StatusPillNext("${pinRegistry.registeredIds.size} algos")
                    }
                }
            }

            if (showTechnical) {
                item {
                    val technicalLines = mutableListOf(
                        "Pixie: ${SelectedAuditBackends.PIXIEWPS_REPOSITORY}@${SelectedAuditBackends.PIXIEWPS_REVISION.take(12)}",
                        "Reaver: ${SelectedAuditBackends.REAVER_REPOSITORY}@${SelectedAuditBackends.REAVER_REVISION.take(12)}",
                        "Native on-air backend: not bundled in this APK",
                    )
                    if (showRawCapabilities) {
                        technicalLines += "Raw: ${network.capabilities.ifBlank { "not reported" }}"
                    }
                    InfoCardNext("Technical", technicalLines)
                }
            }
        }
    }
}

@Composable
private fun AuthorizedTestCardNext(
    testing: Boolean,
    status: String?,
    report: PixieReadinessReport?,
    onStart: () -> Unit,
    onShowTechnical: () -> Unit,
    showTechnical: Boolean,
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.fillMaxWidth(0.60f)) {
                    Text("Authorized WPS test", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    Text(
                        "Only for the AP currently connected on this device.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                FilledTonalButton(onClick = onStart, enabled = !testing) {
                    if (testing) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Rounded.Build, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                    Spacer(Modifier.width(5.dp))
                    Text(if (testing) "Testing" else "Start")
                }
            }

            status?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            report?.let { current ->
                val important = current.checks.filter {
                    it.name in setOf("WPS", "WPS lock", "Root", "Wi-Fi interface", "Monitor mode", "Packet injection")
                }
                important.forEach { CompactReadinessRowNext(it.name, it.level, it.detail) }

                Button(
                    onClick = {},
                    enabled = false,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        if (current.canProceedToLabBackend) {
                            "Run Pixie Dust — backend pending"
                        } else {
                            "Run Pixie Dust — prerequisites blocked"
                        },
                    )
                }
            }

            TextButton(onClick = onShowTechnical) {
                Text(if (showTechnical) "Hide technical details" else "Technical details")
            }
        }
    }
}

@Composable
private fun CompactReadinessRowNext(name: String, level: ReadinessLevel, detail: String) {
    val container = when (level) {
        ReadinessLevel.PASS -> OpenGreenNext
        ReadinessLevel.WARNING -> MaterialTheme.colorScheme.secondaryContainer
        ReadinessLevel.BLOCKED -> MaterialTheme.colorScheme.errorContainer
    }
    val content = when (level) {
        ReadinessLevel.PASS -> OpenOnGreenNext
        ReadinessLevel.WARNING -> MaterialTheme.colorScheme.onSecondaryContainer
        ReadinessLevel.BLOCKED -> MaterialTheme.colorScheme.onErrorContainer
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(7.dp),
        verticalAlignment = Alignment.Top,
    ) {
        StatusPillNext(
            text = when (level) {
                ReadinessLevel.PASS -> "PASS"
                ReadinessLevel.WARNING -> "CHECK"
                ReadinessLevel.BLOCKED -> "BLOCKED"
            },
            containerColor = container,
            contentColor = content,
        )
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(name, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            Text(
                detail,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun CompactValueRowNext(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            "$label:",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(78.dp),
        )
        Text(
            value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun ChannelsScreenNext(modifier: Modifier, networks: List<WifiNetwork>) {
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
                "Detected APs per channel",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (networks.isEmpty()) {
            item { EmptyStateNext("Run a Wi-Fi scan first.") }
        } else {
            WifiBand.values().forEach { band ->
                val entries = grouped[band].orEmpty()
                if (entries.isNotEmpty()) item(key = band.name) { ChannelBandCardNext(band, entries) }
            }
        }
    }
}

@Composable
private fun ChannelBandCardNext(band: WifiBand, entries: List<ChannelUsage>) {
    val minimum = entries.minOfOrNull { it.accessPointCount }
    val leastBusy = entries.filter { it.accessPointCount == minimum }.joinToString(", ") { it.channel.toString() }

    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(band.label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            if (minimum != null) {
                Text(
                    "Lowest detected count: ch $leastBusy • $minimum AP${if (minimum == 1) "" else "s"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            entries.forEachIndexed { index, usageEntry ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Channel ${usageEntry.channel}", style = MaterialTheme.typography.bodySmall)
                    StatusPillNext(if (usageEntry.accessPointCount == 1) "1 AP" else "${usageEntry.accessPointCount} APs")
                }
                if (index != entries.lastIndex) HorizontalDivider()
            }
        }
    }
}

@Composable
private fun DeviceScreenNext(
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
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.fillMaxWidth(0.62f)) {
                    Text("Device capabilities", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("Root + radio", style = MaterialTheme.typography.bodySmall)
                }
                FilledTonalButton(onClick = onInspect, enabled = !inspecting && !verifyingRoot) {
                    Text(if (inspecting) "Inspecting" else "Inspect")
                }
            }
        }

        if (snapshot == null) {
            item { EmptyStateNext("Run Inspect to see device capabilities.") }
        } else {
            item { CapabilityCardNext("Root", snapshot.root.state.name, snapshot.root.detail) }
            item {
                CapabilityCardNext(
                    "Wi-Fi interfaces",
                    if (snapshot.wifiInterfaces.isEmpty()) "NONE" else "${snapshot.wifiInterfaces.size} FOUND",
                    if (snapshot.wifiInterfaces.isEmpty()) {
                        "No Wi-Fi interface found through Android/sysfs yet. Verify root for the rooted fallback."
                    } else {
                        snapshot.wifiInterfaces.joinToString { "${it.name}${if (it.isUp) " (up)" else ""}" }
                    },
                )
            }
            item { CapabilityCardNext("Monitor mode", snapshot.monitorMode.state.name, snapshot.monitorMode.detail) }
            item { CapabilityCardNext("Packet injection", snapshot.packetInjection.state.name, snapshot.packetInjection.detail) }
            item {
                FilledTonalButton(onClick = onVerifyRoot, enabled = !inspecting && !verifyingRoot) {
                    Text(if (verifyingRoot) "Verifying root…" else "Verify root + radio")
                }
            }
        }
    }
}

@Composable
private fun CapabilityCardNext(title: String, status: String, detail: String) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                StatusPillNext(status)
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
private fun SettingsScreenNext(
    modifier: Modifier,
    preferences: ScannerPreferencesNext,
    onPreferencesChanged: (ScannerPreferencesNext) -> Unit,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item { Text("Appearance", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
        item {
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Theme", fontWeight = FontWeight.SemiBold)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        WpsxThemeMode.values().forEach { option ->
                            FilterChip(
                                selected = preferences.themeMode == option,
                                onClick = { onPreferencesChanged(preferences.copy(themeMode = option)) },
                                label = { Text(option.name.lowercase().replaceFirstChar(Char::uppercase)) },
                            )
                        }
                    }
                }
            }
        }
        item { Text("Scanner", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
        item {
            SettingSwitchRowNext(
                "WPS networks only",
                "Hide APs that do not advertise WPS.",
                preferences.wpsOnly,
            ) { onPreferencesChanged(preferences.copy(wpsOnly = it)) }
        }
        item {
            SettingSwitchRowNext(
                "Hide hidden SSIDs",
                "Remove anonymous networks from the list.",
                preferences.hideHidden,
            ) { onPreferencesChanged(preferences.copy(hideHidden = it)) }
        }
        item {
            SettingSwitchRowNext(
                "Show raw capabilities",
                "Include Android's raw capability string in technical details.",
                preferences.showRawCapabilities,
            ) { onPreferencesChanged(preferences.copy(showRawCapabilities = it)) }
        }
        item {
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Sort networks", fontWeight = FontWeight.SemiBold)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        NetworkSortNext.values().forEach { option ->
                            FilterChip(
                                selected = preferences.sort == option,
                                onClick = { onPreferencesChanged(preferences.copy(sort = option)) },
                                label = { Text(option.name.lowercase().replaceFirstChar(Char::uppercase)) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingSwitchRowNext(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.fillMaxWidth(0.78f)) {
                Text(title, fontWeight = FontWeight.SemiBold)
                Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}

@Composable
private fun PermissionRequiredCardNext(onOpenAppSettings: () -> Unit) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Text("Precise location is required", fontWeight = FontWeight.SemiBold)
            Text(
                "Android controls the Exact/Approximate choice. Select Exact for Wi-Fi scan results. " +
                    "The app now requests Approximate + Exact together as Android requires.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            TextButton(onClick = onOpenAppSettings) { Text("Open app settings") }
        }
    }
}

@Composable
private fun WifiRequiredCardNext(onOpenWifiSettings: () -> Unit) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.WifiOff, contentDescription = null)
                Text("Wi-Fi is off", fontWeight = FontWeight.SemiBold)
            }
            Text(
                "Turn Wi-Fi on before scanning.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(onClick = onOpenWifiSettings) { Text("Open Wi-Fi") }
        }
    }
}

@Composable
private fun LocationRequiredCardNext(onOpenLocationSettings: () -> Unit) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.LocationOn, contentDescription = null)
                Text("Location services are off", fontWeight = FontWeight.SemiBold)
            }
            Text(
                "Android requires the system Location switch for Wi-Fi scan results.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(onClick = onOpenLocationSettings) { Text("Open Location") }
        }
    }
}

@Composable
private fun InfoCardNext(title: String, lines: List<String>) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            lines.forEach { line ->
                Text(line, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun EmptyStateNext(message: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Text(
            message,
            modifier = Modifier.padding(12.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun applyScannerPreferencesNext(
    networks: List<WifiNetwork>,
    preferences: ScannerPreferencesNext,
): List<WifiNetwork> {
    val filtered = networks.filter {
        (!preferences.wpsOnly || it.wpsAdvertised) && (!preferences.hideHidden || it.ssid.isNotBlank())
    }
    return when (preferences.sort) {
        NetworkSortNext.SIGNAL -> filtered.sortedByDescending { it.rssiDbm }
        NetworkSortNext.NAME -> filtered.sortedBy { it.ssid.ifBlank { "~" }.lowercase() }
        NetworkSortNext.CHANNEL -> filtered.sortedWith(
            compareBy<WifiNetwork>(
                { wifiBandForFrequency(it.frequencyMhz).ordinal },
                { it.channel ?: Int.MAX_VALUE },
                { -it.rssiDbm },
            ),
        )
    }
}

private fun securityLabelNext(modes: Set<WifiSecurity>): String {
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

private fun signalLevelNext(rssiDbm: Int): Int = when {
    rssiDbm >= -55 -> 4
    rssiDbm >= -67 -> 3
    rssiDbm >= -75 -> 2
    else -> 1
}

private fun formatDistanceNext(distanceMeters: Double): String =
    if (distanceMeters < 10.0) {
        val tenths = (distanceMeters * 10.0).roundToInt() / 10.0
        "≈ $tenths m"
    } else {
        "≈ ${distanceMeters.roundToInt()} m"
    }

private fun loadScannerPreferencesNext(context: Context): ScannerPreferencesNext {
    val prefs = context.getSharedPreferences("scanner_preferences", Context.MODE_PRIVATE)
    val sort = runCatching {
        NetworkSortNext.valueOf(prefs.getString("sort", NetworkSortNext.SIGNAL.name) ?: NetworkSortNext.SIGNAL.name)
    }.getOrDefault(NetworkSortNext.SIGNAL)
    val themeMode = runCatching {
        WpsxThemeMode.valueOf(
            prefs.getString("theme_mode", WpsxThemeMode.SYSTEM.name) ?: WpsxThemeMode.SYSTEM.name,
        )
    }.getOrDefault(WpsxThemeMode.SYSTEM)

    return ScannerPreferencesNext(
        wpsOnly = prefs.getBoolean("wps_only", false),
        hideHidden = prefs.getBoolean("hide_hidden", false),
        showRawCapabilities = prefs.getBoolean("show_raw_capabilities", false),
        sort = sort,
        themeMode = themeMode,
    )
}

private fun saveScannerPreferencesNext(context: Context, preferences: ScannerPreferencesNext) {
    context.getSharedPreferences("scanner_preferences", Context.MODE_PRIVATE)
        .edit()
        .putBoolean("wps_only", preferences.wpsOnly)
        .putBoolean("hide_hidden", preferences.hideHidden)
        .putBoolean("show_raw_capabilities", preferences.showRawCapabilities)
        .putString("sort", preferences.sort.name)
        .putString("theme_mode", preferences.themeMode.name)
        .apply()
}

private fun isLocationServiceEnabledNext(context: Context): Boolean =
    context.getSystemService(LocationManager::class.java)?.isLocationEnabled == true

private fun isWifiEnabledNext(context: Context): Boolean =
    context.getSystemService(WifiManager::class.java)?.isWifiEnabled == true
