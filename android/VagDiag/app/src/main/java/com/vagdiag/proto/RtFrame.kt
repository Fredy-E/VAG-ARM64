package com.vagdiag.proto

/**
 * Observed Ross-Tech legacy cable USB framing from private bench/car captures.
 * Personal paths, raw captures and vehicle identifiers are not distributed.
 *
 * Grammar (both directions, 100% checksum pass over 5340 frames):
 *   Host→cable: [0x53][N][N-3 payload bytes][XOR of first N-1 bytes], N >= 4.
 *   Cable→host: [0x4D][N][N-3 payload bytes][XOR of first N-1 bytes], N >= 4.
 * Observed host lengths: 4, 5, 6, 7, 9, 13, 20, 29. Observed reply lengths: 4, 5, 7, 12, 20, 44.
 *
 * The lone 0xA5 byte sent before the frames is treated as a sync/wake byte.
 * Cable identity banner (plaintext in replies, 27x): "ROSSTECH" +
 * "Copyright (c) 2014 by Hex Microsystems Pty & Ross-Tech, LLC."
 *
 * Vehicle content (part numbers, VIN, DTCs) does NOT travel as USB plaintext; it rides
 * inside the 20/44-byte binary payloads. Opcode/transaction semantics are NOT established -
 * this class is byte-level framing only.
 */
object RtFrame {

    const val SYNC = 0xA5
    const val HEADER = 0x53
    const val REPLY_HEADER = 0x4D
    const val LENGTH = 0x04
    const val FRAME_SIZE = 4
    const val MIN_LEN = 4
    const val MAX_LEN = 64

    const val BANNER_TOKEN = "ROSSTECH"

    /** XOR checksum over [offset, offset + count). */
    fun xorOf(buf: ByteArray, offset: Int, count: Int): Int {
        var x = 0
        for (i in offset until offset + count) x = x xor (buf[i].toInt() and 0xFF)
        return x
    }

    /** Encode one 4-byte probe frame with the observed XOR checksum (bench-compatible). */
    fun encode(opcode: Int): ByteArray = encodePayload(byteArrayOf(opcode.toByte()))

    /** Encode an N-byte host frame ([53][N][payload][XOR]). */
    fun encodePayload(payload: ByteArray): ByteArray {
        require(payload.isNotEmpty()) { "payload must not be empty" }
        val n = payload.size + 3
        require(n in MIN_LEN..MAX_LEN) { "frame length out of range: $n" }
        val out = ByteArray(n)
        out[0] = HEADER.toByte()
        out[1] = n.toByte()
        payload.copyInto(out, 2)
        out[n - 1] = xorOf(out, 0, n - 1).toByte()
        return out
    }

    /** The exact byte sequence VCDS sent during its bench Test (first observed window). */
    fun vcdsTestProbeSequence(): List<ByteArray> = listOf(
        byteArrayOf(SYNC.toByte()),
        encode(0x02),
        encode(0x02),
        encode(0xA0),
        encode(0x08)
    )

    data class Decoded(val opcode: Int, val raw: ByteArray) {
        override fun equals(other: Any?): Boolean {
            if (other !is Decoded) return false
            return opcode == other.opcode && raw.contentEquals(other.raw)
        }

        override fun hashCode(): Int = opcode * 31 + raw.contentHashCode()
    }

    data class Frame(val header: Int, val payload: ByteArray, val raw: ByteArray) {
        override fun equals(other: Any?): Boolean {
            if (other !is Frame) return false
            return header == other.header && payload.contentEquals(other.payload) && raw.contentEquals(other.raw)
        }

        override fun hashCode(): Int = header * 31 + payload.contentHashCode()
    }

    /** Try to decode a 4-byte probe frame at [offset] (bench-compatible). Returns null if invalid. */
    fun tryDecode(buf: ByteArray, offset: Int): Decoded? {
        val f = tryDecodeFrame(buf, offset, HEADER) ?: return null
        if (f.raw.size != FRAME_SIZE) return null
        return Decoded(f.payload[0].toInt() and 0xFF, f.raw)
    }

    /** Try to decode an N-byte frame with the given [header] at [offset]. */
    fun tryDecodeFrame(buf: ByteArray, offset: Int, header: Int = HEADER): Frame? {
        if (offset < 0 || offset + MIN_LEN > buf.size) return null
        val h = buf[offset].toInt() and 0xFF
        val n = buf[offset + 1].toInt() and 0xFF
        if (h != header || n !in MIN_LEN..MAX_LEN) return null
        if (offset + n > buf.size) return null
        if (xorOf(buf, offset, n - 1) != (buf[offset + n - 1].toInt() and 0xFF)) return null
        return Frame(h, buf.copyOfRange(offset + 2, offset + n - 1), buf.copyOfRange(offset, offset + n))
    }

    /** Scan a buffer for all valid frames with the given [header], skipping noise bytes. */
    fun scanFrames(buf: ByteArray, header: Int = HEADER): List<Frame> {
        val out = ArrayList<Frame>()
        var i = 0
        while (i + MIN_LEN <= buf.size) {
            val d = tryDecodeFrame(buf, i, header)
            if (d != null) {
                out.add(d)
                i += d.raw.size
            } else {
                i++
            }
        }
        return out
    }

    /** Scan a buffer for all valid frames, skipping noise bytes. */
    fun scan(buf: ByteArray): List<Decoded> {
        val out = ArrayList<Decoded>()
        var i = 0
        while (i + FRAME_SIZE <= buf.size) {
            val d = tryDecode(buf, i)
            if (d != null) {
                out.add(d)
                i += FRAME_SIZE
            } else {
                i++
            }
        }
        return out
    }
}
