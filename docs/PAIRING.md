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
- No raw captures, scan logs, or identifiers are distributed with this repository.
