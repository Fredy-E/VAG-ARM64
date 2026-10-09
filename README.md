# VAG ARM64

**Windows-on-ARM automotive interoperability research and an independent Android USB cable prototype.**

This project connects three distinct components: VCDS compatibility documentation, native ARM64 USB capture tooling, and **VagDiag**, an experimental Android USB-host application. It is not VCDS for Android, a native ARM64 build of VCDS, or a replacement Ross-Tech interface driver.

## Components

| Component | Location | Actual scope |
| --- | --- | --- |
| Windows compatibility | [VCDS-ARM64](https://github.com/Fredy-E/VCDS-ARM64) | Original setup, troubleshooting and single-machine evidence notes for licensed VCDS on Windows 11 ARM64. |
| Native ARM64 USB capture | [USBPcap-ARM64](https://github.com/Fredy-E/USBPcap-ARM64) | Windows USB capture filter driver and CLI. GPL-2.0 driver / BSD-2-Clause CLI; build-time checks are not production signing or hardware qualification. |
| Android USB prototype | [android/VagDiag](android/VagDiag/) | Kotlin app using Android USB-host APIs for an observed legacy HEX-USB cable. No Windows driver is loaded on Android. |

## Development download

[**VagDiag v0.1.1 development release — APK, source and checksums**](https://github.com/Fredy-E/VAG-ARM64/releases/tag/v0.1.1)

Experimental, development-signed and off-vehicle bench-only. Read the boundaries and limitations below before installing or probing a cable.

## VagDiag: what is implemented

- Discover the observed USB cable (`VID 0403`, `PID FA24`) and request Android USB permission.
- Claim the cable interface and perform FTDI USB control/bulk transfers.
- Observe cable identification text without sending a bulk probe; a banner match is **not proof of authenticity**.
- Explicitly confirmed **bench-only** initialization probe, bounded read windows, and local session-log saving.
- Unit-tested byte framing, checksum logic, identity observation, and FTDI command construction.

**Not implemented or verified:** ECU diagnostic sessions, DTC decoding, live measurements, auto-scan, fault clearing, coding, adaptation, or firmware programming. No physical Android USB-OTG-to-vehicle validation is claimed. **Clear** clears the app's log, not vehicle faults.

The initialization probe transmits observed bytes whose command meanings are not fully established. It is restricted by a confirmation prompt to **off-vehicle bench use**; the app cannot independently verify that the OBD cable is disconnected. Do not connect this prototype to a vehicle for active probing.

## Build and test

Requirements: JDK 17, Gradle 8.7, Android SDK platform 34 and build-tools 34.0.0. Set `ANDROID_HOME` to your installed SDK; never commit `local.properties` or a signing key.

From `android/VagDiag`:

```sh
gradle --no-daemon :app:assembleDebug :app:testDebugUnitTest
```

The debug APK is generated at `app/build/outputs/apk/debug/app-debug.apk`. It requires Android 8.0/API 26 or later and USB-host/OTG hardware. It contains DEX bytecode rather than native ARM64 libraries, so it is **not an ARM64-only binary**.

Run repository checks from the project root:

```sh
python tools/check_project.py
python -m unittest discover -s tools/tests -v
```

GitHub Actions builds the actual Android app and executes its JVM tests. A successful build or JVM test does not validate Android UI behavior or physical USB hardware.

## Documentation

- [Project boundaries and hardware safety](docs/BOUNDARIES.md)
- [Evidence and known limitations](docs/EVIDENCE.md)
- [Release guidance](docs/RELEASES.md)
- [Third-party notices](THIRD-PARTY-NOTICES.md)

## Licensing

Original VagDiag source and project documentation: **Apache License 2.0** — see [LICENSE](LICENSE) and [NOTICE](NOTICE). Copyright 2026 Fredy-E.

The related repositories retain their own existing licenses; none of their drivers or proprietary Ross-Tech files is bundled here. Runtime dependencies retain their own terms in [THIRD-PARTY-NOTICES.md](THIRD-PARTY-NOTICES.md), with license and attribution texts also embedded in the development APK.

No loader, patched executable, firmware, license key, activation bypass, proprietary installer, private USB capture, vehicle identifier, signing key, or machine-specific workspace manifest is distributed.

Independent community interoperability research by Fredy-E. Not affiliated with or endorsed by Ross-Tech. VCDS is a trademark of Ross-Tech, LLC.
