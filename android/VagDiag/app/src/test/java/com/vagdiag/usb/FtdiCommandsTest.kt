package com.vagdiag.usb

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FtdiCommandsTest {

    @Test
    fun divisor_math_matches_ftdi_convention() {
        assertEquals(313, FtdiCommands.divisorFor(9600))   // (3e6 + 4800) / 9600
        assertEquals(1, FtdiCommands.divisorFor(3_000_000))
        assertEquals(26, FtdiCommands.divisorFor(115200))  // 3057600 / 115200 = 26.54
    }

    @Test
    fun split_read_two_bytes_is_status_only() {
        val (status, payload) = FtdiCommands.splitRead(byteArrayOf(0x01, 0x60))
        assertArrayEquals(byteArrayOf(0x01, 0x60), status)
        assertEquals(0, payload.size)
    }

    @Test
    fun split_read_longer_carries_payload_after_status() {
        val (status, payload) = FtdiCommands.splitRead(byteArrayOf(0x01, 0x60, 0xAA.toByte(), 0xBB.toByte()))
        assertEquals(2, status.size)
        assertArrayEquals(byteArrayOf(0xAA.toByte(), 0xBB.toByte()), payload)
    }

    @Test
    fun open_sequence_covers_captured_requests() {
        val seq = FtdiCommands.vcdsPortOpenSequence()
        val values = seq.map { it.value }
        assertTrue(values.contains(0x809C))
        assertTrue(values.contains(0x4138))
        assertTrue(values.contains(0x001A))
        assertTrue(seq.any { it.request == FtdiCommands.REQ_RESET })
        assertTrue(seq.any { it.request == FtdiCommands.REQ_SET_DATA && it.value == FtdiCommands.SET_DATA_8N1 })
        assertTrue(seq.any { it.request == FtdiCommands.REQ_SET_LATENCY })
    }
}
