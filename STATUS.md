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

Verification baseline: Android CI run #4 successfully installed the Android 17 SDK, built `:app:assembleDebug`, ran `:app:testDebugUnitTest`, and uploaded the debug APK artifact. Android CI run #6 also verified the passive scanner build and unit tests. Android CI run #8 verified the WPS PIN core normalization, checksum, registry tests, app build, and debug APK upload. Android CI run #10 verified the AP details navigation/UI integration, existing tests, and debug APK upload.

## Scanner

- [x] Runtime permission flow — **Implemented; CI verified, device verification pending**
- [x] Android Wi-Fi scan adapter — **Implemented; CI verified, device verification pending**
- [x] SSID/BSSID/RSSI/frequency normalization — **Implemented; CI verified, device verification pending**
- [x] Channel calculation — **Implemented AND verified**
- [x] Security capability parsing — **Implemented AND verified**
- [x] AP details screen — **Implemented; CI verified, device verification pending**
- [x] WPS capability discovery from Android scan capability strings — `[IN PROGRESS]` (heuristic/device verification pending)

## WPS PIN engine

- [x] Algorithm registry — **Implemented AND verified**
- [x] Input normalization — **Implemented AND verified**
- [x] WPS checksum helper — **Implemented AND verified**
- [x] Details-screen integration — **Implemented; CI verified, device verification pending**
- [ ] Belkin-family algorithms + vectors — `Not implemented` (real-router credential derivation is not bundled into core)
- [ ] Arcadyan-family algorithms + vectors — `Not implemented` (real-router credential derivation is not bundled into core)
- [ ] Additional documented algorithms + vectors — `Not implemented`

## Pixie Dust

- [ ] Upstream implementation/revision selected — `Not implemented`
- [ ] Licensing reviewed/documented — `Not implemented`
- [ ] Android native build strategy — `Not implemented`
- [ ] Capability detection — `Not implemented`
- [ ] Structured backend interface implementation — `Not implemented`
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
