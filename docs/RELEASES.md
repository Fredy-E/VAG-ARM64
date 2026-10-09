# Development releases

VagDiag releases are **experimental, off-vehicle bench prototypes**, not production diagnostic software.

Version **0.1.1** adds an off-vehicle confirmation before active probing, removes the UI's unsupported authenticity claim, removes private evidence paths/interface serial comments from source, and corrects the framing comment's payload length. The underlying vehicle diagnostic command layer remains unimplemented.

## Release contents

- `VagDiag-0.1.1-debug.apk` — development-signed Android package; API 26 minimum, USB host required, no native ARM64 libraries.
- Complete source zip for the release commit.
- `THIRD-PARTY-NOTICES.md` and the retained Apache-2.0 license text.
- SHA-256 checksums for each uploaded asset and the source commit SHA in the release notes.

No driver binary, proprietary VCDS file, loader, firmware, private capture, vehicle log, credential, SDK, or signing key is included. The APK's valid signature proves package integrity under its signing key, not author identity, safety, hardware compatibility, or production readiness.

Release uploads are checked by downloading the assets back and comparing their SHA-256 values with the local build. Physical Android USB/vehicle validation remains explicitly unverified.
