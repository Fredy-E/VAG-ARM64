"""Dependency-free publication hygiene and source-boundary checks."""
from __future__ import annotations

import argparse
import os
import re
import subprocess
from pathlib import Path
from urllib.parse import unquote, urlsplit

ROOT = Path(__file__).resolve().parents[1]
PATTERNS = {
    'absolute local path': re.compile(r'(?<![A-Za-z0-9])(?:[A-Za-z]:[\\/]|/' + 'home' + r'/|/' + 'Users' + r'/)'),
    'credential token': re.compile(r'gh[pousr]_[A-Za-z0-9]{20,}|github_pat_[A-Za-z0-9_]{20,}|-----BEGIN [A-Z ]*PRIVATE KEY'),
    'vehicle identifier': re.compile(r'\b(?:WVW|WVG|WAU|WUA)[A-HJ-NPR-Z0-9]{14}\b'),
    'interface serial': re.compile(r'\bRT[A-Z0-9]{6}\b'),
    'retired account': re.compile('Froo' + 'dz-E', re.I),
}
FORBIDDEN = {'.env', 'local.properties', 'WORKSPACE.json'}
FORBIDDEN_SUFFIXES = {'.apk', '.aab', '.pcap', '.pcapng', '.jks', '.keystore', '.p12', '.pfx', '.pyc'}
EXPECTED_ANDROID_VERSION = '0.2.0-preview.1'
EXPECTED_ANDROID_VERSION_CODE = 3

# Bounded generated-directory skips for standalone (no-.git) enumeration only.
# Only these are skipped; everything else — including forbidden names like
# .env / local.properties, APK / capture / key suffixes, and unknown binaries —
# is enumerated so the hygiene checks below can flag it instead of hiding it.
SKIP_DIR_NAMES = frozenset({'.git', '.gradle', '__pycache__'})
# Exact Android app build-output prefix (relative POSIX). Only this prefix is
# skipped; other directories named `build` elsewhere are NOT skipped.
SKIP_BUILD_PREFIX = 'android/VagDiag/app/build'


def scan_text(text):
    return [(line_no, label) for line_no, line in enumerate(text.splitlines(), 1)
            for label, pattern in PATTERNS.items() if pattern.search(line)]


def android_version_errors(build_text):
    names = re.findall(r"(?m)^\s*versionName\s+['\"]([^'\"]+)['\"]\s*$", build_text)
    codes = re.findall(r'(?m)^\s*versionCode\s+(\d+)\s*$', build_text)
    errors = []
    if names != [EXPECTED_ANDROID_VERSION]:
        errors.append('Android preview versionName must match the declared candidate')
    if codes != [str(EXPECTED_ANDROID_VERSION_CODE)]:
        errors.append('Android preview versionCode must match the declared candidate')
    return errors


def _is_git_root(root):
    """True when the owned source root itself carries .git (dir or worktree file)."""
    git = Path(root) / '.git'
    try:
        return bool(git.is_dir() or git.is_file() or os.path.lexists(git))
    except OSError:
        return False


def _git_files(root):
    """Exact Git-backed publishable paths. Fail closed on any real Git error."""
    result = subprocess.run(['git', '-C', str(root), 'ls-files', '--cached', '--others', '--exclude-standard'],
                            text=True, capture_output=True, check=True)
    return sorted(set(result.stdout.splitlines()))


def _should_skip_dir(rel_posix):
    """Bounded skip predicate for directories (relative POSIX, '' for root)."""
    if not rel_posix or rel_posix == '.':
        return False
    parts = rel_posix.split('/')
    if any(p in SKIP_DIR_NAMES for p in parts):
        return True
    if rel_posix == SKIP_BUILD_PREFIX or rel_posix.startswith(SKIP_BUILD_PREFIX + '/'):
        return True
    return False


def _should_skip_file(rel_posix):
    """Files inside a skipped directory are skipped; all other files are kept."""
    parts = rel_posix.split('/')
    for i in range(1, len(parts)):
        if _should_skip_dir('/'.join(parts[:i])):
            return True
    if rel_posix == SKIP_BUILD_PREFIX or rel_posix.startswith(SKIP_BUILD_PREFIX + '/'):
        return True
    return False


def _walk_files(root):
    """Stdlib portable enumeration for standalone roots (no .git).

    Uses only os.walk/pathlib: no git executable, no parent-repo lookup.
    Includes dotfiles (e.g. .github, .gitignore), forbidden names, and unknown
    binaries so checks can flag them. Symlinked dirs are surfaced as entries
    without descending; symlinked files are surfaced so the caller can reject
    them before reading.
    """
    root = Path(root)
    found = set()
    for dirpath, dirnames, filenames in os.walk(root, topdown=True, followlinks=False):
        try:
            rel_dir = Path(dirpath).relative_to(root).as_posix()
        except ValueError:
            continue
        if rel_dir == '.':
            rel_dir = ''
        keep = []
        for name in list(dirnames):
            sub = (rel_dir + '/' + name) if rel_dir else name
            full = Path(dirpath) / name
            try:
                is_link = full.is_symlink()
            except OSError:
                is_link = False
            if is_link:
                found.add(sub)
                continue
            if _should_skip_dir(sub):
                continue
            keep.append(name)
        dirnames[:] = keep
        for name in filenames:
            sub = (rel_dir + '/' + name) if rel_dir else name
            if _should_skip_file(sub):
                continue
            found.add(sub)
    return sorted(found)


