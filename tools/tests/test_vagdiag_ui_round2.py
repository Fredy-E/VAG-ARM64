"""VagDiag round2 UI a11y/theme regression (source-level, no device claim).

Covers Stage-2 nits without weakening the original 8 round1 tests in
test_vagdiag_ui.py (left byte-identical): decorative rule silence, focus
clutter, 48dp width+height, no terse contentDescription hiding full text,
bounded log retention, API26-safe headings, system night adaptation via the
existing platform stack (no new libs), and measured WCAG contrast.

Source-level only: passing here is not on-device TalkBack, keyboard,
small-screen, or contrast proof. On-device behavior remains UNVERIFIED.
"""
import re
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
APP = ROOT / "android/VagDiag/app/src/main/java/com/vagdiag/MainActivity.kt"
BUILD = ROOT / "android/VagDiag/app/build.gradle"


def read(p: Path) -> str:
    return p.read_text(encoding="utf-8")


def srgb_lum(rgb):
    def lin(c):
        c = c / 255.0
        return c / 12.92 if c <= 0.04045 else ((c + 0.055) / 1.055) ** 2.4
    r, g, b = (lin(c) for c in rgb)
    return 0.2126 * r + 0.7152 * g + 0.0722 * b


def contrast(fg, bg):
    x, y = srgb_lum(fg), srgb_lum(bg)
    return (max(x, y) + 0.05) / (min(x, y) + 0.05)


def argb_int(lit: str):
    v = int(lit, 16) & 0xFFFFFFFF
    return ((v >> 16) & 0xFF, (v >> 8) & 0xFF, v & 0xFF)


class VagDiagRound2A11yTheme(unittest.TestCase):
    def test_decorative_rule_not_announced(self):
        app = read(APP)
        self.assertNotIn('contentDescription = "gold workbench accent rule"', app)
        self.assertRegex(app, r"importantForAccessibility\s*=\s*View\.IMPORTANT_FOR_ACCESSIBILITY_NO")

    def test_explicit_48dp_touch_target_width_and_height(self):
        app = read(APP)
        self.assertRegex(app, r"minimumWidth\s*=\s*dp\(48f\)")
        self.assertRegex(app, r"minimumHeight\s*=\s*dp\(48f\)")

    def test_heading_semantics_api26_safe(self):
        app = read(APP)
        self.assertIn("isAccessibilityHeading", app)
        # Heading API is 28+; an SDK guard keeps minSdk 26 safe.
        self.assertTrue(
            "Build.VERSION.SDK_INT >= 28" in app or "SDK_INT >= 28" in app,
            "accessibilityHeading must be guarded for API 26 safety",
        )

    def test_static_focus_clutter_reduced(self):
        app = read(APP)
        # Section titles/hints are headings/descriptions, not tab stops.
        heading = re.search(r"private fun sectionHeading.*?\{.*?\n\}", app, re.S)
        # Fall back to whole-file checks if the non-greedy span misses.
        self.assertIn("private fun sectionHeading", app)
        self.assertIn("private fun sectionHint", app)
        # Both helpers must mark themselves non-focusable.
        for marker in ("private fun sectionHeading", "private fun sectionHint"):
            start = app.index(marker)
            block = app[start:start + 800]
            self.assertIn("isFocusable = false", block, marker)
        # Scroll containers must not add redundant focus stops.
        self.assertIn("outer.isFocusable = false", app)
        # Masthead statics must not be focusable stops.
        title_block = app[app.index('text = "VagDiag'):app.index('text = "VagDiag') + 1200]
        self.assertNotIn("isFocusable = true", title_block)
        # Status, log, and buttons stay keyboard/screen-reader reachable.
        self.assertIn("accessibilityLiveRegion = View.ACCESSIBILITY_LIVE_REGION_POLITE", app)
        for marker in ("statusView = TextView", "logView = TextView", "private fun button("):
            start = app.index(marker)
            block = app[start:start + 1200]
            self.assertIn("isFocusable = true", block, marker)

    def test_full_safety_status_log_text_not_hidden(self):
        app = read(APP)
        # Terse contentDescriptions that replaced the real text must be gone.
        self.assertNotIn('contentDescription = "Safety: bench-only limits', app)
        self.assertNotIn('contentDescription = "Cable connection status"', app)
        self.assertNotIn('contentDescription = "Bounded session log"', app)
        # The full on-screen strings stay (TalkBack reads text, not a summary).
        self.assertIn("Safety — bench only", app)
        self.assertIn("No cable found. Plug the HEX-USB cable", app)
        self.assertIn("ACCESSIBILITY_LIVE_REGION_POLITE", app)
        # Buttons keep their label as the accessible name (not content hiding).
        self.assertIn("b.contentDescription = label", app)

    def test_bounded_readable_log_retained(self):
        app = read(APP)
        self.assertIn("dp(240f)", app)
        self.assertIn("MAX_LOG_CHARS", app)
        self.assertIn("MONOSPACE", app)
        self.assertIn("ScrollingMovementMethod", app)
        self.assertIn("setHorizontallyScrolling(true)", app)

    def test_system_night_adaptation_without_new_libs(self):
        app = read(APP)
        build = read(BUILD)
        self.assertIn("UI_MODE_NIGHT_MASK", app)
        self.assertIn("UI_MODE_NIGHT_YES", app)
        self.assertNotIn("androidx.compose", app)
        self.assertNotIn("com.google.android.material", app)
        self.assertNotIn("androidx.appcompat", app)
        self.assertNotIn("compose", build.lower())
        self.assertNotIn("material", build.lower())
        # Dark semantic literals must exist alongside the light ones.
        for lit in ("0xFF060B14", "0xFF8CBCFF", "0xFFC4D2E6", "0xFFE0B25C"):
            self.assertIn(lit, app, lit)
        # Light workbench literals stay for the original palette tests.
        for lit in ("0xFF0C1422", "0xFFD4A94E"):
            self.assertIn(lit, app, lit)

    def test_measured_semantic_contrast_aa(self):
        app = read(APP)
        pairs = [
            # (label, fg literal, bg literal) — every literal must be in source.
            ("light-heading on light-root", "0xFF204FB0", "0xFFF2F5FA"),
            ("light-hint on light-root", "0xFF4D617E", "0xFFF2F5FA"),
            ("light-status on light-root", "0xFF1D2B44", "0xFFF2F5FA"),
            ("light-log on white", "0xFF1D2B44", "0xFFFFFFFF"),
            ("safety-ink on navy-surface", "0xFFEEF4FF", "0xFF142034"),
            ("gold on navy-bg", "0xFFD4A94E", "0xFF0C1422"),
            ("muted on navy-bg", "0xFFADBDD2", "0xFF0C1422"),
            ("night-heading on night-root", "0xFF8CBCFF", "0xFF0C1422"),
            ("night-hint on night-root", "0xFFC4D2E6", "0xFF0C1422"),
            ("night-status on night-root", "0xFFEEF4FF", "0xFF0C1422"),
            ("night-log on night-surface", "0xFFEEF4FF", "0xFF142034"),
            ("night-gold on night-header", "0xFFE0B25C", "0xFF060B14"),
        ]
        for label, fg_lit, bg_lit in pairs:
            with self.subTest(label=label):
                self.assertIn(fg_lit, app, "fg " + fg_lit)
                self.assertIn(bg_lit, app, "bg " + bg_lit)
                ratio = contrast(argb_int(fg_lit), argb_int(bg_lit))
                self.assertGreaterEqual(
                    ratio, 4.5, "%s contrast %.2f below AA 4.5" % (label, ratio)
                )


if __name__ == "__main__":
    unittest.main()
