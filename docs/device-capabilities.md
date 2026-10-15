# Device capability layer

The device capability layer keeps root and low-level Wi-Fi assumptions out of the Compose UI.

## Current probes

- passive `su` path visibility check
- explicit root verification using the fixed command `su -c id`
- Wi-Fi-like interface discovery through `NetworkInterface`
- `/sys/class/net/<name>/wireless` used only as an additional classification hint when visible

The root verification command is fixed in code and does not include AP, SSID, BSSID, or other user-controlled strings.

## Important limits

Finding `su` is only evidence that root may be available. It is not a successful root check.

A successful root check does not imply monitor mode or packet injection. Those remain `UNKNOWN` until separate probes exist.

Interface discovery also does not change interface mode or perform packet capture. It only reports interfaces visible to the app.

## Verification policy

- pure output parsing and interface classification have unit tests
- Android process/interface behavior must also be checked on physical devices
- monitor-mode and injection status must never be inferred from root alone
