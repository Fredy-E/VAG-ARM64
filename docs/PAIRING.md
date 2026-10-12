# Per-controller transaction pairing

Private car captures (two sessions: pre- and post-fault-clear, same vehicle, same
read-only scan flow) were split at the 27 FTDI port-open markers. Within each segment,
host request transactions and cable reply frames were counted by length class.

## Segment table (first session; second session identical except seg3, seg26)

| seg | dur (s) | host 20B | reply 44B | reply 20B | reply 4B | role |
|-----|---------|----------|-----------|-----------|----------|------|
| 0 | 20.4 | 0 | 16 | 4 | 20 | Interface test: identity banner, no controller traffic |
| 1 | 5.1 | 29 | 16 | 92 | 44 | scan #1 |
| 2 | 5.9 | 30 | 16 | 64 | 46 | scan #2 |
| 3 | 5.8 | 42→33* | 16 | 108→73* | 58 | scan (sole pre/post-clear delta — see below) |
| 4–16 | ~5–8 | 25–47 | 16 | 43–81 | 41–63 | scan (deterministic across sessions) |
| 17 | 6.8 | 83 | 16 | 144 | 99 | heavyweight outlier, identical both sessions (unattributed) |
| 18–19 | ~5–6 | 27–30 | 16 | 48–76 | 43–46 | scan |
| 20 | 5.5 | 27 | 16 | 18 | 59 | lightest responses (hypothesis: unreachable controller) |
| 21 | 5.3 | 27 | 16 | 49 | 42 | scan (last of 21) |
| 22–23 | 5–27 | 27 | 16 | 49–58 | 43 | post-scan module reads + idle |
| 24–26 | 5–9 | 11–28 | 16 | 44–52 | 28–41 | short module reads |

Frames check out at 100% XOR in both directions in both sessions
(host 1885–1886 frames, cable 3425–3454 frames), with zero foreign-device
records and zero truncation.

## What the pre/post-clear pair proves

- The scan flow is deterministic: 25 of 27 segments are identical in transaction
  counts across sessions run on different days in different fault states.
- Every controller open yields exactly 16 long (44-byte) identification blocks plus
  a variable number of 20-byte data responses that scale with fault content:
  the cleared controller shows 35 fewer data responses for 4 cleared faults.
- Long-block payloads are unique per segment (zero overlap), consistent with
  per-controller identity content.
- Request-block inner bytes are high-entropy with per-segment-distinct openers:
  controller addressing is not USB-plaintext, so no address mapping is claimed
  from byte values. The engine attribution below is behavioral (sole-delta
  inference), not a byte decode.

## Open (needs vehicle-layer decode, not more USB)

- Exact address-to-segment map, except the cleared controller (strong inference).
- Heavyweight/lightweight segment attributions are hypotheses.
- Per-transaction request/response semantics inside the 20/44-byte payloads.
- A byte-equality diff of the cleared controller's segment across the two sessions
  does NOT isolate fault frames: request sets are fully disjoint and even the
  identification blocks differ run-to-run, while short acks stay stable. The
  vehicle layer therefore uses session-dependent encoding, which also means fixed
  USB replays across sessions will not work — live sessions are required.
- Positional analysis shows full-payload encoding (17/20 bytes differ per paired
  frame — not a counter/timestamp), with two surviving clues: one stable
  message-class byte position in requests, and above-random structure in one
  reply region, which is where crib analysis against known scan-file ASCII starts.
- A cross-session frame census draws the control/data boundary exactly: all short
  frames (host lengths 4–9, cable lengths 4–7) plus one 20-byte identity frame
  are byte-identical across sessions (plaintext control plane, including a
  type-plus-ASCII-name identity frame repeated once per port-open); all long
  transaction payloads are session-unique (encoded data plane).
- Follow-up crib battery (single-byte key sweep, known-byte drag, wrapper-library
  triage): trivial ciphers ruled out, no standard crypto APIs in the wrapper, and
  naive cribs are confounded by structured cable headers. Next: crib open K-line
  command bytes against transaction payloads, or use the cable's documented
  passthrough mode for K-line modules.
- The bundled USB transport library is the FTDI vendor build (exports mirror the
  vendor API exactly): framing and session encoding live in the packed
  application binary, which imports the transport through a single entry point.
  Static analysis must start from an unpacked image, not the on-disk file.
- Unpacked-image analysis confirms variable-block Rijndael session encoding
  (round count selected per message size class) behind a virtual-dispatched
  API; key derivation is still open.
- No raw captures, scan logs, or identifiers are distributed with this repository.
