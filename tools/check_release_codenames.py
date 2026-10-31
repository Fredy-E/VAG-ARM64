"""Offline release-identity documentation checks; never modifies APKs or remotes."""
import argparse
import hashlib
import json
import re
from pathlib import Path


def require(condition, message):
    if not condition:
        raise ValueError(message)


def roadmap_pairs(text):
    section = text.split('## 2. Official version and codename roadmap', 1)[1].split('**Count:**', 1)[0]
    result = []
    for line in section.splitlines():
        if not line.startswith('|'):
            continue
        cells = [cell.strip().replace('**', '') for cell in line.strip('|').split('|')]
        if re.fullmatch(r'\d+\.\d+', cells[0]):
            result.append((cells[0], cells[1]))
    return result


def verify(root, original=None, download=None):
    doc = (root / 'docs/RELEASE_CODENAMES.md').read_text(encoding='utf-8')
    index = json.loads((root / 'docs/release-index.json').read_text(encoding='utf-8'))
    pairs = roadmap_pairs(doc)
    require(len(pairs) == 24 and len(dict(pairs)) == 24, 'Roadmap must have 24 distinct families')
    require(pairs == [(item['family'], item['codename']) for item in index['roadmap']], 'Roadmap/index mismatch')
    detail = re.findall(r'^### (\d+\.\d+) — ([^·\n]+) ·', doc, re.M)
    require([(version, name.strip()) for version, name in detail] == pairs, 'Detailed names/order changed')
    if original:
        require(pairs == roadmap_pairs(original.read_text(encoding='utf-8')), 'Original approved names changed')
    if download:
        require(doc == download.read_text(encoding='utf-8'), 'Downloads/canonical document mismatch')
    require(sum(line.startswith('```') for line in doc.splitlines()) % 2 == 0, 'Unbalanced fenced code blocks')
    require(not re.search(r'\b[A-Za-z]:[/\\]', doc), 'Machine-local absolute path in GitHub-ready document')
    require(not re.search(r'WVW[A-Z0-9]{14}', doc), 'Private vehicle VIN in release document')
    require('docs/RELEASE_CODENAMES.md' in (root / 'README.md').read_text(encoding='utf-8'), 'README link missing')
    source_spec = index.get('published_source', {
        'build_file': 'app/build.gradle', 'version_name': index['current_local_version'],
        'version_code': index['current_local_version_code']})
    build_relative = source_spec['build_file']
    require(build_relative in {'app/build.gradle', 'android/VagDiag/app/build.gradle'}, 'Unexpected source layout')
    gradle = (root / build_relative).read_text(encoding='utf-8')
    version = re.search(r"versionName\s+'([^']+)'", gradle).group(1)
    code = int(re.search(r'versionCode\s+(\d+)', gradle).group(1))
    require(version == source_spec['version_name'] and code == source_spec['version_code'], 'Published source metadata/index mismatch')
    families = dict(pairs)
    hashes = set()
    aligned = doc.split('### 2.1 ', 1)[1].split('### 2.2 ', 1)[0]
    rows = [[cell.strip().replace('**', '').replace('`', '') for cell in line.strip('|').split('|')]
            for line in aligned.splitlines() if line.startswith('|')]
    current = []
    for build in index['artifacts']:
        label = build['version_name']
        require(re.fullmatch(r'\d+\.\d+\.\d+(?:-[A-Za-z0-9.-]+)?', label) is not None, 'Malformed exact version')
        family = '.'.join(label.split('.')[:2])
        require(build['family'] == family and build['codename'] == families[family], 'Patch/family codename mismatch')
        digest = build['sha256']
        require(re.fullmatch(r'[a-f0-9]{64}', digest) is not None, 'Invalid SHA-256')
        require(digest not in hashes and digest in doc, 'Duplicate/missing artifact digest')
        hashes.add(digest)
        matched = [row for row in rows if build['artifact'] in row[0] and label in row[0]]
        require(len(matched) == 1, 'Missing/ambiguous alignment row for ' + build['artifact'])
        require(matched[0][1] == build['codename'] and matched[0][2] == str(build['version_code']), 'Artifact table/index mismatch')
        if build['published']:
            require(build['release_url'] in doc and build['tag'] in doc, 'Published release link/tag missing')
        require(build['physical_android_vehicle_qualified'] is False, 'Unqualified historical build promoted')
        if label == index['current_local_version']:
            current.append(build)
    require(len(current) == 1 and not current[0]['published'], 'Current local status ambiguous')
    require(current[0]['codename'] == index['current_local_codename'], 'Current codename mismatch')
    return {'roadmap_names_preserved': len(pairs), 'artifact_rows_checked': len(hashes),
            'public_prereleases': sum(build['published'] for build in index['artifacts']),
            'current_local_version': index['current_local_version'], 'published_source_version': version, 'current_local_codename': current[0]['codename'],
            'document_sha256': hashlib.sha256(doc.encode('utf-8')).hexdigest()}


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--root', type=Path, default=Path(__file__).resolve().parents[1])
    parser.add_argument('--original', type=Path)
    parser.add_argument('--download', type=Path)
    args = parser.parse_args()
    print(json.dumps(verify(args.root, args.original, args.download), indent=2))
