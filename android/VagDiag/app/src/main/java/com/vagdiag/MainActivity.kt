package com.vagdiag

import android.app.Activity
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.method.ScrollingMovementMethod
import android.util.TypedValue
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.vagdiag.proto.CableIdentity
import com.vagdiag.proto.RtFrame
import com.vagdiag.usb.FtdiCable
import com.vagdiag.usb.FtdiCommands
import java.util.concurrent.Executors

/**
 * VagDiag - independent experimental USB bench transport tool.
 *
 * v0.1.1 adds an off-vehicle confirmation before active bench probing and
 * labels observed identity text without claiming authenticity. ECU diagnostics,
 * coding, adaptation and fault clearing are not implemented.
 */
class MainActivity : Activity() {

    companion object {
        private const val ACTION_USB_PERMISSION = "com.vagdiag.USB_PERMISSION"
        private const val MAX_LOG_CHARS = 20000
    }

    private lateinit var usbManager: UsbManager
    private lateinit var logView: TextView
    private lateinit var statusView: TextView
    private val worker = Executors.newSingleThreadExecutor()
    private val main = Handler(Looper.getMainLooper())
    private val log = StringBuilder()
    private var cable: FtdiCable? = null
    private var device: UsbDevice? = null

    private val permissionReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action != ACTION_USB_PERMISSION) return
            val ok = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)
            logLine("usb permission granted=$ok")
            if (ok) connectNow() else setStatus("Permission denied")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        usbManager = getSystemService(Context.USB_SERVICE) as UsbManager
        setContentView(buildUi())
        registerPermissionReceiver()
        logLine("VagDiag 0.1.1 bench USB prototype - vehicle diagnostics not implemented")
        findCable()
    }

    override fun onResume() {
        super.onResume()
        if (device == null) findCable()
    }

    override fun onDestroy() {
        runCatching { unregisterReceiver(permissionReceiver) }
        cable?.close()
        worker.shutdownNow()
        super.onDestroy()
    }

    private fun registerPermissionReceiver() {
        val f = IntentFilter(ACTION_USB_PERMISSION)
        if (Build.VERSION.SDK_INT >= 33) {
            registerReceiver(permissionReceiver, f, Context.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            registerReceiver(permissionReceiver, f)
        }
    }

    private fun buildUi(): LinearLayout {
        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        val pad = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10f, resources.displayMetrics).toInt()
        root.setPadding(pad, pad, pad, pad)

        statusView = TextView(this).apply {
            textSize = 15f
            text = "No cable found. Plug the HEX-USB cable into the phone (USB-OTG)."
        }
        root.addView(statusView)

        val row = LinearLayout(this)
        row.orientation = LinearLayout.HORIZONTAL
        row.gravity = Gravity.START

        row.addView(button("Find") { findCable() })
        row.addView(button("Connect") { connectNow() })
        row.addView(button("Identify") { identifyNow() })
        root.addView(row)

        val row2 = LinearLayout(this)
        row2.orientation = LinearLayout.HORIZONTAL
        row2.gravity = Gravity.START
        row2.addView(button("Init probe") { confirmBenchProbe() })
        row2.addView(button("Read 2s") { readTwoSeconds() })
        row2.addView(button("Save log") { saveLog() })
        row2.addView(button("Clear") { log.setLength(0); renderLog() })
        root.addView(row2)

        logView = TextView(this).apply {
            typeface = android.graphics.Typeface.MONOSPACE
            textSize = 11f
            setHorizontallyScrolling(true)
            movementMethod = ScrollingMovementMethod()
        }
        val scroll = ScrollView(this)
        scroll.addView(logView, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f))
        val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f)
        root.addView(scroll, lp)
        return root
    }

    private fun button(label: String, action: () -> Unit): Button {
        val b = Button(this)
        b.text = label
        b.setOnClickListener { action() }
        return b
    }

    // ---------------- UI helpers ----------------

    private fun setStatus(s: String) = main.post { statusView.text = s }

    private fun logLine(s: String) {
        main.post {
            log.append(s).append('\n')
            if (log.length > MAX_LOG_CHARS) log.delete(0, log.length - MAX_LOG_CHARS)
            renderLog()
        }
    }

    private fun renderLog() {
        logView.text = log.toString()
    }

    private fun hex(data: ByteArray, max: Int = 64): String {
        val n = minOf(data.size, max)
        val sb = StringBuilder(n * 3)
        for (i in 0 until n) {
            val v = data[i].toInt() and 0xFF
            if (v < 16) sb.append('0')
            sb.append(Integer.toHexString(v)).append(' ')
        }
        if (data.size > n) sb.append("... (").append(data.size).append(" bytes)")
        return sb.toString().trim()
    }

    // ---------------- USB flow ----------------

    private fun findCable() {
        val found = usbManager.deviceList.values.firstOrNull { FtdiCable.matches(it) }
        if (found == null) {
            device = null
            setStatus("No cable (VID 0403 PID FA24) attached.")
            logLine("find: no matching device")
            return
        }
        device = found
        setStatus("Found: ${found.deviceName} (${found.vendorId.toString(16)}:${found.productId.toString(16)})")
        logLine("find: ${found.deviceName} ifaces=${found.interfaceCount}")
        if (!usbManager.hasPermission(found)) {
            logLine("requesting permission...")
            val pi = PendingIntent.getBroadcast(
                this, 0, Intent(ACTION_USB_PERMISSION), PendingIntent.FLAG_IMMUTABLE)
            usbManager.requestPermission(found, pi)
        }
    }

    private fun connectNow() {
        val d = device ?: run { logLine("connect: no device - press Find first"); return }
        worker.execute {
            try {
                cable?.close()
                val conn = usbManager.openDevice(d)
                if (conn == null) { logLine("openDevice failed (permission?)"); return@execute }
                val c = FtdiCable(d, conn)
                if (!c.open()) { logLine("claimInterface failed"); return@execute }
                cable = c
                val sn = c.serialHint()
                logLine("connected. serial='${sn}'")
                setStatus("Connected" + if (sn.isNotEmpty()) " ($sn)" else "")
            } catch (t: Throwable) {
                logLine("connect error: $t")
            }
        }
    }

    private fun confirmBenchProbe() {
        AlertDialog.Builder(this)
            .setTitle("Off-vehicle bench probe only")
            .setMessage("Disconnect the cable from the vehicle OBD port first. " +
                "This probe transmits observed USB traffic whose command meanings " +
                "are not fully established. The app cannot verify disconnection.")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Cable is off vehicle") { _, _ -> initProbe() }
            .show()
    }

    private fun initProbe() {
        val c = cable ?: run { logLine("init: connect first"); return }
        worker.execute {
            try {
                val results = c.applyVcdsPortOpenSequence()
                logLine("port-open ctrl results: $results")
                for ((i, frame) in RtFrame.vcdsTestProbeSequence().withIndex()) {
                    val n = c.write(frame)
                    logLine("tx[$i] (${frame.size}B, wrote $n): ${hex(frame)}")
                    Thread.sleep(150)
                }
                logLine("probe sent. Press 'Read 2s' to collect responses.")
            } catch (t: Throwable) {
                logLine("init error: $t")
            }
        }
    }

    private fun readTwoSeconds() {
        val c = cable ?: run { logLine("read: connect first"); return }
        worker.execute {
            try {
                val deadline = System.currentTimeMillis() + 2000
                var reads = 0
                var payloadBytes = 0
                while (System.currentTimeMillis() < deadline) {
                    val data = c.read()
                    if (data.isEmpty()) continue
                    reads++
                    val (status, payload) = FtdiCommands.splitRead(data)
                    if (payload.isEmpty()) {
                        logLine("rx status: ${hex(status)}")
                    } else {
                        payloadBytes += payload.size
                        logLine("rx (${data.size}B) status=${hex(status)} data: ${hex(payload)}")
                    }
                }
                logLine("read window done: $reads reads, $payloadBytes payload bytes")
            } catch (t: Throwable) {
                logLine("read error: $t")
            }
        }
    }

    /**
     * Passive identification: reads for 3 s WITHOUT transmitting anything and checks
     * for observed identity text (ROSSTECH + 2014 copyright banner).
     * A token/banner match is not proof of authenticity.
     */
    private fun identifyNow() {
        val c = cable ?: run { logLine("identify: connect first"); return }
        worker.execute {
            try {
                val payloads = ArrayList<ByteArray>()
                val deadline = System.currentTimeMillis() + 3000
                while (System.currentTimeMillis() < deadline) {
                    val data = c.read()
                    if (data.isEmpty()) continue
                    val (_, payload) = FtdiCommands.splitRead(data)
                    if (payload.isNotEmpty()) payloads.add(payload)
                }
                val r = CableIdentity.identify(payloads)
                logLine("identify: tokenHits=${r.tokenHits} banner=${r.bannerFound} " +
                    "replyFrames=${r.framesSeen} identityMatch=${r.genuine}")
                setStatus(if (r.genuine) "Ross-Tech identity observed (unverified)" else "No cable identity seen (passive read)")
            } catch (t: Throwable) {
                logLine("identify error: $t")
            }
        }
    }

    /** Save the on-screen log to the app-private files dir (no permissions needed). */
    private fun saveLog() {
        try {
            val name = "vagdiag-" + System.currentTimeMillis() + ".txt"
            val f = java.io.File(filesDir, name)
            f.writeText(log.toString())
            logLine("log saved: ${f.absolutePath} (${f.length()} bytes)")
        } catch (t: Throwable) {
            logLine("save error: $t")
        }
    }
}
