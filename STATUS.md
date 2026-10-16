# Status

Allowed states:

- `Not implemented`
- `[IN PROGRESS]`
- `[BLOCKED]`
- `Implemented AND verified`

Do not mark work complete only because a file or placeholder exists.

## Project foundation

- [x] Repository initialized — **Implemented AND verified**
- [x] Android/Compose skeleton — **Implemented AND verified**
- [x] Core scanner contract — **Implemented AND verified**
- [x] WPS PIN algorithm contract — **Implemented; no production algorithms yet**
- [x] Active audit backend contract — **Implemented; no active backend yet**
- [x] CI build passes on GitHub Actions — **Implemented AND verified**
- [x] Debug APK uploaded as CI artifact — **Implemented AND verified**

Verification baseline: Android CI run #4 successfully installed the Android 17 SDK, built `:app:assembleDebug`, ran `:app:testDebugUnitTest`, and uploaded the debug APK artifact. Android CI run #6 also verified the passive scanner build and unit tests. Android CI run #8 verified the WPS PIN core normalization, checksum, registry tests, app build, and debug APK upload. Android CI run #10 verified the AP details navigation/UI integration, existing tests, and debug APK upload. Android CI run #14 verified the device capability models, root identity parser, Wi-Fi interface classifier, Android adapters, capability UI, existing tests, and debug APK upload. Android CI run #17 verified the read-only `iw phy` monitor-mode parser/probe, existing tests, app build, and debug APK upload. Android CI run #20 verified the structured executable lifecycle, process-argument policy tests, packet-injection readiness assessment, root/`iw` probe refactor, complete app build, unit tests, and debug APK upload. Android CI run #22 verified the Material 3 Expressive UI refresh, scanner/device tab split, Location-settings flow, adaptive/themed launcher icon resources, complete app build, unit tests, and debug APK upload. Android CI run #27 verified the compact scanner UI, hidden/open/security/WPS/band indicators, Wi-Fi band helpers, per-channel AP occupancy aggregation/tests, Channels tab, persistent scanner settings, complete app build, unit tests, and debug APK upload. Android CI run #35 verified the density-independent adaptive launcher foreground, System/Light/Dark/OLED themes, Location-off dialog flow, approximate-distance helper/tests, revised WPS/open-network indicators, channel low-occupancy hint, complete app build, unit tests, and debug APK upload. Android CI run #44 verified passive WPS information-element parsing, WPS version/AP Setup Locked/manufacturer/model extraction tests, local OUI vendor lookup tests, Android Wi-Fi-standard integration, approved launcher artwork resources, complete app build, unit tests, and debug APK upload.

## UI / UX

- [x] Scanner/device tab split — **Implemented; CI verified, device verification pending**
- [x] Channels tab — **Implemented; CI verified, device verification pending**
- [x] Compact network cards — **Implemented; CI verified, device verification pending**
- [x] Network status indicators for hidden SSID, security/open network, Wi-Fi band and advertised WPS — **Implemented; CI verified, device verification pending**
- [x] Green visual state for open networks — **Implemented; CI verified, device verification pending**
- [x] WPS state badge using advertised/locked metadata when available — **Implemented; CI verified, device verification pending**
- [x] Router vendor badge/monogram with generic fallback — **Implemented; CI verified, device verification pending**
- [x] Settings screen with persisted scanner filters/sort — **Implemented; CI verified, device verification pending**
- [x] Persisted System/Light/Dark/OLED appearance modes — **Implemented; CI verified, device verification pending**
- [x] Material 3 Expressive theme — **Implemented; CI verified, device verification pending**
- [x] System Location-switch detection — **Implemented; CI verified, device verification pending**
- [x] Location-off dialog with direct Settings action + return-to-scan flow — **Implemented; CI verified, device verification pending**
- [x] User-approved launcher artwork wired into adaptive/round icons — **Implemented; CI verified, launcher visual verification pending**
- [x] Android 13+ monochrome themed icon — **Implemented; CI verified, launcher verification pending**

