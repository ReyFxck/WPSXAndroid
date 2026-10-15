# Architecture

WPSXAndroid is split so Android UI code, passive Wi-Fi discovery, offline WPS PIN calculations, and active audit engines do not become one tightly coupled block.

## Layers

```text
app / Compose UI
        |
        v
application use-cases
        |
        +-------------------+------------------+
        |                   |                  |
        v                   v                  v
Wi-Fi scanner          PIN engine        Audit backend API
(Android APIs)       (pure Kotlin)        (capability gated)
                                               |
                                +--------------+--------------+
                                |                             |
                                v                             v
                         Pixie backend                  Reaver backend
                         native/root                    native/root
```

## Hard rules

- UI must not execute shell commands directly.
- PIN algorithms must remain pure/deterministic wherever possible.
- Active audit engines live behind `AuditBackend`.
- Root access is a capability, not an assumption.
- Monitor mode and packet injection are separately detected capabilities.
- Unsupported hardware must fail closed with an explanation rather than pretending a test ran.
- Every algorithm requires test vectors before status can become **Implemented AND verified**.
- Native third-party components must have their license and exact upstream revision documented.

## Planned modules

The bootstrap begins as one Android module to keep iteration fast. Split into Gradle modules only when boundaries are exercised by real code.

Potential later split:

```text
:app
:core:model
:core:wifi
:core:wps
:core:audit
:backend:pixie
:backend:reaver
:platform:root
```

## Data flow

1. Scanner discovers AP metadata exposed by Android or a supported low-level backend.
2. Normalized AP metadata is stored as core models.
3. Offline algorithms receive only the fields they require.
4. Active audit engines receive an explicit target and capability snapshot.
5. Engine output is converted to structured events for the UI and logs.

## Security boundary

Never pass arbitrary user-provided strings directly into a root shell command. Native/root adapters must use structured arguments, validation, and explicit executable paths.
