import sys
import unittest
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
import check_project


class PreviewVersionChecks(unittest.TestCase):
    def test_exact_preview_metadata_is_accepted(self):
        self.assertEqual([], check_project.android_version_errors(
            "versionName '0.2.0-preview.1'\nversionCode 3\n"))

    def test_old_name_fails_with_correct_code(self):
        self.assertEqual(['Android preview versionName must match the declared candidate'],
                         check_project.android_version_errors("versionName '0.1.1'\nversionCode 3\n"))

    def test_old_code_fails_with_correct_name(self):
        self.assertEqual(['Android preview versionCode must match the declared candidate'],
                         check_project.android_version_errors("versionName '0.2.0-preview.1'\nversionCode 2\n"))

    def test_missing_fields_are_rejected(self):
        self.assertEqual(2, len(check_project.android_version_errors('defaultConfig { }')))

    def test_duplicate_name_or_code_is_rejected(self):
        valid = "versionName '0.2.0-preview.1'\nversionCode 3\n"
        for extra in ("versionName '0.2.0-preview.1'\n", 'versionCode 3\n'):
            with self.subTest(extra=extra):
                self.assertEqual(1, len(check_project.android_version_errors(valid + extra)))


if __name__ == '__main__':
    unittest.main()
