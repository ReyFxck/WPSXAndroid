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
- [x] Core scanner contract — **Implemented; no platform scanner yet**
- [x] WPS PIN algorithm contract — **Implemented; no algorithms yet**
- [x] Active audit backend contract — **Implemented; no active backend yet**
- [x] CI build passes on GitHub Actions — **Implemented AND verified**
- [x] Debug APK uploaded as CI artifact — **Implemented AND verified**

Verification baseline: Android CI run #4 successfully installed the Android 17 SDK, built `:app:assembleDebug`, ran `:app:testDebugUnitTest`, and uploaded the debug APK artifact.

## Scanner

- [ ] Runtime permission flow — `Not implemented`
- [ ] Android Wi-Fi scan adapter — `Not implemented`
- [ ] SSID/BSSID/RSSI/frequency normalization — `Not implemented`
- [ ] Channel calculation — `Not implemented`
- [ ] Security capability parsing — `Not implemented`
- [ ] WPS capability discovery — `Not implemented`

## WPS PIN engine

- [ ] Algorithm registry — `Not implemented`
- [ ] Input normalization — `Not implemented`
- [ ] WPS checksum helper — `Not implemented`
- [ ] Belkin-family algorithms + vectors — `Not implemented`
- [ ] Arcadyan-family algorithms + vectors — `Not implemented`
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
