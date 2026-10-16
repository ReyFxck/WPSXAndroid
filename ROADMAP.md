# Roadmap

## Phase 0 — Bootstrap

- [x] Initialize repository
- [x] Add Android/Compose app skeleton
- [x] Define architectural boundaries
- [x] Add status/checklist discipline
- [x] Get CI green

## Phase 1 — Passive scanner

- [x] Permission UX for supported Android versions
- [x] Discover nearby Wi-Fi networks using Android APIs
- [x] Normalize SSID/BSSID/RSSI/frequency/security metadata
- [x] Add channel/frequency helpers
- [x] Add Wi-Fi band classification (2.4/5/6/60 GHz)
- [x] Add compact network cards and status indicators
- [x] Add RSSI-aware Wi-Fi signal indicators with green open-network state
- [x] Add AP details screen
- [x] Add Channels tab with detected-AP occupancy per channel
- [x] Add persistent scanner filters/sort settings
- [ ] Verify scanner/details/channels flow on physical devices
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
- [x] Capability status UI
- [x] Read-only monitor-mode support probe
- [x] Read-only packet-injection readiness assessment
- [x] Native executable lifecycle with bounded structured execution traces
- [ ] Verify root/interface/monitor probes on physical devices
- [ ] Add an explicit controlled-lab active injection verification method

## Phase 4 — Pixie Dust backend

- [x] Select and pin candidate upstream revision (`kimocoder/pixiewps`)
- [x] Document Pixiewps third-party license/provenance
- [x] Add read-only Pixie readiness evaluator + tests
- [x] Add AP-details readiness/session-check UI
- [ ] Reproducible Android/NDK build of the pinned engine
- [ ] Structured backend adapter
- [ ] Persistent/crash-safe backend session logs
- [ ] Controlled-lab test matrix

## Phase 5 — Reaver backend

- [x] Select and pin candidate upstream revision (`t6x/reaver-wps-fork-t6x`)
- [ ] Complete per-file third-party redistribution/license review
- [ ] Reproducible Android build
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
