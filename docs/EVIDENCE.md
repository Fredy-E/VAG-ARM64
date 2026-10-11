# Evidence and known limitations

## Evidence categories

| Evidence | What it establishes | What it does not establish |
| --- | --- | --- |
| Android debug build | Kotlin compilation, resource packaging and creation of an APK | Android UI correctness, USB-OTG operation or ECU communication |
| JVM unit tests | Framing/checksum properties, token observation and FTDI request construction | Full live stream reassembly, cable authenticity or vehicle safety |
| Local Windows bench/car capture analyses | Observations informing legacy USB framing research | Android-to-vehicle success or decoded command meanings |
| Second (post-clear) car capture + segment pairing | Same grammar reproduced end-to-end; scan determinism (25/27 identical segments); fault-content scaling of data responses | Address-to-segment map (except one behavioral inference) and transaction semantics |
| USBPcap ARM64 build CI | Native build and offline validation of that separate project | Production signing, loaded-driver safety or a full hardware matrix |

The earlier private Android snapshot recorded **20 JVM tests, zero failures/errors/skips**. The public staging copy is rebuilt and retested before release; CI reports the actual results for each published commit. There are no raw capture files, VINs, interface serial numbers, vehicle scan logs, or personal workspace paths in this repository. Per-controller pairing analysis lives in [PAIRING.md](PAIRING.md).

## Current limits

- Physical Android USB-host/cable operation has not been verified as part of this publication.
- ECU transaction meanings and diagnostic payload decoding are not established.
- Banner matching is an observation, not cryptographic authentication. The internal result field named `genuine` is a legacy implementation detail and must not be interpreted as proof.
- FTDI read splitting currently removes the first two status bytes only. Per-packet status stripping for multi-packet USB reads and complete streaming reassembly are not implemented. Treat output as experimental raw observations, not a validated diagnostic stream.
- The baud divisor helper is a simplified model; observed register values are recorded without inferring their full hardware meaning.
- Unit fixtures exercise properties and observed message shapes; they are not a full replay of private hardware captures.
- The bench-probe confirmation is user acknowledgement, not hardware enforcement.
- Release APKs are debug/development-signed, not production-signed or approved for vehicle control.

Publish only claims supported by the specific evidence category above. Do not use the existence of a Windows driver, an APK, or a green build as a complete-system compatibility claim.