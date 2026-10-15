# Reaver backend

Reaver integration is planned behind the common active-audit interface.

## Design requirements

- [ ] Pin an exact upstream fork/revision
- [ ] Review/document upstream and dependency licenses
- [ ] Reproducibly build required native components for Android
- [ ] Detect root separately from Wi-Fi capabilities
- [ ] Detect monitor mode separately
- [ ] Detect packet injection separately
- [ ] Use structured arguments rather than unescaped command strings
- [ ] Parse output into structured events
- [ ] Support cancellation and cleanup
- [ ] Persist only non-sensitive session metadata by default
- [ ] Verify against owned lab hardware

The UI must not show an active test as available unless the backend capability probe passes.
