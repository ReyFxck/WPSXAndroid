# Roadmap

## Phase 0 — Bootstrap

- [x] Initialize repository
- [x] Add Android/Compose app skeleton
- [x] Define architectural boundaries
- [x] Add status/checklist discipline
- [ ] Get CI green

## Phase 1 — Passive scanner

- [ ] Permission UX for supported Android versions
- [ ] Discover nearby Wi-Fi networks using Android APIs
- [ ] Normalize SSID/BSSID/RSSI/frequency/security metadata
- [ ] Add channel/frequency helpers
- [ ] Add AP details screen
- [ ] Research reliable WPS visibility on modern Android

## Phase 2 — Offline WPS PIN engine

- [ ] Build algorithm registry
- [ ] Add strict BSSID/MAC parsing
- [ ] Add WPS checksum helper
- [ ] Implement one algorithm at a time
- [ ] Add upstream/reference attribution for every algorithm
- [ ] Add positive and negative test vectors before enabling in UI

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
