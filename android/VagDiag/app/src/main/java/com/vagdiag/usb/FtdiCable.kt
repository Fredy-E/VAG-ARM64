package com.vagdiag.usb

import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbEndpoint
import android.hardware.usb.UsbInterface

/**
 * Thin transport over the Ross-Tech HEX-USB (FTDI FT232R).
 *
 * Endpoint map from the verified capture: EP 0x81 (IN, bulk, 64B), EP 0x02 (OUT, bulk, 64B),
 * single interface (index 0). All USB access is synchronous; call from a worker thread.
 */
class FtdiCable(
    private val device: UsbDevice,
    private val connection: UsbDeviceConnection
) {
    companion object {
        const val VENDOR_ID = 0x0403
        const val PRODUCT_ID = 0xFA24
        const val EP_IN = 0x81
        const val EP_OUT = 0x02
        const val CONTROL_TIMEOUT_MS = 3000
        const val BULK_TIMEOUT_MS = 1200

        fun matches(d: UsbDevice): Boolean =
            d.vendorId == VENDOR_ID && d.productId == PRODUCT_ID
    }

    private var iface: UsbInterface? = null
    private val epIn: UsbEndpoint?
        get() = endpoint(EP_IN)
    private val epOut: UsbEndpoint?
        get() = endpoint(EP_OUT)

    val isOpen: Boolean get() = iface != null

    private fun endpoint(address: Int): UsbEndpoint? {
        val i = iface ?: return null
        for (n in 0 until i.endpointCount) {
            val e = i.getEndpoint(n)
            if (e.address == address) return e
        }
        return null
    }

    /** Claim the single interface. Returns false on failure. */
    fun open(): Boolean {
        if (isOpen) return true
        val i = device.getInterface(0)
        if (!connection.claimInterface(i, true)) return false
        iface = i
        return true
    }

    fun close() {
        iface?.let { runCatching { connection.releaseInterface(it) } }
        iface = null
        runCatching { connection.close() }
    }

    /** Vendor control OUT (bmRequestType 0x40). Returns bytes transferred or -1. */
    fun controlOut(request: Int, value: Int, index: Int = 0, data: ByteArray? = null): Int =
        connection.controlTransfer(0x40, request, value, index, data, data?.size ?: 0, CONTROL_TIMEOUT_MS)

    /** Apply the captured port-open control sequence. Returns the per-request results. */
    fun applyVcdsPortOpenSequence(): List<Int> =
        FtdiCommands.vcdsPortOpenSequence().map { c -> controlOut(c.request, c.value, c.index) }

    /** Bulk write. Returns bytes written or -1. */
    fun write(data: ByteArray): Int {
        val e = epOut ?: return -1
        return connection.bulkTransfer(e, data, data.size, BULK_TIMEOUT_MS)
    }

    /** Single bulk read up to [max] bytes. Empty array on timeout/no data. */
    fun read(max: Int = 4096): ByteArray {
        val e = epIn ?: return ByteArray(0)
        val buf = ByteArray(max)
        val n = connection.bulkTransfer(e, buf, max, BULK_TIMEOUT_MS)
        return if (n > 0) buf.copyOf(n) else ByteArray(0)
    }

    fun serialHint(): String {
        val s = runCatching { connection.serial }.getOrNull()
        return if (s.isNullOrBlank()) "" else s
    }
}
