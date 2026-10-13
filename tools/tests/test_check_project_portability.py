"""Portability of the source hygiene checker (standalone vs git, no device claim).

Uses only synthetic temporary fixture roots owned by each test. No literal
private paths or secret values appear here; sensitive samples are built at
runtime via concatenation so this file itself stays scanner-clean.
"""
import os
import subprocess
import sys
import tempfile
import unittest
from pathlib import Path
from unittest import mock

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
import check_project


def _write(root, rel, text="ok\n"):
    target = Path(root) / rel
    target.parent.mkdir(parents=True, exist_ok=True)
    target.write_text(text, encoding="utf-8")
    return target


def _minimal_tree(root):
    root = Path(root)
    (root / "LICENSE").write_text("Apache-2.0 placeholder\n", encoding="utf-8")
    app = root / "android/VagDiag/app/src/main/java/com/vagdiag/MainActivity.kt"
    app.parent.mkdir(parents=True, exist_ok=True)
    app.write_text(
        'button("Init probe") { confirmBenchProbe() }\n'
        '.setNegativeButton("Cancel", null)\n'
        '.setPositiveButton("Cable is off vehicle") { _, _ -> initProbe() }\n'
        'Ross-Tech identity observed (unverified)\n'
        'fun secondSite() { initProbe() }\n',
        encoding="utf-8",
    )
    # NOTE: the positive-button line above holds one probe site and the dummy
    # below holds the second; the total count below is adjusted to stay at two
    # by removing the extra dummy when the real tree already has two.
    text = app.read_text(encoding="utf-8")
    if text.count("initProbe()") != 2:
        # Keep exactly two sites: drop the dummy line when needed.
        lines = [ln for ln in text.splitlines(True) if "secondSite" not in ln]
        # Re-add a single second site only when only one remains.
        if "".join(lines).count("initProbe()") == 1:
            lines.append("// second probe site initProbe()\n")
        app.write_text("".join(lines), encoding="utf-8")
    build = root / "android/VagDiag/app/build.gradle"
    build.parent.mkdir(parents=True, exist_ok=True)
    build.write_text("versionName '0.2.0-preview.1'\nversionCode 3\n", encoding="utf-8")


