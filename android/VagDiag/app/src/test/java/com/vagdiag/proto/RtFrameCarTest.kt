package com.vagdiag.proto

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertNotNull
import org.junit.Test

class RtFrameCarTest {

    private fun bytes(vararg v: Int): ByteArray = ByteArray(v.size) { v[it].toByte() }

    @Test
    fun nbyte_roundtrip_all_observed_host_lengths() {
        for (n in intArrayOf(4, 5, 6, 7, 9, 13, 20, 29)) {
            val payload = ByteArray(n - 3) { (it * 37 + 11).toByte() }
            val enc = RtFrame.encodePayload(payload)
            assertEquals(n, enc.size)
            assertEquals(0x53, enc[0].toInt() and 0xFF)
            assertEquals(n, enc[1].toInt() and 0xFF)
            val f = RtFrame.tryDecodeFrame(enc, 0, 0x53)
            assertNotNull("len $n", f)
            assertArrayEquals(payload, f!!.payload)
        }
    }

    @Test
    fun reply_roundtrip_all_observed_reply_lengths() {
        for (n in intArrayOf(4, 5, 7, 12, 20, 44)) {
            val payload = ByteArray(n - 3) { (it * 53 + 7).toByte() }
            // build a synthetic reply frame by hand: [4D][N][payload][XOR]
            val raw = ByteArray(n)
            raw[0] = 0x4D.toByte()
            raw[1] = n.toByte()
            payload.copyInto(raw, 2)
            var x = 0
            for (i in 0 until n - 1) x = x xor (raw[i].toInt() and 0xFF)
            raw[n - 1] = x.toByte()
            val f = RtFrame.tryDecodeFrame(raw, 0, 0x4D)
            assertNotNull("reply len $n", f)
            assertArrayEquals(payload, f!!.payload)
        }
    }

    @Test
    fun rejects_corrupt_checksum_and_short_buffer() {
        val good = RtFrame.encodePayload(byteArrayOf(0x01, 0x02, 0x03))
        val bad = good.copyOf()
        bad[bad.size - 1] = (bad.last().toInt() xor 0xFF).toByte()
        assertNull(RtFrame.tryDecodeFrame(bad, 0, 0x53))
        assertNull(RtFrame.tryDecodeFrame(good, 0, 0x4D)) // wrong header
        assertNull(RtFrame.tryDecodeFrame(bytes(0x53, 0x04), 0, 0x53))
    }

    @Test
    fun scanFrames_skips_noise_and_finds_mixed_lengths() {
        val f1 = RtFrame.encode(0x02)
        val f2 = RtFrame.encodePayload(ByteArray(10) { it.toByte() })
        val buf = bytes(0x00, 0xFF) + f1 + bytes(0x13) + f2 + bytes(0x00)
        val found = RtFrame.scanFrames(buf, 0x53)
        assertEquals(2, found.size)
        assertEquals(4, found[0].raw.size)
        assertEquals(13, found[1].raw.size)
    }

    @Test
    fun banner_token_constant_matches_capture() {
        assertEquals("ROSSTECH", RtFrame.BANNER_TOKEN)
    }
}
