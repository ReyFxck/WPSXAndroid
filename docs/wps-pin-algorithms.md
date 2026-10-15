# WPS PIN algorithms

WPSXAndroid will support documented offline PIN derivation algorithms only after each implementation has reproducible provenance and tests.

## Implementation checklist per algorithm

- [ ] Assign stable internal ID
- [ ] Record algorithm family/vendor context
- [ ] Record public reference/provenance
- [ ] Document required inputs (BSSID, SSID, serial-derived data, etc.)
- [ ] Reject malformed/insufficient input
- [ ] Implement as pure Kotlin when practical
- [ ] Add known-positive test vectors
- [ ] Add negative/edge-case tests
- [ ] Verify leading-zero handling
- [ ] Verify checksum behavior where applicable
- [ ] Mark verified in `STATUS.md` only after tests pass

## Planned families

- [ ] Belkin-family
- [ ] Arcadyan-family
- [ ] Additional public algorithms selected after provenance review

Do not merge copied algorithm code until its license compatibility is understood.
