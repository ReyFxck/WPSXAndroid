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
- [x] Active audit backend contract — **Implemented; no active on-air backend yet**
- [x] CI build/test pipeline — **Implemented AND verified**
- [x] Debug APK CI artifact — **Implemented AND verified**

Current verification baseline: Android CI run #77 built `:app:assembleDebug`, ran `:app:testDebugUnitTest`, and uploaded the debug APK after the compact AP-details refresh, Android coarse+fine location request fix, Wi-Fi-off UX, connected-target authorization gate, rooted interface fallback, rooted read-only `iw phy` fallback, and their new unit tests. Earlier verified milestones remain represented by the completed checklist items below.

## UI / UX

- [x] Scanner/device tab split — **Implemented; CI verified, device verification pending**
- [x] Channels tab — **Implemented; CI verified, device verification pending**
- [x] Compact network cards — **Implemented; CI verified, device verification pending**
- [x] RSSI-aware Wi-Fi signal indicator — **Implemented; CI verified, device verification pending**
- [x] Green visual state for open networks — **Implemented; CI verified, device verification pending**
- [x] WPS state/version badge — **Implemented; CI verified, device verification pending**
- [x] Router vendor text from WPS metadata/OUI fallback — **Implemented; CI verified, device verification pending**
- [x] Compact AP-details layout with expandable WPS/technical details — **Implemented; CI verified, device verification pending**
- [x] Wi-Fi-disabled dialog/card with direct Wi-Fi settings action — **Implemented; CI verified, device verification pending**
- [x] Android coarse+fine permission request for Exact/Approximate location selector — **Implemented; CI verified, device verification pending**
- [x] Precise-location recovery link to app settings — **Implemented; CI verified, device verification pending**
- [x] Settings screen with persisted filters/sort/theme — **Implemented; CI verified, device verification pending**
- [x] Material 3 Expressive theme — **Implemented; CI verified, device verification pending**
- [x] System Location-switch detection/dialog — **Implemented; CI verified, device verification pending**
- [x] User-approved launcher artwork + adaptive safe-zone inset — **Implemented; CI verified; user device visually verified**
- [x] Android 13+ monochrome themed icon — **Implemented; CI verified, launcher verification pending**
- [ ] Curated manufacturer-logo pack with trademark/source review — `Not implemented`

## Scanner

- [x] Runtime permission flow — **Implemented; CI verified, device verification pending**
- [x] Android Wi-Fi scan adapter — **Implemented; CI verified, device verification pending**
- [x] Explicit Wi-Fi-disabled scan failure instead of silent/empty behavior — **Implemented; CI verified, device verification pending**
- [x] SSID/BSSID/RSSI/frequency normalization — **Implemented; CI verified, device verification pending**
- [x] Channel calculation — **Implemented AND verified**
- [x] Wi-Fi band classification (2.4/5/6/60 GHz) — **Implemented AND verified**
- [x] Android Wi-Fi-standard classification — **Implemented; CI verified, device verification pending**
- [x] Per-channel AP occupancy aggregation — **Implemented AND verified**
- [x] Approximate RSSI distance estimator — **Implemented AND verified**
- [x] Security capability parsing — **Implemented AND verified**
- [x] Passive WPS vendor-specific IE parser — **Implemented AND verified**
- [x] Passive WPS version / Version2 extraction — **Implemented AND verified**
- [x] Passive WPS AP Setup Locked extraction — **Implemented AND verified**
- [x] Passive WPS Setup State extraction — **Implemented AND verified**
- [x] Passive manufacturer/model/model-number/device-name/serial/config-method extraction — **Implemented AND verified**
- [x] Local MAC/OUI vendor fallback — **Implemented AND verified** (compact starter table)
- [ ] Full generated/updateable OUI vendor registry — `Not implemented`

## WPS PIN engine

- [x] Algorithm registry — **Implemented AND verified**
- [x] Input normalization — **Implemented AND verified**
- [x] WPS checksum helper — **Implemented AND verified**
- [x] Details-screen integration — **Implemented; CI verified, device verification pending**
- [ ] Belkin-family algorithms + vectors — `Not implemented`
- [ ] Arcadyan-family algorithms + vectors — `Not implemented`
- [ ] Additional documented algorithms + vectors — `Not implemented`

## Device capabilities

- [x] Root detection adapter — **Implemented; CI verified; root verified on user device**
- [x] Explicit root verification flow — **Implemented; CI verified; user device verified**
- [x] Wi-Fi interface discovery through Java/sysfs — **Implemented; CI verified, device verification pending**
- [x] Rooted `/sys/class/net` interface fallback — **Implemented AND CI verified; device verification pending**
- [x] Device capability UI — **Implemented; CI verified, device verification pending**
- [x] Read-only monitor-mode `iw phy` probe — **Implemented; CI verified, device verification pending**
- [x] Read-only `iw dev` monitor-interface probe + backend classifier — **Implemented; CI verification pending, device verification pending**
- [x] Rooted read-only `iw phy` fallback — **Implemented; CI verified, device verification pending**
- [x] Packet-injection readiness assessment — **Implemented; CI verified, device verification pending** (read-only; no frames transmitted)
- [x] Native executable lifecycle + structured execution traces — **Implemented; CI verified, device verification pending**
- [ ] Platform-specific same-interface monitor evidence adapters — `[IN PROGRESS]`
- [ ] Active packet-injection verification — `Not implemented`

## Authorized test mode

- [x] Selected AP must match the device's currently connected BSSID — **Implemented AND verified**
- [x] Selected AP must advertise WPS — **Implemented AND verified**
- [x] Explicit ownership/authorization confirmation — **Implemented; CI verified, device verification pending**
- [x] One-tap root/radio/Pixie readiness preflight — **Implemented; CI verified, device verification pending**
- [ ] On-air WPS/Pixie execution — `Not implemented` (native Reaver/Pixiewps transport is not bundled yet)

## Pixie Dust

- [x] Upstream implementation/revision selected — **Implemented AND verified** (`kimocoder/pixiewps` pinned to `8e8062eda97b0309e3f6baaa90eb6fa33423c37c`)
- [x] Pixiewps licensing/provenance documented — **Implemented AND verified** (GPL-3.0 reference; see `docs/PIXIE_BACKEND_SELECTION.md`)
- [ ] Reproducible Android/NDK build of pinned Pixiewps — `[IN PROGRESS]` (upstream build is C99 + pthread with bundled crypto; Android adaptation still required)
- [x] Read-only capability/readiness evaluator — **Implemented AND verified**
- [ ] Structured active backend implementation — `Not implemented`
- [x] Compact AP-details readiness UI — **Implemented; CI verified, device verification pending**
- [ ] Persistent/crash-safe backend session logs — `Not implemented`
- [ ] Controlled-lab active verification — `Not implemented`

## Reaver

- [x] Upstream fork/revision selected — **Implemented AND verified** (`t6x/reaver-wps-fork-t6x` pinned to `f9b5e150f913a23d34c8098d598b90aaa08261cc`)
- [ ] Licensing reviewed/documented — `[IN PROGRESS]`
- [ ] Reproducible Android native build — `[IN PROGRESS]` (larger port: WPS/crypto/TLS/wireless-extension layers and Android radio integration)
- [ ] Root/backend process adapter — `Not implemented`
- [x] Read-only monitor-mode layer reusable by backend — **Implemented AND verified**
- [x] Packet-injection readiness layer reusable by backend — **Implemented AND verified**
- [ ] Structured output parser — `Not implemented`
- [ ] Session persistence — `Not implemented`
- [ ] Controlled-lab active verification — `Not implemented`
