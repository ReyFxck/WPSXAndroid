# WPS PIN engine

The WPS PIN engine is intentionally split into small, testable pieces.

## Core pieces

- [x] Canonical BSSID normalization
- [x] Standard WPS checksum formatting/validation
- [x] Algorithm registry and dispatcher
- [x] Registry input normalization
- [x] Synthetic fixture coverage
- [ ] Device/lab integration coverage
- [ ] Additional non-credential metadata heuristics

## Algorithm contract

Algorithms implement `WpsPinAlgorithm`. The registry supplies a normalized
`WpsPinInput`, calls only algorithms whose `supports()` method accepts that
input, verifies that returned candidates identify the algorithm that produced
them, removes duplicate candidates, and returns the final list.

Vendor-specific default credential derivation is not bundled into the core.
The core remains suitable for authorized lab fixtures and independently
verifiable, non-invasive analysis without embedding real-router credential
prediction formulas.
