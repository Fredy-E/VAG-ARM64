# VagDiag UI preview 0.2.0-preview.1

**Experimental, development-signed/debuggable Android bench prototype.**
The UI refresh reorganizes the existing connection, identification, bench-probe
and local-log controls. It does not implement a vehicle diagnostic layer.

## Package and evidence

- Package `com.vagdiag`; versionName `0.2.0-preview.1`, versionCode `3`.
- Android API 26 minimum; USB-host/OTG hardware required for cable operations.
- DEX-only package: no native ARM64 libraries, so not an ARM64-only Android app.
- Debug APK: 844663 bytes, SHA-256
  `96befaede74fbd8001a11df788609126f0569a75b22d7ec181f95054c1d7ad07`.
- Actual APK assembly/signature/package inspection, 20 existing JVM framing
  tests, and 47 repository/source tests passed in the local UI-release work.
  Test/build evidence is not a physical Android UI, USB-OTG or vehicle test.
- The complete source download retains the newer public `docs/PAIRING.md`
  research update. It changes no Android protocol/runtime behavior or APK bytes.
- Apache-2.0 license, project NOTICE, third-party notices and license texts
  remain retained; no signing key, SDK, driver, proprietary VCDS file, private
  capture or vehicle identifier is included.

## Safety and limitations

Passive identification observations do not establish cable authenticity.
Active initialization requires explicit confirmation for off-vehicle bench use;
the application cannot determine whether a cable is physically disconnected from
an OBD port. Do not actively probe with this prototype connected to a vehicle.
Clear erases the app log, not vehicle faults. No ECU sessions, DTC decoding,
measurements, fault clearing, coding, adaptation or programming is implemented.

The prior v0.1.1 development release and its dated evidence remain unchanged.
Build commands and boundaries are in the root README and `docs/BOUNDARIES.md`.
This prerelease does not certify production readiness or hardware compatibility.
