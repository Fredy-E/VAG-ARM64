package com.vagdiag.usb

/**
 * FTDI FT232R command layer for the Ross-Tech HEX-USB cable (VID 0403, PID FA24).
 *
 * Control-transfer parameters below are taken from the verified USB capture
 * (bench session 2026-10-07). The exact re-init sequence the Windows FTDI driver
 * performs when VCDS opens the port was captured and is reproduced in
 * [vcdsPortOpenSequence].
 *
 * Everything here is pure/byte-level; no Android imports, so it is unit-testable.
 */
object FtdiCommands {

    // Vendor requests (bmRequestType 0x40, wIndex 0x0000 in the capture)
    const val REQ_RESET = 0x00
    const val REQ_SET_MODEM_CTRL = 0x01
    const val REQ_SET_FLOW_CTRL = 0x02
    const val REQ_SET_BAUD = 0x03
    const val REQ_SET_DATA = 0x04
    const val REQ_SET_LATENCY = 0x09

    // SIO_RESET wValue semantics (FTDI convention, matches observed values 0/1/2)
    const val RESET_SIO = 0
    const val RESET_PURGE_RX = 1
    const val RESET_PURGE_TX = 2

    const val SET_DATA_8N1 = 0x0008

    /** FT232R default base clock in Hz (no external EEPROM overrides observed). */
    const val BASE_CLOCK_HZ = 3_000_000

    /** FTDI baud divisor for the standard 3 MHz base clock. */
    fun divisorFor(baud: Int): Int {
        require(baud > 0) { "baud must be positive" }
        return ((BASE_CLOCK_HZ + baud / 2) / baud).coerceAtLeast(1)
    }

    /**
     * Raw baud-rate register values observed from VCDS's session (as divisors at the
     * FTDI input). Their meaning (target baud / switching order) is NOT yet established -
     * do not guess. Listed in observed send order with capture counts.
     */
    val observedVcdsBaudDivisors = listOf(
        0x809C to 4, // sent x4
        0x4138 to 2, // sent x2
        0x001A to 2  // sent x2
    )

    data class CtrlRequest(val request: Int, val value: Int, val index: Int)

    /** Exact control sequence captured at port-open time (canonicalized, de-duplicated). */
    fun vcdsPortOpenSequence(latencyMs: Int = 1): List<CtrlRequest> = listOf(
        CtrlRequest(REQ_RESET, RESET_SIO, 0),
        CtrlRequest(REQ_SET_BAUD, 0x809C, 0),
        CtrlRequest(REQ_SET_BAUD, 0x4138, 0),
        CtrlRequest(REQ_SET_BAUD, 0x001A, 0),
        CtrlRequest(REQ_SET_DATA, SET_DATA_8N1, 0),
        CtrlRequest(REQ_SET_FLOW_CTRL, 0, 0),
        CtrlRequest(REQ_SET_MODEM_CTRL, 0x0100, 0),
        CtrlRequest(REQ_SET_MODEM_CTRL, 0x0200, 0),
        CtrlRequest(REQ_SET_LATENCY, latencyMs, 0)
    )

    /**
     * FTDI read parsing: a bulk read that returns exactly 2 bytes carries only the
     * modem/line status (observed constants: 01 60 with no car attached). Longer reads
     * are assumed to carry status(2) + payload; this split is UNVERIFIED until the
     * car-side capture shows real responses.
     */
    fun splitRead(data: ByteArray): Pair<ByteArray, ByteArray> {
        if (data.size <= 2) return data to ByteArray(0)
        return data.copyOfRange(0, 2) to data.copyOfRange(2, data.size)
    }
}
