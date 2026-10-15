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
- [x] Add AP details screen
- [ ] Verify scanner/details flow on physical devices
- [ ] Research reliable WPS visibility on modern Android beyond capability-string heuristics

## Phase 2 — Offline WPS PIN engine

- [x] Build algorithm registry
- [x] Add strict BSSID/MAC parsing
- [x] Add WPS checksum helper
- [x] Integrate engine status into AP details UI
- [ ] Implement eligible documented lab-safe algorithm modules one at a time
- [ ] Add upstream/reference attribution for every algorithm
- [ ] Add positive and negative test vectors before enabling any algorithm in UI

## Phase 3 — Device capability layer

- [ ] Root detection adapter
- [ ] Wi-Fi interface discovery
- [ ] Monitor-mode detection
- [ ] Packet-injection capability detection
- [ ] Native executable lifecycle and structured logging

## Phase 4 — Pixie Dust backend

- [ ] Select audited upstream revision
- [ ] Document third-party license
- [ ] Reproducible Android build
- [ ] Structured backend adapter
- [ ] Controlled-lab test matrix

## Phase 5 — Reaver backend

- [ ] Select audited upstream revision
- [ ] Document third-party license/dependencies
- [ ] Reproducible Android build
- [ ] Structured backend adapter
- [ ] Session/log integration
- [ ] Controlled-lab test matrix

## Phase 6 — UX and release engineering

- [ ] Persistent scan history (opt-in)
- [ ] Exportable audit report
- [ ] Crash-safe backend sessions
- [ ] GitHub Actions release APK artifacts
- [ ] Versioned changelog
- [ ] Signed release process
