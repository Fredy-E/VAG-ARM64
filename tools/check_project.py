"""Dependency-free publication hygiene and source-boundary checks."""
from __future__ import annotations

import argparse
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


def scan_text(text):
    return [(line_no, label) for line_no, line in enumerate(text.splitlines(), 1)
            for label, pattern in PATTERNS.items() if pattern.search(line)]


def project_files():
    result = subprocess.run(['git', '-C', str(ROOT), 'ls-files', '--cached', '--others', '--exclude-standard'],
                            text=True, capture_output=True, check=True)
    return sorted(set(result.stdout.splitlines()))


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--pre-approval', action='store_true', help='Allow LICENSE to remain undecided in the private draft')
    args = parser.parse_args()
    errors = []
    paths = project_files()
    assert paths, 'No publishable files found'
    for relative in paths:
        path = ROOT / relative
        if path.name in FORBIDDEN or path.suffix.lower() in FORBIDDEN_SUFFIXES:
            errors.append(f'{relative}: excluded file')
            continue
        try:
            text = path.read_text(encoding='utf-8')
        except UnicodeDecodeError:
            errors.append(f'{relative}: unexpected binary in source tree')
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
    if "versionName '0.1.1'" not in build:
        errors.append('Android version differs from release documentation')
    if errors:
        print('\n'.join(errors))
        return 1
    print(f'PASS: {len(paths)} publishable text files; privacy, local links and bench-guard contracts.')
    print('Static checks are not Android UI, physical USB, ECU or legal qualification.')
    return 0


if __name__ == '__main__':
    raise SystemExit(main())