def project_files(root=None):
    """Publishable relative paths (POSIX, sorted).

    Git root (owned root carries .git file or dir): exact `git ls-files`
    behavior; real Git errors propagate fail-closed with no walk fallback.
    Standalone root (no .git): portable filesystem walk with bounded skips.
    """
    selected = Path(root) if root is not None else ROOT
    if _is_git_root(selected):
        return _git_files(selected)
    return _walk_files(selected)


def _is_unsafe_relative(relative):
    if not relative:
        return True
    if '\\' in relative or relative.startswith('/'):
        return True
    try:
        if Path(relative).is_absolute():
            return True
    except (OSError, ValueError):
        return True
    parts = relative.split('/')
    if any(p in ('', '.', '..') for p in parts):
        return True
    return False


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--pre-approval', action='store_true', help='Allow LICENSE to remain undecided in the private draft')
    args = parser.parse_args()
    errors = []
    try:
        paths = project_files()
    except subprocess.CalledProcessError as exc:
        detail = exc.stderr.strip() if exc.stderr else ''
        print(f'git file list failed (exit {exc.returncode})' + (f': {detail}' if detail else ''))
        return 1
    except FileNotFoundError:
        print('git executable missing for git-backed source root')
        return 1
    except OSError as exc:
        print(f'file enumeration failed: {exc}')
        return 1
    assert paths, 'No publishable files found'
    root_resolved = ROOT.resolve()
    for relative in paths:
        if _is_unsafe_relative(relative):
            errors.append(f'{relative}: unsafe path')
            continue
        path = ROOT / relative
        try:
            is_link = path.is_symlink()
        except OSError:
            errors.append(f'{relative}: unreadable path')
            continue
        if is_link:
            errors.append(f'{relative}: unsafe symlink')
            continue
        try:
            resolved = path.resolve()
        except OSError:
            errors.append(f'{relative}: unreadable path')
            continue
        if not resolved.is_relative_to(root_resolved):
            errors.append(f'{relative}: escaped project root')
            continue
        if path.is_dir():
            errors.append(f'{relative}: unexpected directory in file list')
            continue
        if not path.exists():
            errors.append(f'{relative}: missing file')
            continue
        if path.name in FORBIDDEN or path.suffix.lower() in FORBIDDEN_SUFFIXES:
            errors.append(f'{relative}: excluded file')
            continue
        try:
            text = path.read_text(encoding='utf-8')
        except UnicodeDecodeError:
            errors.append(f'{relative}: unexpected binary in source tree')
            continue
        except OSError:
            errors.append(f'{relative}: unreadable file')
            continue
        errors.extend(f'{relative}:{line}: {label}' for line, label in scan_text(text))
        if path.suffix == '.md':
            for target in re.findall(r'\[[^\]]*\]\(([^)\s]+)\)', text):
                if urlsplit(target).scheme:
                    continue
                resolved = (path.parent / unquote(target.split('#', 1)[0])).resolve()
                if not resolved.is_relative_to(ROOT.resolve()) or not resolved.exists():
                    errors.append(f'{relative}: unresolved local link')
    if not args.pre_approval and not (ROOT / 'LICENSE').is_file():
        errors.append('LICENSE: owner-approved license missing')
    app = ROOT / 'android/VagDiag/app/src/main/java/com/vagdiag/MainActivity.kt'
    text = app.read_text(encoding='utf-8')
    required = ['button("Init probe") { confirmBenchProbe() }',
                '.setNegativeButton("Cancel", null)',
                '.setPositiveButton("Cable is off vehicle") { _, _ -> initProbe() }',
                'Ross-Tech identity observed (unverified)']
    for marker in required:
        if marker not in text:
            errors.append('MainActivity.kt: bench guard or identity disclosure missing')
    if text.count('initProbe()') != 2:
        errors.append('MainActivity.kt: unexpected direct probe call site')
    build = (ROOT / 'android/VagDiag/app/build.gradle').read_text(encoding='utf-8')
    errors.extend(android_version_errors(build))
    if errors:
        print('\n'.join(errors))
        return 1
    print(f'PASS: {len(paths)} publishable text files; privacy, local links and bench-guard contracts.')
    print('Static checks are not Android UI, physical USB, ECU or legal qualification.')
    return 0


if __name__ == '__main__':
    raise SystemExit(main())
