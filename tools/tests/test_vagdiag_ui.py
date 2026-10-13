"""VagDiag bench-UI preview regression (source-level).

Verifies the 0.2.0-preview.1 workbench revamp without claiming Android UI,
physical USB, ECU or hardware qualification. Compile/JVM success alone is
not UI or hardware proof; on-device visual and USB-OTG checks remain
unverified (WSA lacks USB passthrough).
"""
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
APP = ROOT / "android/VagDiag/app/src/main/java/com/vagdiag/MainActivity.kt"
BUILD = ROOT / "android/VagDiag/app/build.gradle"
MANIFEST = ROOT / "android/VagDiag/app/src/main/AndroidManifest.xml"


def read(p: Path) -> str:
    return p.read_text(encoding="utf-8")


class VagDiagUiPreview(unittest.TestCase):
    def test_preview_version_labels_current(self):
        build = read(BUILD)
        app = read(APP)
        self.assertIn("versionCode 3", build)
        self.assertIn("versionName '0.2.0-preview.1'", build)
        self.assertIn("0.2.0-preview.1", app)

    def test_experimental_bench_status_explicit(self):
        app = read(APP)
        self.assertIn("Experimental", app)
        self.assertIn("bench", app.lower())
        # Safety panel must state off-vehicle scope and unimplemented ECU work.
        self.assertIn("off-vehicle", app.lower())
        self.assertIn("not implemented", app.lower())

    def test_grouped_discovery_vs_bench_vs_log(self):
        app = read(APP)
        lowered = app.lower()
        self.assertIn("discovery", lowered)
        self.assertIn("bench", lowered)
        self.assertIn("session log", lowered)
        # Existing callbacks stay wired; no invented actions.
        for marker in (
            "findCable()",
            "connectNow()",
            "identifyNow()",
            'button("Init probe") { confirmBenchProbe() }',
            "readTwoSeconds()",
            "saveLog()",
        ):
            self.assertIn(marker, app)

    def test_small_screen_buttons_wrap_or_grid(self):
        app = read(APP)
        self.assertTrue(
            "GridLayout" in app or "columnCount" in app or "WRAP_CONTENT" in app,
            "action buttons must wrap or form a controlled grid",
        )
        # Minimum touch target (48dp) for bench buttons.
        self.assertTrue(
            "48" in app and ("minHeight" in app or "minimumHeight" in app or "setMinHeight" in app),
            "buttons need a 48dp minimum touch target",
        )

    def test_workbench_palette_and_log_readability(self):
        app = read(APP)
        # Navy/blue workbench with restrained gold accent (hex literals).
        self.assertTrue("0xFF0C1422" in app or "#0C1422" in app or "0c1422" in app.lower())
        self.assertTrue("0xFF" in app and ("Gold" in app or "gold" in app.lower() or "D4A9" in app))
        self.assertIn("MONOSPACE", app)
        self.assertIn("MAX_LOG_CHARS", app)

    def test_accessible_status_and_focus_order(self):
        app = read(APP)
        self.assertTrue(
            "ACCESSIBILITY_LIVE_REGION" in app or "contentDescription" in app,
            "cable/connection status needs an accessible live announcement",
        )
        self.assertIn("isFocusable", app)

    def test_transport_and_safety_contracts_preserved(self):
        app = read(APP)
        for marker in (
            '.setNegativeButton("Cancel", null)',
            '.setPositiveButton("Cable is off vehicle") { _, _ -> initProbe() }',
            "Ross-Tech identity observed (unverified)",
            "ACTION_USB_PERMISSION",
            "RECEIVER_NOT_EXPORTED",
        ):
            self.assertIn(marker, app)
        self.assertEqual(2, app.count("initProbe()"))
        # Clear clears the on-screen log, not ECU faults.
        self.assertIn("log.setLength(0)", app)
        self.assertNotIn("clearDTC", app)
        self.assertNotIn("Genuine HEX-USB", app)

    def test_no_new_runtime_permissions_or_frameworks(self):
        app = read(APP)
        manifest = read(MANIFEST)
        build = read(BUILD)
        self.assertNotIn("androidx.compose", app)
        self.assertNotIn("com.google.android.material", app)
        self.assertNotIn("INTERNET", manifest)
        self.assertNotIn("WRITE_EXTERNAL_STORAGE", manifest)
        self.assertIn("android.hardware.usb.host", manifest)
        self.assertNotIn("compose", build.lower())
        self.assertNotIn("material", build.lower())


if __name__ == "__main__":
    unittest.main()
