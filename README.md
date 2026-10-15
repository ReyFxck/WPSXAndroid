# WPSXAndroid

WPSXAndroid is an open-source Android project for **authorized Wi-Fi/WPS security testing, research, and lab use**.

The project aims to provide a modern Android interface for Wi-Fi/WPS discovery, known WPS PIN derivation algorithms, Pixie Dust auditing, and a Reaver-compatible backend where the device, kernel, driver, and permissions support the required low-level Wi-Fi capabilities.

> **Authorized use only.** Use WPSXAndroid only on networks and devices you own or have explicit permission to test. The project is intended for security research, education, interoperability testing, and defensive auditing. You are responsible for complying with applicable laws and rules.

## Project status

Early bootstrap. See [STATUS.md](STATUS.md) and [ROADMAP.md](ROADMAP.md).

## Planned v1 scope

- [ ] Android Wi-Fi scanner
- [ ] WPS capability/status detection where Android exposes enough information
- [ ] AP details: SSID, BSSID, RSSI, frequency/channel, security capabilities
- [ ] Local WPS PIN derivation engine
- [ ] Belkin-family algorithms
- [ ] Arcadyan-family algorithms
- [ ] Additional documented WPS PIN algorithms with tests
- [ ] Pixie Dust audit backend
- [ ] Reaver-compatible audit backend
- [ ] Root/capability detection
- [ ] Monitor-mode / injection capability detection
- [ ] Live logs and saved audit sessions
- [ ] Clear separation between passive scanning, offline calculations, and active authorized tests

## Architecture

The UI and Android-facing code are written in Kotlin with Jetpack Compose. Security engines are isolated behind interfaces so native/root-dependent backends do not leak into UI code.

See [ARCHITECTURE.md](ARCHITECTURE.md) for the design rules.

## Development rules

1. Do not mark a feature complete until it is implemented **and verified**.
2. Keep algorithms deterministic and covered by test vectors.
3. Keep Android framework access behind platform adapters.
4. Keep root/native execution behind explicit backend interfaces.
5. Do not silently claim monitor mode, packet injection, Pixie Dust, or Reaver support on unsupported hardware.
6. Prefer reproducible builds and pinned dependency versions.

## Build target

The bootstrap targets modern Android with `compileSdk 37`, AGP 9.4, Gradle 9.6, and Jetpack Compose. Exact toolchain notes live in [docs/build.md](docs/build.md).

## License

A license has not yet been selected. Until one is added, normal copyright rules apply to this repository.
