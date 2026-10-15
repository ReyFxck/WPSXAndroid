# Android Wi-Fi limitations

Modern Android deliberately restricts Wi-Fi scanning and low-level 802.11 access.

WPSXAndroid therefore treats capabilities separately:

- Android framework scan visibility
- root access
- Wi-Fi interface visibility
- monitor mode
- packet capture
- packet injection

A device having root does **not** automatically mean its Wi-Fi firmware/driver supports monitor mode or injection.

The app must report the capability it actually verified and the reason a backend is unavailable.

## Framework scanning

The passive scanner uses `WifiManager.startScan()` and waits for `SCAN_RESULTS_AVAILABLE_ACTION`. Android can reject/throttle a requested scan, in which case WPSXAndroid may show the latest cached results and marks them as not fresh.

On modern Android, `startScan()` and `getScanResults()` still require `ACCESS_FINE_LOCATION`, and system Location services must be enabled. This remains true even though other Wi-Fi APIs moved to `NEARBY_WIFI_DEVICES` on Android 13+.

The UI requests precise-location permission only when the user starts a framework Wi-Fi scan.

## WPS visibility

Android's `ScanResult.capabilities` may advertise a `WPS` token on some devices/firmware. WPSXAndroid currently treats that as a heuristic signal only. A missing token must not be interpreted as proof that an AP has WPS disabled.

## Low-level features

Framework scanning is independent from monitor mode and packet injection. Root alone does not provide either capability. Future native backends must probe each requirement independently and report unsupported hardware honestly.

Do not add broad storage permissions for logs. Use app-private storage or the system document picker for exports.
