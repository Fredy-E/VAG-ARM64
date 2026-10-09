package com.vagdiag.proto

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CableIdentityTest {

    private fun replyFrame(payload: ByteArray): ByteArray {
        val n = payload.size + 3
        val raw = ByteArray(n)
        raw[0] = 0x4D.toByte()
        raw[1] = n.toByte()
        payload.copyInto(raw, 2)
        var x = 0
        for (i in 0 until n - 1) x = x xor (raw[i].toInt() and 0xFF)
        raw[n - 1] = x.toByte()
        return raw
    }

    @Test
    fun detects_genuine_banner() {
        // Real USB reads fragment mid-frame: pass raw chunks split at odd offsets,
        // plus one valid reply frame so the frame check also passes.
        val banner = CableIdentity.BANNER.toByteArray(Charsets.ISO_8859_1)
        val token = CableIdentity.TOKEN.toByteArray(Charsets.ISO_8859_1)
        val chunks = listOf(
            banner.copyOfRange(0, 20),
            banner.copyOfRange(20, banner.size),
            token,
            replyFrame(byteArrayOf(0x01, 0x02))
        )
        val r = CableIdentity.identify(chunks)
        assertTrue(r.bannerFound)
        assertTrue(r.framesSeen >= 1)
        assertTrue(r.genuine)
    }

    @Test
    fun token_counts_multiple_hits() {
        val t = CableIdentity.TOKEN.toByteArray(Charsets.ISO_8859_1)
        val r = CableIdentity.identify(listOf(t + t + t, replyFrame(byteArrayOf(0x05))))
        assertEquals(3, r.tokenHits)
        assertTrue(r.genuine)
    }

    @Test
    fun empty_input_is_not_genuine() {
        val r = CableIdentity.identify(listOf(ByteArray(0)))
        assertEquals(0, r.tokenHits)
        assertFalse(r.bannerFound)
        assertFalse(r.genuine)
    }

    @Test
    fun status_only_reads_are_not_genuine() {
        // bench off-car shape: FTDI status bytes only, no frames
        val r = CableIdentity.identify(
            listOf(byteArrayOf(0x01, 0x60), byteArrayOf(0x01, 0x60)))
        assertFalse(r.genuine)
    }
}
