# Pixie / Reaver backend selection

This document records the third-party references selected for the future controlled-lab WPS backend. Selection does **not** mean the binaries are currently bundled or executed by WPSXAndroid.

## Selected Pixiewps reference

- Repository: `kimocoder/pixiewps`
- Pinned revision: `8e8062eda97b0309e3f6baaa90eb6fa33423c37c`
- License observed in repository: GPL-3.0
- Role in WPSXAndroid: future offline Pixie Dust calculation engine after reproducible Android builds and license-compliant packaging are completed.

Why this fork was selected for evaluation:

- It is a real fork of the established Wiire codebase rather than a wrapper project.
- It contains additional work beyond the old 1.4.2 release, including the pixiewrapper/time-range work and later upstream merges.
- It is maintained by a contributor active in the Reaver/WPS ecosystem.
- Its delta from upstream is relatively small enough to audit before vendoring.

## Selected Reaver reference

- Repository: `t6x/reaver-wps-fork-t6x`
- Pinned revision: `f9b5e150f913a23d34c8098d598b90aaa08261cc`
- Role in WPSXAndroid: future WPS transport/session backend and structured source for `wash`-style WPS metadata.
- License note: the project is GPLv2-era code and also contains imported components with their own GPL/BSD notices. A complete redistribution review is still required before any source/binary is bundled in the APK.

Reaver t6x is useful because it already handles WPS protocol interaction, lock handling, sessions, channel control, Pixie parameter collection, PCAP output, and extended `wash` metadata. Its Pixie mode still delegates the offline calculation to Pixiewps.

## Rejected / deferred candidates

### `noerror-web/pixiewps-extended`

Deferred for now. It is active and contains substantial new code, but its documentation makes very large claims about success rates, static-PKE exploitation, hybrid attacks, and router coverage. The current default branch is also a Copilot-named repair branch. WPSXAndroid will not depend on those claims without independent source review and reproducible test vectors.

### OneShot / OneShot-Extended

Not selected as the primary backend because WPSXAndroid already has its own scanner/UI/capability layer and the project goal is to integrate the underlying audited components rather than another orchestration frontend.

## Current app behavior

The AP details screen may run a **read-only readiness check**. It combines:

- WPS advertised state
- WPS lock metadata when available
- root verification
- visible Wi-Fi interfaces
- monitor-mode capability status
- packet-injection readiness status
- pinned backend provenance

The readiness check does not switch interface mode, transmit packets, invoke Reaver/Pixiewps, or attempt credential/PIN recovery.

## Before bundling third-party code

- [x] Select candidate Pixiewps fork/revision
- [x] Select candidate Reaver fork/revision
- [x] Record Pixiewps license
- [ ] Complete per-file Reaver redistribution/license review
- [ ] Produce reproducible Android/NDK builds
- [ ] Record exact compiler/NDK flags and binary hashes
- [ ] Add structured output parsers
- [ ] Add crash-safe session lifecycle
- [ ] Verify only in a controlled lab on owned hardware