## Scanner

- [x] Runtime permission flow — **Implemented; CI verified, device verification pending**
- [x] Android Wi-Fi scan adapter — **Implemented; CI verified, device verification pending**
- [x] SSID/BSSID/RSSI/frequency normalization — **Implemented; CI verified, device verification pending**
- [x] Channel calculation — **Implemented AND verified**
- [x] Wi-Fi band classification (2.4/5/6/60 GHz) — **Implemented AND verified**
- [x] Android Wi-Fi-standard classification (legacy/4/5/6/60 GHz/7 where API exposes it) — **Implemented; CI verified, device verification pending**
- [x] Per-channel detected-AP occupancy aggregation — **Implemented AND verified**
- [x] Lowest-count hint among listed channels — **Implemented; CI verified, device verification pending**
- [x] Approximate RSSI distance estimator + tests — **Implemented AND verified** (displayed with `≈`; not a physical ranging measurement)
- [x] Security capability parsing — **Implemented AND verified**
- [x] AP details screen — **Implemented; CI verified, device verification pending**
- [x] WPS capability discovery from Android scan capability strings — **Implemented; CI verified, device verification pending**
- [x] Passive WPS vendor-specific information-element parser — **Implemented AND verified**
- [x] Passive WPS version / Version2 extraction — **Implemented AND verified**
- [x] Passive WPS AP Setup Locked extraction — **Implemented AND verified** (only when the AP/device exposes the attribute; otherwise `Unknown`)
- [x] Passive WPS manufacturer/model/model-number/device-name extraction — **Implemented AND verified**
- [x] Local MAC/OUI vendor resolver fallback — **Implemented AND verified** (compact starter table; larger generated registry pending)
- [ ] Full generated/updateable OUI vendor registry — `Not implemented`

## WPS PIN engine

- [x] Algorithm registry — **Implemented AND verified**
- [x] Input normalization — **Implemented AND verified**
- [x] WPS checksum helper — **Implemented AND verified**
- [x] Details-screen integration — **Implemented; CI verified, device verification pending**
- [ ] Belkin-family algorithms + vectors — `Not implemented` (real-router credential derivation is not bundled into core)
- [ ] Arcadyan-family algorithms + vectors — `Not implemented` (real-router credential derivation is not bundled into core)
- [ ] Additional documented algorithms + vectors — `Not implemented`

## Device capabilities

- [x] Root detection adapter — **Implemented; CI verified, device verification pending**
- [x] Explicit root verification flow — **Implemented; CI verified, device verification pending**
- [x] Wi-Fi interface discovery — **Implemented; CI verified, device verification pending**
- [x] Device capability UI — **Implemented; CI verified, device verification pending**
- [x] Read-only monitor-mode capability probe — **Implemented; CI verified, device verification pending**
- [x] Packet-injection readiness assessment — **Implemented; CI verified, device verification pending** (read-only; no frames are transmitted)
- [x] Native executable lifecycle + structured execution traces — **Implemented; CI verified, device verification pending**
- [ ] Active packet-injection verification — `Not implemented` (requires explicit lab-only transmission test)

## Pixie Dust

- [ ] Upstream implementation/revision selected — `Not implemented`
- [ ] Licensing reviewed/documented — `Not implemented`
- [ ] Android native build strategy — `Not implemented`
- [ ] Capability detection — `Not implemented`
- [ ] Structured backend interface implementation — `Not implemented`
- [ ] Session/log UI — `Not implemented`
- [ ] Lab verification — `Not implemented`

## Reaver

- [ ] Upstream fork/revision selected — `Not implemented`
- [ ] Licensing reviewed/documented — `Not implemented`
- [ ] Android native build strategy — `Not implemented`
- [ ] Root/backend process adapter — `Not implemented`
- [ ] Monitor-mode capability detection — `Not implemented`
- [ ] Injection capability detection — `Not implemented`
- [ ] Structured output parser — `Not implemented`
- [ ] Session persistence — `Not implemented`
- [ ] Lab verification — `Not implemented`
