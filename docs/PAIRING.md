# USB transaction segment comparison

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

## What was observed, and what remains inferred

- In these two captures, 25 of 27 segments have identical transaction counts.
  Both sessions were recorded on the same day, around a separately performed
  fault clear. This observation does not prove universal scan determinism.
- The compared segments contain a repeated count of long 44-byte replies and
  differing counts of 20-byte replies. A 35-reply reduction correlates with the
  changed fault state in one segment; it is not a decoded DTC mapping or proof
  that every long reply is a controller-identification block.
- Long-block payloads are unique per segment (zero overlap), consistent with
  per-controller identity content.
- Request-block inner bytes are high-entropy with per-segment-distinct openers:
  controller addressing is not USB-plaintext, so no address mapping is claimed
  from byte values. The engine attribution below is behavioral (sole-delta
  inference), not a byte decode.

## Open: transport/session semantics remain unresolved

- Exact address-to-segment map, except the cleared controller (strong inference).
- Heavyweight/lightweight segment attributions are hypotheses.
- Per-transaction request/response semantics inside the 20/44-byte payloads.
- A byte-equality diff of the cleared controller's segment across the two sessions
  does NOT isolate fault frames: request sets are fully disjoint and even the
  long blocks differ run-to-run, while short acks stay stable. The
  differing bytes make fixed USB replay an unsupported diagnostic strategy.
  Session variability does not identify a particular encoding algorithm, mode,
  key, counter scheme or packet-to-cipher data flow.
- Positional analysis found widespread byte differences, with two clues: one stable
  message-class byte position in requests, and above-random structure in one
  reply region, which is where crib analysis against known scan-file ASCII starts.
- A cross-session frame census separates shared short frames from varying long replies: short
  frames (host lengths 4–9, cable lengths 4–7) plus one 20-byte identity frame
  are byte-identical across sessions (shared short-control/identity fragments, including a
  type-plus-ASCII-name identity frame repeated once per port-open); all long
  transaction payloads differ across the compared sessions. Their semantics
  and precise encoding remain unestablished.
- Follow-up crib battery (single-byte key sweep, known-byte drag, wrapper-library
  triage): tested simple transforms did not yield a validated decode, and naive
  cribs are confounded by structured cable headers. Absence of standard crypto
  imports does not establish a custom/lightweight cipher. Legacy dumb-mode
  K-line passthrough is not raw CAN access and does not establish Eos coverage.
- In the inspected Windows installation, the USB transport library matches the FTDI vendor API (exports mirror the
  vendor API exactly). That import shape alone does not locate the complete
  packet/framing/cipher path or establish which layers the cable handles.
- **Retraction:** the earlier statement that unpacked-image analysis confirmed
  variable-block Rijndael session encoding was unsupported. Rijndael-related
  symbols/code were located, but linkage to captured packets, mode, key
  derivation and strength remain unresolved. Neither round counts nor unrelated
  crypto code prove packet encryption or a "stronger than AES-128" claim.
- No raw captures, scan logs, or identifiers are distributed with this repository.
