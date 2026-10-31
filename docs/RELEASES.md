# Froodz VAG Diag — development releases

Published APKs are **experimental, off-vehicle bench prototypes**, not production diagnostic software. Codenames identify version families, not supported protocols or completion of a roadmap milestone. See the [approved naming policy and exact artifact ledger](RELEASE_CODENAMES.md).

## Published Ignition — 0.1.1

[Froodz VAG Diag 0.1.1 Ignition — Experimental Alpha](https://github.com/Fredy-E/VAG-ARM64/releases/tag/v0.1.1), originally published 2026-10-09 UTC, remains a GitHub prerelease. It adds an off-vehicle confirmation before active probing, removes the unsupported authenticity claim and private source comments, and corrects the framing-length comment. The vehicle diagnostic command layer remains unimplemented.

## Published Spark — 0.2.0-preview.1

[Froodz VAG Diag 0.2.0-preview.1 Spark — Experimental UI Preview](https://github.com/Fredy-E/VAG-ARM64/releases/tag/v0.2.0-preview.1), originally published 2026-10-13 UTC, remains a GitHub prerelease. It is a UI preview, not qualified Android USB/ECU operation or a completed diagnostic milestone. The [preview evidence](UI-PREVIEW.md) and exact APK/source hashes remain separate from local Ignition work.

## Local Ignition builds — not uploaded by this update

- `0.1.2-workshop-dev`, versionCode 3: retained historical local Workshop snapshot.
- `0.1.3-can-foundation-dev`, versionCode 4: frozen local receive-only CAN-foundation work; no live CAN diagnostic session.
- `0.1.4-usb-fix-dev`, versionCode 5: current locally handed-off USB/button-fix build. Its 93 JVM tests, 10 native instrumentation cases and 112 WSA UI checks are evidence for that exact local APK, not for the older published source/binaries. Physical phone/cable/vehicle operation remains unqualified.

The higher-numbered public Spark preview and the later local 0.1.x workstream coexist. Do not relabel or renumber binaries to make their history look linear. Existing tags, manifests, filenames, bytes and checksums remain intact; release display names and documentation use the approved branding. No new tag or binary upload is part of this naming-only publication.

## Preserved release contents

- `VagDiag-0.1.1-debug.apk` — development-signed Android package; API 26 minimum, USB host required, no native ARM64 libraries.
- `VagDiag-0.2.0-preview.1-debug.apk` — the separate historical Spark UI-preview package; its source archive and notices remain on its own release page.
- Complete source zip for the release commit.
- `THIRD-PARTY-NOTICES.md` and the retained Apache-2.0 license text.
- SHA-256 checksums for each uploaded asset and the source commit SHA in the release notes.

No driver binary, proprietary VCDS file, loader, firmware, private capture, vehicle log, credential, SDK, or signing key is included. The APK's valid signature proves package integrity under its signing key, not author identity, safety, hardware compatibility, or production readiness.

Release uploads are checked by downloading the assets back and comparing their SHA-256 values with the local build. Physical Android USB/vehicle validation remains explicitly unverified.