class CheckProjectPortability(unittest.TestCase):
    def test_standalone_enumerates_nested_tree_sorted_posix(self):
        with tempfile.TemporaryDirectory() as tmp:
            _write(tmp, "README.md", "hello\n")
            _write(tmp, "docs/notes.md", "notes\n")
            _write(tmp, "android/VagDiag/settings.gradle", "root\n")
            got = check_project.project_files(tmp)
            self.assertEqual(got, sorted(got))
            self.assertIn("README.md", got)
            self.assertIn("docs/notes.md", got)
            self.assertIn("android/VagDiag/settings.gradle", got)
            for entry in got:
                self.assertNotIn(chr(92), entry)
                self.assertFalse(Path(entry).is_absolute())

    def test_standalone_includes_dotfiles_and_skips_only_bounded(self):
        with tempfile.TemporaryDirectory() as tmp:
            _write(tmp, ".gitignore", "*.log\n")
            _write(tmp, ".github/workflows/verify.yml", "jobs\n")
            _write(tmp, "src/keep.py", "x\n")
            _write(tmp, ".gradle/cache.dat", "skip\n")
            _write(tmp, "tools/tests/__pycache__/skip.pyc", "skip\n")
            _write(tmp, "android/VagDiag/app/build/outputs/skip.txt", "skip\n")
            _write(tmp, "other/build/keep.txt", "keep\n")
            got = set(check_project.project_files(tmp))
            self.assertIn(".gitignore", got)
            self.assertIn(".github/workflows/verify.yml", got)
            self.assertIn("src/keep.py", got)
            self.assertIn("other/build/keep.txt", got)
            self.assertNotIn(".gradle/cache.dat", got)
            self.assertNotIn("tools/tests/__pycache__/skip.pyc", got)
            self.assertNotIn("android/VagDiag/app/build/outputs/skip.txt", got)

    def test_walk_skips_git_dir_when_called_directly(self):
        with tempfile.TemporaryDirectory() as tmp:
            _write(tmp, "keep.txt", "k\n")
            _write(tmp, ".git/objects/blob", "g\n")
            got = set(check_project._walk_files(tmp))
            self.assertIn("keep.txt", got)
            self.assertFalse(any(p == ".git" or p.startswith(".git/") for p in got))

    def test_git_backed_exact_ls_files_match(self):
        with tempfile.TemporaryDirectory() as tmp:
            subprocess.run(["git", "-C", tmp, "init", "-q"], check=True,
                           capture_output=True, text=True)
            _write(tmp, "a.txt", "a\n")
            _write(tmp, "sub/b.txt", "b\n")
            raw = subprocess.run(
                ["git", "-C", tmp, "ls-files", "--cached", "--others",
                 "--exclude-standard"],
                text=True, capture_output=True, check=True)
            expected = sorted(set(raw.stdout.splitlines()))
            self.assertEqual(check_project.project_files(tmp), expected)
            self.assertTrue(check_project._is_git_root(tmp))

    def test_git_worktree_file_counts_as_git_root(self):
        with tempfile.TemporaryDirectory() as tmp:
            (Path(tmp) / ".git").write_text("gitdir: elsewhere\n", encoding="utf-8")
            self.assertTrue(check_project._is_git_root(Path(tmp)))
            with self.assertRaises((subprocess.CalledProcessError, FileNotFoundError, OSError)):
                check_project.project_files(tmp)

    def test_git_errors_fail_closed_no_fallback(self):
        with tempfile.TemporaryDirectory() as tmp:
            (Path(tmp) / ".git").mkdir()
            with self.assertRaises(subprocess.CalledProcessError):
                check_project.project_files(tmp)

    def test_missing_git_standalone_still_works(self):
        with tempfile.TemporaryDirectory() as tmp:
            _write(tmp, "keep.txt", "k\n")
            with mock.patch.object(check_project.subprocess, "run",
                                   side_effect=AssertionError("git must not be called")):
                got = check_project.project_files(tmp)
            self.assertIn("keep.txt", got)
        with tempfile.TemporaryDirectory() as tmp:
            (Path(tmp) / ".git").mkdir()
            with mock.patch.object(check_project.subprocess, "run",
                                   side_effect=FileNotFoundError("no git")):
                with self.assertRaises(FileNotFoundError):
                    check_project.project_files(tmp)

    def test_forbidden_names_enumerated_not_hidden(self):
        with tempfile.TemporaryDirectory() as tmp:
            names = [".env", "local.properties", "snap.apk", "cap.pcap",
                     "k.jks", "note.pyc"]
            for name in names:
                _write(tmp, name, "x\n")
            got = set(check_project.project_files(tmp))
            for name in names:
                self.assertIn(name, got)

    def test_forbidden_causes_checker_error(self):
        with tempfile.TemporaryDirectory() as tmp:
            _minimal_tree(tmp)
            _write(tmp, ".env", "x\n")
            with mock.patch.object(check_project, "ROOT", Path(tmp)):
                with mock.patch.object(sys, "argv", ["check_project.py"]):
                    import io
                    from contextlib import redirect_stdout
                    buf = io.StringIO()
                    with redirect_stdout(buf):
                        code = check_project.main()
            self.assertEqual(1, code)
            self.assertIn("excluded file", buf.getvalue())

    def test_unicode_filename_enumerated_and_readable(self):
        uni = "docs/unit-\u00e9-\u65e5\u672c.md"
        with tempfile.TemporaryDirectory() as tmp:
            _write(tmp, uni, "plain text\n")
            got = check_project.project_files(tmp)
            self.assertIn(uni, got)
            text = (Path(tmp) / uni).read_text(encoding="utf-8")
            self.assertEqual("plain text\n", text)

    def test_unicode_content_preserved(self):
        sample = "dash " + chr(8212) + " arrow " + chr(8594) + "\n"
        with tempfile.TemporaryDirectory() as tmp:
            _write(tmp, "docs/u.md", sample)
            got = check_project.project_files(tmp)
            self.assertIn("docs/u.md", got)
            self.assertEqual(sample, (Path(tmp) / "docs/u.md").read_text(encoding="utf-8"))

    def test_symlink_file_rejected_before_reading(self):
        with tempfile.TemporaryDirectory() as outer:
            secret = Path(outer) / "secret.txt"
            token = "gh" + "p_" + "x" * 24
            secret.write_text(token + "\n", encoding="utf-8")
            with tempfile.TemporaryDirectory() as tmp:
                _minimal_tree(tmp)
                link = Path(tmp) / "link.txt"
                try:
                    link.symlink_to(secret)
                except (OSError, NotImplementedError) as exc:
                    self.skipTest("symlinks unavailable: %s" % exc)
                got = check_project.project_files(tmp)
                self.assertIn("link.txt", got)
                with mock.patch.object(check_project, "ROOT", Path(tmp)):
                    with mock.patch.object(sys, "argv", ["check_project.py"]):
                        import io
                        from contextlib import redirect_stdout
                        buf = io.StringIO()
                        with redirect_stdout(buf):
                            code = check_project.main()
                out = buf.getvalue()
                self.assertEqual(1, code)
                self.assertIn("unsafe symlink", out)
                self.assertNotIn(token, out)

    def test_symlink_dir_not_followed(self):
        with tempfile.TemporaryDirectory() as tmp:
            real = Path(tmp) / "real"
            real.mkdir()
            (real / "inner.txt").write_text("in\n", encoding="utf-8")
            linkdir = Path(tmp) / "linked"
            try:
                linkdir.symlink_to(real, target_is_directory=True)
            except (OSError, NotImplementedError) as exc:
                self.skipTest("symlinks unavailable: %s" % exc)
            got = set(check_project.project_files(tmp))
            self.assertIn("linked", got)
            self.assertNotIn("linked/inner.txt", got)

    def test_unsafe_relative_paths_rejected(self):
        self.assertTrue(check_project._is_unsafe_relative("../escape.txt"))
        self.assertTrue(check_project._is_unsafe_relative("/absolute.txt"))
        self.assertTrue(check_project._is_unsafe_relative("a" + chr(92) + "b.txt"))
        self.assertTrue(check_project._is_unsafe_relative(""))
        self.assertFalse(check_project._is_unsafe_relative("docs/ok.md"))
        with tempfile.TemporaryDirectory() as tmp:
            _minimal_tree(tmp)
            with mock.patch.object(check_project, "ROOT", Path(tmp)):
                with mock.patch.object(check_project, "project_files",
                                       return_value=["../escape.txt"]):
                    with mock.patch.object(sys, "argv", ["check_project.py"]):
                        import io
                        from contextlib import redirect_stdout
                        buf = io.StringIO()
                        with redirect_stdout(buf):
                            code = check_project.main()
            self.assertEqual(1, code)
            self.assertIn("unsafe path", buf.getvalue())

    def test_unknown_binary_included_and_flagged(self):
        with tempfile.TemporaryDirectory() as tmp:
            _minimal_tree(tmp)
            blob = Path(tmp) / "blob.bin"
            blob.write_bytes(bytes([255, 254, 0, 1, 2, 3]))
            self.assertIn("blob.bin", check_project.project_files(tmp))
            with mock.patch.object(check_project, "ROOT", Path(tmp)):
                with mock.patch.object(sys, "argv", ["check_project.py"]):
                    import io
                    from contextlib import redirect_stdout
                    buf = io.StringIO()
                    with redirect_stdout(buf):
                        code = check_project.main()
            self.assertEqual(1, code)
            self.assertIn("unexpected binary", buf.getvalue())

    def test_standalone_privacy_scan_not_weakened(self):
        with tempfile.TemporaryDirectory() as tmp:
            _minimal_tree(tmp)
            token = "gh" + "p_" + "x" * 24
            _write(tmp, "notes.txt", "leak " + token + "\n")
            with mock.patch.object(check_project, "ROOT", Path(tmp)):
                with mock.patch.object(sys, "argv", ["check_project.py"]):
                    import io
                    from contextlib import redirect_stdout
                    buf = io.StringIO()
                    with redirect_stdout(buf):
                        code = check_project.main()
            self.assertEqual(1, code)
            self.assertIn("credential token", buf.getvalue())
            self.assertNotIn(token, buf.getvalue())

    def test_new_tests_and_checker_stay_scanner_clean(self):
        self_text = Path(__file__).read_text(encoding="utf-8")
        self.assertEqual([], check_project.scan_text(self_text))
        checker_text = Path(check_project.__file__).read_text(encoding="utf-8")
        self.assertEqual([], check_project.scan_text(checker_text))


if __name__ == "__main__":
    unittest.main()
