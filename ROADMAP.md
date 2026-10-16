# Roadmap

## Phase 0 — Bootstrap

- [x] Initialize repository
- [x] Add Android/Compose app skeleton
- [x] Define architectural boundaries
- [x] Add status/checklist discipline
- [x] Get CI green

## Phase 1 — Passive scanner

- [x] Permission UX for supported Android versions
- [x] Request coarse + fine location together for Android's Exact/Approximate choice
- [x] Detect Wi-Fi-off state and provide direct Wi-Fi settings flow
- [x] Discover nearby Wi-Fi networks using Android APIs
- [x] Normalize SSID/BSSID/RSSI/frequency/security metadata
- [x] Add channel/frequency helpers
- [x] Add Wi-Fi band classification (2.4/5/6/60 GHz)
- [x] Add compact network cards and status indicators
- [x] Add RSSI-aware Wi-Fi signal indicators with green open-network state
- [x] Add compact AP-details screen with expandable technical/WPS information
- [x] Add Channels tab with detected-AP occupancy per channel
- [x] Add persistent scanner filters/sort settings
- [ ] Verify revised scanner/details/channels flow on physical devices
- [ ] Research reliable WPS visibility on modern Android beyond capability-string heuristics
- [ ] Research additional reliable read-only WPS registrar state sources

## Phase 2 — Offline WPS PIN engine

- [x] Build algorithm registry
- [x] Add strict BSSID/MAC parsing
- [x] Add WPS checksum helper
- [x] Integrate engine status into AP details UI
- [ ] Implement eligible documented lab-safe algorithm modules one at a time
- [ ] Add upstream/reference attribution for every algorithm
- [ ] Add positive and negative test vectors before enabling any algorithm in UI

## Phase 3 — Device capability layer

- [x] Root detection adapter
- [x] Explicit user-triggered root verification
- [x] Wi-Fi interface discovery
- [x] Add rooted `/sys/class/net` interface fallback
- [x] Capability status UI
- [x] Read-only monitor-mode support probe
- [x] Add rooted read-only `iw phy` fallback
- [x] Read-only packet-injection readiness assessment
- [x] Native executable lifecycle with bounded structured execution traces
- [ ] Verify rooted interface/monitor probes on physical devices
- [ ] Add an explicit controlled-lab active injection verification method

## Phase 4 — Authorized test mode / Pixie Dust backend

- [x] Restrict active-test entry to the currently connected selected AP
- [x] Require explicit ownership/authorization confirmation
- [x] Select and pin candidate upstream revision (`kimocoder/pixiewps`)
- [x] Document Pixiewps third-party license/provenance
- [x] Add read-only Pixie readiness evaluator + tests
- [x] Add compact AP-details test/preflight UI
- [ ] Reproducible Android/NDK build of the pinned Pixiewps engine
- [ ] Structured Pixie backend adapter
- [ ] Persistent/crash-safe backend session logs
- [ ] Controlled-lab active Pixie verification

## Phase 5 — Reaver transport/backend

- [x] Select and pin candidate upstream revision (`t6x/reaver-wps-fork-t6x`)
- [ ] Complete per-file third-party redistribution/license review
- [ ] Reproducible Android native build
- [ ] Integrate controlled on-air WPS transport behind the connected-target authorization gate
- [ ] Structured backend adapter
- [ ] Structured Reaver/wash output parser
- [ ] Session/log integration
- [ ] Controlled-lab test matrix

## Phase 6 — UX and release engineering

- [x] Material 3 Expressive theme
- [x] Adaptive launcher icon + monochrome themed icon
- [x] Top-level settings screen
- [ ] Curated manufacturer logo pack with source/trademark review
- [ ] Persistent scan history (opt-in)
- [ ] Exportable audit report
- [ ] Crash-safe backend sessions
- [ ] GitHub Actions release APK artifacts
- [ ] Versioned changelog
- [ ] Signed release process
