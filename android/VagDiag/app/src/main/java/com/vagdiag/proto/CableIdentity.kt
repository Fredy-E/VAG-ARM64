package com.vagdiag.proto

/**
 * Passive cable identification from observed USB bytes.
 *
 * The genuine Ross-Tech HEX-USB cable announces itself in plaintext inside its
 * [0x4D]-framed replies (verified 27x per car session, 2026-10-09):
 * token "ROSSTECH" + banner "Copyright (c) 2014 by Hex Microsystems Pty & Ross-Tech, LLC."
 *
 * Pure logic, no Android imports, unit-testable. Sends nothing - identification is
 * read-only observation of bytes the cable volunteers.
 */
object CableIdentity {

    const val TOKEN = "ROSSTECH"
    const val BANNER = "Copyright (c) 2014 by Hex Microsystems Pty & Ross-Tech, LLC."

    data class Result(
        val tokenHits: Int,
        val bannerFound: Boolean,
        val framesSeen: Int,
        val genuine: Boolean
    )

    /** Scan reassembled cable reply payloads (FTDI status already stripped). */
    fun identify(payloads: List<ByteArray>): Result {
        var tokenHits = 0
        val blob = payloads.fold(ByteArray(0)) { acc, b -> acc + b }
        val text = blob.toString(Charsets.ISO_8859_1)
        var i = text.indexOf(TOKEN)
        while (i >= 0) {
            tokenHits++
            i = text.indexOf(TOKEN, i + 1)
        }
        val bannerFound = text.contains(BANNER)
        val frames = RtFrame.scanFrames(blob, RtFrame.REPLY_HEADER).size
        return Result(tokenHits, bannerFound, frames, tokenHits > 0 && frames > 0)
    }
}
