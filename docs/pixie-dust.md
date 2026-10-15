# Pixie Dust backend

Pixie Dust support is planned as an **active authorized-audit backend**, not UI logic.

The Android app should interact with it through structured request/result objects and capability checks.

## Integration checklist

- [ ] Select exact upstream implementation and revision
- [ ] Review/document license
- [ ] Identify native dependencies
- [ ] Make build reproducible for supported Android ABIs
- [ ] Define capability probe
- [ ] Define structured input model
- [ ] Define structured progress/result events
- [ ] Avoid shell-string concatenation
- [ ] Add cancellation/timeouts
- [ ] Verify only in controlled lab fixtures

No backend is considered available merely because a binary exists on disk.
