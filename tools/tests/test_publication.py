import sys
import unittest
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
import check_project


class PublicationChecks(unittest.TestCase):
    def test_web_urls_are_not_local_paths(self):
        self.assertEqual([], check_project.scan_text('https://github.com/Fredy-E/VAG-ARM64'))

    def test_private_windows_path_detected(self):
        sample = 'D:' + chr(92) + 'private' + chr(92) + 'capture'
        self.assertIn('absolute local path', [label for _, label in check_project.scan_text(sample)])

    def test_fake_token_detected_without_printing_value(self):
        sample = 'gh' + 'p_' + 'x' * 24
        self.assertIn('credential token', [label for _, label in check_project.scan_text(sample)])

    def test_cable_serial_detected(self):
        sample = 'RT' + '9G38F0'
        self.assertIn('interface serial', [label for _, label in check_project.scan_text(sample)])

    def test_vehicle_identifier_detected(self):
        sample = 'WVW' + 'ZZZ1KZ6W000001'
        self.assertIn('vehicle identifier', [label for _, label in check_project.scan_text(sample)])

    def test_scanner_does_not_flag_its_own_regex(self):
        text = Path(check_project.__file__).read_text(encoding='utf-8')
        self.assertEqual([], check_project.scan_text(text))

    def test_active_probe_requires_confirmation(self):
        text = (check_project.ROOT / 'android/VagDiag/app/src/main/java/com/vagdiag/MainActivity.kt').read_text(encoding='utf-8')
        self.assertIn('button("Init probe") { confirmBenchProbe() }', text)
        self.assertIn('.setNegativeButton("Cancel", null)', text)
        self.assertIn('.setPositiveButton("Cable is off vehicle") { _, _ -> initProbe() }', text)
        self.assertEqual(2, text.count('initProbe()'))

    def test_android_sdk_setup_avoids_obsolete_tools(self):
        text = (check_project.ROOT / '.github/workflows/verify.yml').read_text(encoding='utf-8')
        self.assertIn('- uses: android-actions/setup-android@v3\n        with:\n          packages: platform-tools', text)
        self.assertIn("sdkmanager 'platforms;android-34' 'build-tools;34.0.0'", text)

    def test_identity_ui_does_not_claim_authenticity(self):
        text = (check_project.ROOT / 'android/VagDiag/app/src/main/java/com/vagdiag/MainActivity.kt').read_text(encoding='utf-8')
        self.assertIn('Ross-Tech identity observed (unverified)', text)
        self.assertNotIn('"Genuine HEX-USB cable"', text)


if __name__ == '__main__':
    unittest.main()
