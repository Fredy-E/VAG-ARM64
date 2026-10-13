package com.vagdiag

import android.app.Activity
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.method.ScrollingMovementMethod
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.GridLayout
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
 *
 * v0.2.0-preview.1 revamps only the bench workbench presentation (grouped
 * discovery vs active bench actions, bounded log panel, small-screen grid).
 * Transport, USB permission/lifecycle, worker threading, log cap/save
 * behavior, off-vehicle confirmation and identity-observation limits are
 * unchanged. No ECU diagnostics/coding/adaptation/fault clearing/firmware
 * is implemented. Compile/JVM tests are not Android UI or hardware proof.
 */
class MainActivity : Activity() {

    companion object {
        private const val ACTION_USB_PERMISSION = "com.vagdiag.USB_PERMISSION"
        private const val MAX_LOG_CHARS = 20000
        // Workbench palette: readable navy/blue with a restrained gold accent.
        // Light pairings (measured AA): heading 0xFF204FB0 on 0xFFF2F5FA,
        // hint 0xFF4D617E on 0xFFF2F5FA, status/log 0xFF1D2B44 on 0xFFF2F5FA/white,
        // ink 0xFFEEF4FF on navy 0xFF142034, gold 0xFFD4A94E + muted 0xFFADBDD2 on navy.
        // Night pairings (measured AA, system uiMode): heading 0xFF8CBCFF,
        // hint 0xFFC4D2E6, status/log 0xFFEEF4FF on night root/surface,
        // gold 0xFFE0B25C on night header 0xFF060B14.
        private const val NAVY_BG = 0xFF0C1422
        private const val NAVY_SURFACE = 0xFF142034
        private const val INK_ON_NAVY = 0xFFEEF4FF
        private const val MUTED_ON_NAVY = 0xFFADBDD2
        private const val GOLD_ACCENT = 0xFFD4A94E // restrained gold bench accent
        private const val NIGHT_HEADER_BG = 0xFF060B14
        private const val NIGHT_SURFACE = 0xFF1A2A45
        private const val NIGHT_HEADING = 0xFF8CBCFF
        private const val NIGHT_HINT = 0xFFC4D2E6
        private const val NIGHT_GOLD = 0xFFE0B25C
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
        logLine("VagDiag 0.2.0-preview.1 bench USB prototype - vehicle diagnostics not implemented")
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

    private fun dp(v: Float): Int =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v, resources.displayMetrics).toInt()

    // System dark/light adaptation via the existing platform stack (no new
    // libraries): follow resources.configuration.uiMode so content follows the
    // system night setting on recreate. Manifest theme is untouched.
    private fun isNightMode(): Boolean =
        (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
            Configuration.UI_MODE_NIGHT_YES

    private fun buildUi(): ScrollView {
        val night = isNightMode()
        val outer = ScrollView(this)
        outer.isFocusable = false

        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setBackgroundColor((if (night) NAVY_BG else 0xFFF2F5FA).toInt())
        val pad = dp(12f)
        root.setPadding(pad, pad, pad, pad)
        outer.addView(root, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT))

        // Masthead: navy bench header with explicit experimental status + version.
        val header = LinearLayout(this)
        header.orientation = LinearLayout.VERTICAL
        header.setBackgroundColor((if (night) NIGHT_HEADER_BG else NAVY_BG).toInt())
        header.setPadding(dp(12f), dp(12f), dp(12f), dp(12f))
        val title = TextView(this).apply {
            text = "VagDiag — USB bench"
            textSize = 19f
            setTextColor(INK_ON_NAVY.toInt())
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            isFocusable = false
            if (Build.VERSION.SDK_INT >= 28) {
                isAccessibilityHeading = true
            }
        }
        header.addView(title)
        val subtitle = TextView(this).apply {
            text = "Experimental bench prototype · v0.2.0-preview.1"
            textSize = 13f
            setTextColor((if (night) NIGHT_HINT else MUTED_ON_NAVY).toInt())
            isFocusable = false
        }
        header.addView(subtitle)
        val benchNote = TextView(this).apply {
            text = "Off-vehicle USB bench only — no vehicle connection for active probing."
            textSize = 12.5f
            setTextColor((if (night) NIGHT_GOLD else GOLD_ACCENT).toInt())
            isFocusable = false
        }
        header.addView(benchNote)
        root.addView(header)

        // Restrained gold divider line under the masthead. Decorative: hidden
        // from accessibility services so it is not announced.
        val goldRule = View(this).apply {
            setBackgroundColor((if (night) NIGHT_GOLD else GOLD_ACCENT).toInt())
            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
        }
        root.addView(goldRule, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, dp(2f)).apply {
            topMargin = dp(0f); bottomMargin = dp(10f)
        })

        // Safety panel: explicit limits. Styling does not upgrade these claims.
        // Full text stays the accessible content (no terse contentDescription).
        val safety = TextView(this).apply {
            text = "Safety — bench only: ECU diagnostics, coding, adaptation, fault " +
                "clearing and firmware are not implemented. Init probe transmits " +
                "observed USB traffic whose command meanings are not fully " +
                "established and needs explicit off-vehicle confirmation; the app " +
                "cannot verify disconnection. Identify only observes volunteered " +
                "text — a banner match is not authenticity proof. 'Clear' clears " +
                "this log, not ECU faults."
            textSize = 13f
            setTextColor(INK_ON_NAVY.toInt())
            setBackgroundColor((if (night) NIGHT_SURFACE else NAVY_SURFACE).toInt())
            setPadding(dp(10f), dp(10f), dp(10f), dp(10f))
            isFocusable = true
        }
        root.addView(safety)

