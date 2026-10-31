"""Release naming checks distinguish public source from private development builds."""
import importlib.util
import json
from pathlib import Path
import shutil
import tempfile
import unittest

ROOT = Path(__file__).resolve().parents[2]
spec = importlib.util.spec_from_file_location('release_names', ROOT / 'tools/check_release_codenames.py')
release_names = importlib.util.module_from_spec(spec)
spec.loader.exec_module(release_names)


class ReleaseNamingTests(unittest.TestCase):
    def fixture(self, directory):
        root = Path(directory)
        for name in ['README.md', 'docs/RELEASE_CODENAMES.md', 'docs/release-index.json',
                     'android/VagDiag/app/build.gradle']:
            target = root / name
            target.parent.mkdir(parents=True, exist_ok=True)
            shutil.copyfile(ROOT / name, target)
        return root

    def test_exact_roadmap_and_distinct_source_workstreams(self):
        result = release_names.verify(ROOT)
        self.assertEqual(24, result['roadmap_names_preserved'])
        self.assertEqual(7, result['artifact_rows_checked'])
        self.assertEqual('0.1.4-usb-fix-dev', result['current_local_version'])
        self.assertEqual('Ignition', result['current_local_codename'])
        self.assertEqual('0.2.0-preview.1', result['published_source_version'])

    def test_standalone_archive_needs_no_git_metadata(self):
        with tempfile.TemporaryDirectory() as directory:
            root = self.fixture(directory)
            self.assertFalse((root / '.git').exists())
            self.assertEqual(2, release_names.verify(root)['public_prereleases'])

    def test_patch_must_not_inherit_last_digit_codename(self):
        with tempfile.TemporaryDirectory() as directory:
            root = self.fixture(directory)
            path = root / 'docs/release-index.json'
            data = json.loads(path.read_text())
            data['artifacts'][-1]['codename'] = 'Crankshaft'
            data['current_local_codename'] = 'Crankshaft'
            path.write_text(json.dumps(data), encoding='utf-8')
            with self.assertRaisesRegex(ValueError, 'Patch/family codename mismatch'):
                release_names.verify(root)

    def test_public_source_must_not_claim_local_version(self):
        with tempfile.TemporaryDirectory() as directory:
            root = self.fixture(directory)
            path = root / 'docs/release-index.json'
            data = json.loads(path.read_text())
            data['published_source']['version_name'] = '0.1.4-usb-fix-dev'
            path.write_text(json.dumps(data), encoding='utf-8')
            with self.assertRaisesRegex(ValueError, 'Published source metadata/index mismatch'):
                release_names.verify(root)

    def test_missing_artifact_hash_fails(self):
        with tempfile.TemporaryDirectory() as directory:
            root = self.fixture(directory)
            path = root / 'docs/RELEASE_CODENAMES.md'
            data = json.loads((root / 'docs/release-index.json').read_text())
            path.write_text(path.read_text(encoding='utf-8').replace(data['artifacts'][-1]['sha256'], '0' * 64), encoding='utf-8')
            with self.assertRaisesRegex(ValueError, 'Duplicate/missing artifact digest'):
                release_names.verify(root)


if __name__ == '__main__':
    unittest.main()
