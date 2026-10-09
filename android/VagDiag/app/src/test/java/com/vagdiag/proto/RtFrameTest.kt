package com.vagdiag.proto

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class RtFrameTest {

    private fun bytes(vararg v: Int): ByteArray = ByteArray(v.size) { v[it].toByte() }

    @Test
    fun encodes_observed_check_cases() {
        // From the verified capture: 53 04 02 55 / 53 04 A0 F7 / 53 04 08 5F
        assertArrayEquals(bytes(0x53, 0x04, 0x02, 0x55), RtFrame.encode(0x02))
        assertArrayEquals(bytes(0x53, 0x04, 0xA0, 0xF7), RtFrame.encode(0xA0))
        assertArrayEquals(bytes(0x53, 0x04, 0x08, 0x5F), RtFrame.encode(0x08))
    }

    @Test
    fun decode_roundtrip() {
        for (op in intArrayOf(0x00, 0x02, 0x08, 0x55, 0xA0, 0xFF)) {
            val d = RtFrame.tryDecode(RtFrame.encode(op), 0)
            assertEquals("opcode $op", op, d!!.opcode)
        }
    }

    @Test
    fun rejects_bad_checksum() {
        assertNull(RtFrame.tryDecode(bytes(0x53, 0x04, 0x02, 0x56), 0))
    }

    @Test
    fun rejects_wrong_header_or_length() {
        assertNull(RtFrame.tryDecode(bytes(0x52, 0x04, 0x02, 0x54), 0))
        assertNull(RtFrame.tryDecode(bytes(0x53, 0x05, 0x02, 0x54), 0))
    }

    @Test
    fun scan_finds_frames_in_noise() {
        val noise = bytes(0x00, 0xFF, 0x01)
        val f1 = RtFrame.encode(0x02)
        val f2 = RtFrame.encode(0xA0)
        val buf = noise + f1 + bytes(0x13) + f2 + noise
        val found = RtFrame.scan(buf)
        assertEquals(2, found.size)
        assertEquals(0x02, found[0].opcode)
        assertEquals(0xA0, found[1].opcode)
    }

    @Test
    fun probe_sequence_matches_capture() {
        val seq = RtFrame.vcdsTestProbeSequence()
        assertEquals(5, seq.size)
        assertArrayEquals(bytes(0xA5), seq[0])
        assertArrayEquals(bytes(0x53, 0x04, 0x02, 0x55), seq[1])
        assertArrayEquals(bytes(0x53, 0x04, 0x02, 0x55), seq[2])
        assertArrayEquals(bytes(0x53, 0x04, 0xA0, 0xF7), seq[3])
        assertArrayEquals(bytes(0x53, 0x04, 0x08, 0x5F), seq[4])
    }

    @Test
    fun decode_single_frame_standalone_sync_not_frame() {
        assertNull(RtFrame.tryDecode(bytes(0xA5), 0))
        assertNotNull(RtFrame.tryDecode(RtFrame.encode(0x02), 0))
    }
}
