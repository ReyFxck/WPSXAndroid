# Device capability layer

The device capability layer keeps root and low-level Wi-Fi assumptions out of the Compose UI.

## Current probes

- passive `su` path visibility check
- explicit root verification using the fixed command `su -c id`
- Wi-Fi-like interface discovery through `NetworkInterface`
- `/sys/class/net/<name>/wireless` used only as an additional classification hint when visible
- read-only `iw dev` parsing to detect an already-exposed monitor interface
- read-only `iw phy` parsing to detect generic driver-advertised monitor support
- capability-driven monitor backend classification so the app does not assume every device creates `wlan0mon`

The root verification command is fixed in code and does not include AP, SSID, BSSID, or other user-controlled strings.

The monitor-mode probes are read-only. They do not change interface type, bring interfaces down/up, capture packets, transmit frames, or modify radio state.

## Monitor backend model

Different Android kernels and Wi-Fi drivers expose monitor capability differently. WPSXAndroid now represents that explicitly instead of treating one Linux interface layout as universal.

Current classifier inputs include:

- an already-existing interface whose reported type is `monitor`
- generic monitor support reported by the driver's supported-interface-modes data
- a reserved same-interface hint for platform-specific adapters where a driver exposes monitor capability without creating a second interface

The pure classifier chooses the strongest verified capability while preserving unknown alternatives so the UI can explain what still requires device verification. Vendor/device-specific evidence belongs in platform adapters, not in Compose UI or the generic core.

## Important limits

Finding `su` is only evidence that root may be available. It is not a successful root check.

A successful root check does not imply monitor mode or packet injection.

If the driver reports monitor support, the app may report monitor capability as available. That still does not prove that a particular interface transition works, that channel control works, or that packet injection works.

Packet injection remains independently assessed and must not be inferred from monitor mode alone.

Interface discovery also does not change interface mode or perform packet capture. It only reports interfaces visible to the app.

## Verification policy

- pure output parsing, backend selection, and interface classification have unit tests
- Android process/interface behavior must also be checked on physical devices
- monitor-mode and injection status must never be inferred from root alone
- device-specific monitor exposure paths must fail with an explanation instead of silently assuming a `wlan0mon`-style interface