        // Cable / connection status with accessible live announcement.
        // Full status text stays the accessible content (no terse description).
        root.addView(sectionHeading("Cable / connection status"))
        statusView = TextView(this).apply {
            textSize = 14.5f
            setTextColor((if (night) INK_ON_NAVY else 0xFF1D2B44).toInt())
            if (night) {
                setBackgroundColor(NAVY_BG.toInt())
            }
            text = "No cable found. Plug the HEX-USB cable into the phone (USB-OTG)."
            setPadding(dp(4f), dp(4f), dp(4f), dp(8f))
            isFocusable = true
            accessibilityLiveRegion = View.ACCESSIBILITY_LIVE_REGION_POLITE
        }
        root.addView(statusView)

        // Group 1: passive discovery (no transmit).
        root.addView(sectionHeading("1 — Discovery (passive, no transmit)"))
        root.addView(sectionHint("Find the observed cable, claim it, or passively observe identity text."))
        val discoveryGrid = GridLayout(this).apply {
            columnCount = 2
            alignmentMode = GridLayout.ALIGN_BOUNDS
            useDefaultMargins = true
        }
        discoveryGrid.addView(gridCell(button("Find") { findCable() }))
        discoveryGrid.addView(gridCell(button("Connect") { connectNow() }))
        discoveryGrid.addView(gridCell(button("Identify") { identifyNow() }))
        root.addView(discoveryGrid)

        // Group 2: active bench actions (off-vehicle confirmation required).
        root.addView(sectionHeading("2 — Bench actions (active, off-vehicle confirmation required)"))
        root.addView(sectionHint("Init probe asks for explicit off-vehicle confirmation before transmitting."))
        val benchGrid = GridLayout(this).apply {
            columnCount = 2
            alignmentMode = GridLayout.ALIGN_BOUNDS
            useDefaultMargins = true
        }
        benchGrid.addView(gridCell(button("Init probe") { confirmBenchProbe() }))
        benchGrid.addView(gridCell(button("Read 2s") { readTwoSeconds() }))
        root.addView(benchGrid)

        // Group 3: bounded session log with its own save/clear actions.
        root.addView(sectionHeading("Session log (bounded, app-private)"))
        root.addView(sectionHint("Bounded on-screen log. Save writes to the app-private files dir. Clear clears this log only."))
        val logActions = GridLayout(this).apply {
            columnCount = 2
            alignmentMode = GridLayout.ALIGN_BOUNDS
            useDefaultMargins = true
        }
        logActions.addView(gridCell(button("Save log") { saveLog() }))
        logActions.addView(gridCell(button("Clear") { log.setLength(0); renderLog() }))
        root.addView(logActions)

        // Readable monospaced bounded log. No animation, so reduced-motion safe.
        // Full log text stays the accessible content (no terse description).
        // The fixed-height scroller is not a separate focus stop; the log text
        // itself is the single focusable stop, avoiding a redundant trap.
        logView = TextView(this).apply {
            typeface = android.graphics.Typeface.MONOSPACE
            textSize = 12f
            setTextColor((if (night) INK_ON_NAVY else 0xFF1D2B44).toInt())
            setBackgroundColor((if (night) NAVY_SURFACE else 0xFFFFFFFF).toInt())
            setPadding(dp(8f), dp(8f), dp(8f), dp(8f))
            setHorizontallyScrolling(true)
            movementMethod = ScrollingMovementMethod()
            isFocusable = true
        }
        val scroll = ScrollView(this).apply {
            isFocusable = false
        }
        scroll.addView(logView, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT))
        root.addView(scroll, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, dp(240f)).apply {
            topMargin = dp(4f)
        })
        return outer
    }

    private fun sectionHeading(s: String): TextView =
        TextView(this).apply {
            text = s
            textSize = 14.5f
            setTextColor((if (isNightMode()) NIGHT_HEADING else 0xFF204FB0).toInt())
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setPadding(0, dp(10f), 0, dp(2f))
            isFocusable = false
            if (Build.VERSION.SDK_INT >= 28) {
                isAccessibilityHeading = true
            }
        }

    private fun sectionHint(s: String): TextView =
        TextView(this).apply {
            text = s
            textSize = 12.5f
            setTextColor((if (isNightMode()) NIGHT_HINT else 0xFF4D617E).toInt())
            setPadding(0, 0, 0, dp(4f))
            isFocusable = false
        }

    private fun gridCell(b: Button): Button {
        val lp = GridLayout.LayoutParams(
            GridLayout.spec(GridLayout.UNDEFINED, 1f),
            GridLayout.spec(GridLayout.UNDEFINED, 1f)).apply {
            width = 0
            height = GridLayout.LayoutParams.WRAP_CONTENT
        }
        b.layoutParams = lp
        return b
    }

    private fun button(label: String, action: () -> Unit): Button {
        val b = Button(this)
        b.text = label
        b.contentDescription = label
        b.isFocusable = true
        b.minimumHeight = dp(48f)
        b.minimumWidth = dp(48f)
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
