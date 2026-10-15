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

## Permission design

The manifest declares Wi-Fi state permissions and the modern nearby-Wi-Fi permission. Runtime permission behavior will be implemented and tested as part of Phase 1 because requirements differ by Android version and API used.

Do not add broad storage permissions for logs. Use app-private storage or the system document picker for exports.
