#!/usr/bin/env bash
# Build a game-ready Atlantis.jar from current sources.
#
# Usage:
#   bash scripts/build-bot-jar.sh <output-jar>            # fat jar, one file to deploy
#   bash scripts/build-bot-jar.sh <output-jar> --thin     # small jar + lib/ folder next to it
#
# Why this script exists instead of hand-rolled zip commands (each invariant
# below broke a game run in the past, see _AI/REVIEW.md Stage C notes):
#   1. Every source must be compiled (a stale file list once skipped new
#      classes, shipping a franken-jar) -> the source list is regenerated with
#      find on every run, minus the one file that cannot compile to Java 8.
#   2. Bytecode must be Java 8 (the container runs Corretto 8) -> --release 8.
#      That includes tests: the whole tree is compiled in one javac invocation,
#      so a test using a Java 9+ API (e.g. List.of) breaks the game jar. The
#      only exclusion is ATargetingTest, which needs the Nashorn engine.
#   3. No duplicate entries (zip appends shadows old classes) -> fresh zip.
#   4. bwapi/bwem and the native libraries come from JBWAPI-Rav, and its version
#      wins over anything stale (classpath shadowing otherwise crashes at
#      runtime).
#   5. The jar is built FROM SCRATCH - never on top of a previously built one.
#      That is what kept the artifact growing: every rebuild inherited the
#      previous jar's payload (Maps/, lib/*.jar, the whole test toolchain, and
#      in the IntelliJ setup even the jar itself - 62 MB -> 467 MB -> 540 MB).
#
# What goes in (everything else is deliberately left out, see scripts/lib-classpath.md):
#   - production classes: atlantis/, main/, jfap/, bweb/, jbweb/, jps/ (tests/
#     is compiled for the Java 8 check and then deliberately NOT shipped - the
#     harness is not part of the bot)
#   - everything in JBWAPI-Rav: bwapi/, bwem/, JNA (com/sun/jna/) and the
#     win32-*/ natives - jnativehook needs JNA at runtime
#   - runtime libraries only: jnativehook (atlantis.keyboard.AKeyboard), slf4j
#     (jnativehook's logging), kryo + minlog + reflectasm + objenesis
#     (atlantis.debug.object.ObjectToFile), vecmath (atlantis.util.Vector)
#   - nothing else: no JUnit, no Mockito, no ByteBuddy, no ArchUnit, no
#     lib/lib-unused/jbwapi-2.1.0.jar, no Maps/ (the bot reads maps and build
#     orders from the filesystem next to the jar, never from the classpath -
#     nothing in production code calls getResource*).
set -euo pipefail

cd "$(dirname "$0")/.."

OUT_JAR="${1:?usage: build-bot-jar.sh <output-jar> [--thin]}"
MODE="fat"
if [ "${2:-}" = "--thin" ]; then
    MODE="thin"
elif [ -n "${2:-}" ]; then
    echo "unknown option: $2" >&2
    exit 1
fi

WORK="$(mktemp -d)"
trap 'rm -rf "$WORK"' EXIT

# Libraries the game actually loads. Keep in sync with scripts/lib-classpath.md.
RUNTIME_LIBS=(
    "lib/JBWAPI-Rav.jar"
    "lib/jnativehook-2.2.1.jar"
    "lib/slf4j-api-2.0.12.jar"
    "lib/kryo-5.6.2.jar"
    "lib/kryo-deps/minlog-1.3.1.jar"
    "lib/kryo-deps/reflectasm-1.11.9.jar"
    "lib/kryo-deps/objenesis-3.2.jar"
    "lib/vecmath.jar"
)

# Libraries that must OVERRIDE what JBWAPI-Rav bundles, not just fill gaps.
#
# junixsocket: JBWAPI-Rav ships junixsocket 1.0.x, whose
# AFUNIXSocket.newInstance() calls java.net.Socket.setCreated() - a method that
# does not exist after Java 8. On a modern JVM (this machine runs 17) the call
# throws
#     IllegalStateException: Cannot find method "setCreated" in java.net.Socket
# and JBWAPI swallows it as a plain connection failure, so the bot loops on
# "Unable to open communications socket" against a perfectly healthy OpenBW
# server (measured 2026-10-07). That transport is the only way a POSIX client
# attaches, so the fix is to ship a junixsocket that works on this JVM.
OVERRIDE_LIBS=(
    "lib/junixsocket-common-2.10.1.jar"
    "lib/junixsocket-native-common-2.10.1.jar"
)

find src -name "*.java" \
    | grep -v "src/tests/unit/ATargetingTest.java" \
    | sort > "$WORK/sources.txt"

CP="$(find lib -path '*lib-unused*' -prune -o -name '*.jar' -print | tr '\n' ':')"
mkdir -p "$WORK/classes"
# Fail loudly instead of shipping a franken-jar.
javac --release 8 -nowarn -cp "$CP" -d "$WORK/classes" @"$WORK/sources.txt"

# Leftovers of an exploded jar that sit next to the sources: class files only,
# no sources, never compiled - drop them so they cannot be packaged.
for junk in "atlantis (copy)" bwapi bwta com META-INF; do
    rm -rf "$WORK/classes/$junk"
done
# Nothing under tests/ is packaged, and nothing may reference it either: as of
# 2026-10-04 production code imports no test class at all (the ports in
# _AI/NEXT.md #28 closed the last five, and #28's starengine question ended with
# the simulator deleted rather than given ports). Before that, 15 production
# classes reached into the harness - AUnit -> tests.fakes.FakeUnit, Bullets ->
# tests.fakes.FakeBullets, ClearCountCache -> tests.unit.helpers.ClearAllCaches -
# and the jar had to carry 119 harness classes to keep them loadable. The
# self-check below turns that from a comment into an invariant: if a production
# class ever names tests/ again, the build fails here instead of shipping a
# bigger jar.
#
# (The two JUnit classes that used to live in the production tree have moved to
# src/tests/acceptance/production/, so nothing under atlantis/ is a test class
# any more.)

mkdir -p "$(dirname "$OUT_JAR")"
python3 - "$OUT_JAR" "$WORK/classes" "$MODE" "${RUNTIME_LIBS[@]}" "--" "${OVERRIDE_LIBS[@]}" <<'EOF'
import sys, zipfile, os, posixpath

out_jar, classes, mode = sys.argv[1], sys.argv[2], sys.argv[3]
rest = sys.argv[4:]
sep = rest.index('--')
runtime_libs = rest[:sep]
override_libs = rest[sep + 1:]


def _is_native(name):
    """A library's platform payload: keep every platform's, not just win32."""
    return (name.endswith('.so') or name.endswith('.dylib')
            or name.endswith('.dll') or name.startswith('win32-x86')
            or '/linux/' in name or '/darwin/' in name or '/freebsd/' in name)

# --- our compiled classes -------------------------------------------------
# Everything under tests/ is dropped here, not filtered later: it is compiled so
# the whole tree is type-checked as Java 8, and it is not part of the bot.
NOT_SHIPPED = ('tests/', 'starengine/')
ours = {}
skipped = 0
for root, _, files in os.walk(classes):
    for fn in files:
        full = os.path.join(root, fn)
        arc = os.path.relpath(full, classes).replace(os.sep, '/')
        if arc.startswith(NOT_SHIPPED):
            skipped += 1
            continue
        if fn.endswith('.class'):
            ours[arc] = open(full, 'rb').read()
        else:  # resources: there are none in use, but never lose one silently
            print('WARN: packaged resource %s' % arc)

# --- everything JBWAPI-Rav brings -----------------------------------------
# All of it, not just bwapi/bwem: the jar also carries JNA (com/sun/jna/**)
# with its win32/x86-64 natives, and jnativehook (atlantis.keyboard.AKeyboard)
# needs it at runtime. Taking only bwapi/bwem produced a jar that died in the
# game with:
#   UnsatisfiedLinkError: Native library (com/sun/jna/win32-x86/jnidispatch.dll)
#   not found in resource path ([file:/.../AI/Atlantis.jar])  (GAME_C628E0B7)
rav = zipfile.ZipFile('lib/JBWAPI-Rav.jar', 'r')
rav_entries = {}
for name in rav.namelist():
    if name.endswith('/') or name == 'META-INF/MANIFEST.MF':
        continue
    # JBWAPI-Rav itself ships a stray maven-wrapper.jar at its root - build
    # detritus of whoever assembled that jar, 48 KB of nothing.
    if name.startswith('maven-wrapper'):
        continue
    rav_entries[name] = rav.read(name)

# src/bwem/BWMap.java is a FORK of the BWEM API - it adds chokes(), which the
# atlantis code calls (Chokes.chokes() and friends). So *our* compiled bwem must
# win over JBWAPI-Rav's; JBWAPI-Rav only fills the gaps (bwapi/**, JNA, natives,
# and every bwem class we do not compile). The deployed jar that worked before
# had exactly this layering, whatever a stale comment in an older version of this
# script claimed.

manifest = ['Manifest-Version: 1.0', 'Main-Class: main.Main']
# Multi-Release MUST be declared: the packaged junixsocket is a multi-release
# jar (META-INF/versions/9|21/...), and without this flag the JVM ignores those
# entries. The loader then cannot resolve its NAR metadata and throws
#     UnsatisfiedLinkError: Could not load native library junixsocket-native
#     for architecture [amd64-Linux]
# even with the .so present and correctly named (measured 2026-10-07). The
# previous manifest had no such line, which is why the OpenBW client could
# never attach while an isolated classpath test connected fine.
manifest.append('Multi-Release: true')
if mode == 'thin':
    # `java -jar` (that is how scbw starts a bot, see sc-docker
    # docker/scripts/play_common.sh) honours a relative Class-Path, so the
    # libraries live in a lib/ folder next to the jar and stay replaceable -
    # JBWAPI-Rav included, so the jar itself stays ~2.4 MB.
    manifest.append('Class-Path: ' + ' '.join(
        'lib/' + posixpath.basename(lib) for lib in runtime_libs
    ))
manifest_bytes = ('\r\n'.join(manifest) + '\r\n\r\n').encode('utf-8')

with zipfile.ZipFile(out_jar, 'w', zipfile.ZIP_DEFLATED) as zout:
    zout.writestr('META-INF/MANIFEST.MF', manifest_bytes)
    entries = dict(ours)
    if mode == 'fat':
        # JBWAPI-Rav whole (bwapi/, bwem/, JNA, natives), but without overwriting
        # anything we compiled.
        for arc, data in rav_entries.items():
            entries.setdefault(arc, data)
        # Overrides BEFORE the plain runtime libs: these replace classes JBWAPI
        # already contributed (junixsocket), so they must assign, not setdefault.
        # See OVERRIDE_LIBS above for why.
        #
        # The whole org/newsclub package from JBWAPI-Rav is dropped first, and
        # this is the part that is easy to get wrong: JBWAPI ships junixsocket
        # 1.0.x (20 classes, AFUNIXSocket extends java.net.Socket), the override
        # ships 2.10.1 (1500+ classes, AFUNIXSocket extends AFSocket). Overriding
        # only the classes that exist in BOTH leaves a mix - the new
        # AFUNIXSocket.next to the old AFUNIXSocketImpl - and the class loader
        # then throws on the first call, which JBWAPI reports as a plain
        # connection failure (measured 2026-10-07, "Cannot find method
        # setCreated" and then a silent non-connection). Never a mix: drop the
        # old package entirely, then lay the new one down.
        for arc in list(entries):
            if arc.startswith('org/newsclub/'):
                del entries[arc]
            # The 1.0.x NATIVE payload lives under lib/, not under
            # org/newsclub/: lib/<arch>/jni/libjunixsocket-native-1.0.2.so. The
            # 2.10.1 loader looks for its own .so by name and finds the old one
            # first otherwise, throwing UnsatisfiedLinkError.
            elif 'junixsocket-native-1.' in arc:
                del entries[arc]
            # The old fork's maven metadata (no.fiken.oss.junixsocket) is read by
            # NativeLibraryLoader to decide WHICH native to load; leaving it in
            # makes the loader look for the wrong artefact even when the right
            # .so is packaged (measured 2026-10-07 - this was the last blocker).
            elif arc.startswith('META-INF/maven/no.fiken.oss.junixsocket/'):
                del entries[arc]
        for lib in override_libs:
            with zipfile.ZipFile(lib, 'r') as z:
                for name in z.namelist():
                    if name.endswith('/') or name == 'META-INF/MANIFEST.MF':
                        continue
                    # NOT just .class and natives: junixsocket 2.x decides WHICH
                    # native to load from its META-INF/native-image/**/resource-config.json
                    # descriptors. Dropping them made the loader throw
                    #     UnsatisfiedLinkError: Could not load native library
                    #     junixsocket-native for architecture [amd64-Linux]
                    # even though the .so was right there in the jar (measured
                    # 2026-10-07 - the .so alone is not enough).
                    if name.endswith('.class') or _is_native(name) \
                            or name.startswith('META-INF/native-image/') \
                            or name.startswith('META-INF/maven/'):
                        entries[name] = z.read(name)
                        # junixsocket 2.10.1 ships its Linux natives under
                        # lib/<arch>-<compiler>/jni/ (e.g. amd64-Linux-clang),
                        # but the loader builds the path from the JVM's own
                        # architecture string and looks under lib/amd64-Linux/jni/
                        # - so it reports the architecture as unsupported even
                        # though the .so is in the jar (measured 2026-10-07:
                        # UnsatisfiedLinkError "Could not load native library
                        # junixsocket-native for architecture [amd64-Linux]").
                        # Alias every -clang native to the plain architecture
                        # name, which is what the loader actually asks for.
                        if '/jni/' in name and '-clang/' in name:
                            entries[name.replace('-clang/', '/')] = z.read(name)
        for lib in runtime_libs:
            if lib.endswith('JBWAPI-Rav.jar'):
                continue  # already merged above, and it must not overwrite our bwem fork
            with zipfile.ZipFile(lib, 'r') as z:
                for name in z.namelist():
                    if name.endswith('/') or name == 'META-INF/MANIFEST.MF':
                        continue
                    if name.endswith('.class') or _is_native(name):
                        entries.setdefault(name, z.read(name))
    for arc, data in entries.items():
        zout.writestr(arc, data)

    if mode == 'thin':
        lib_dir = os.path.join(os.path.dirname(os.path.abspath(out_jar)), 'lib')
        os.makedirs(lib_dir, exist_ok=True)
        for lib in runtime_libs:
            with open(lib, 'rb') as src, open(os.path.join(lib_dir, os.path.basename(lib)), 'wb') as dst:
                dst.write(src.read())

# --- self-checks ----------------------------------------------------------
with zipfile.ZipFile(out_jar) as z:
    names = z.namelist()
    assert len(names) == len(set(names)), 'duplicate entries in %s' % out_jar
    # lib/ is allowed (JNA keeps its natives under lib/amd64-Linux-gpp/...), but a
    # nested jar is not: that was the double packaging.
    forbidden = [n for n in names
                 if 'Maps/' in n or n.endswith('.jar')
                 or n.startswith('org/junit') or n.startswith('org/mockito')
                 or n.startswith('net/bytebuddy') or n.startswith('com/tngtech')
                 or n.startswith('tests/') or n.startswith('starengine/')]
    assert not forbidden, 'test data, harness classes or nested jars leaked in: %s' % forbidden[:5]
    assert 'main/Main.class' in names, 'entry point missing'

    # The payload is only the right size if nothing in it still names the harness.
    # A class can reference tests/ without importing it (reflection, a string), so
    # the constant pool is scanned rather than the source.
    ours_refs = [n for n in names
                 if n.endswith('.class') and (b'tests/' in z.read(n) or b'starengine/' in z.read(n))]
    assert not ours_refs, 'production classes reference the harness: %s' % ours_refs[:5]
    # our forked bwem.BWMap adds chokes() to the BWEM API; if JBWAPI-Rav's
    # version shadowed it, every choke lookup dies at runtime with
    # NoSuchMethodError: bwem.BWMap.chokes()Ljava/util/List; (GAME_BA5E7286)
    bwmap = zipfile.ZipFile(out_jar).read('bwem/BWMap.class')
    assert b'chokes' in bwmap, 'bwem.BWMap in the jar has no chokes() - wrong version packaged'
    if mode == 'fat':
        for required in ('bwapi/BWClient.class', 'bwem/BWEM.class', 'com/sun/jna/Native.class'):
            assert required in names, 'missing %s (JBWAPI-Rav must be packaged whole)' % required
    else:
        # thin: JBWAPI-Rav stays a library, so it must be on the Class-Path and
        # really present next to the jar
        class_path = zipfile.ZipFile(out_jar).read('META-INF/MANIFEST.MF').decode()
        assert 'JBWAPI-Rav.jar' in class_path, 'thin jar without JBWAPI-Rav on the Class-Path'
        for lib in runtime_libs:
            assert os.path.exists(os.path.join(os.path.dirname(os.path.abspath(out_jar)),
                                               'lib', os.path.basename(lib))), \
                'thin mode: %s missing from lib/' % lib

# The packaged junixsocket must be the Java-8+-compatible one, not the 1.0.x
# JBWAPI-Rav bundles: the old AFUNIXSocket calls Socket.setCreated(), which
# throws on any JVM after 8 and makes every POSIX client fail to attach
# (measured 2026-10-07). Assert the override really won.
# The whole junixsocket package must be the 2.10.1 one, never a mix with
# JBWAPI's 1.0.x: the old AFUNIXSocketImpl next to the new AFSocket-based
# AFUNIXSocket breaks the loader, and JBWAPI reports it as a plain
# connection failure (measured 2026-10-07).
afunix = zipfile.ZipFile(out_jar).read('org/newsclub/net/unix/AFUNIXSocket.class')
assert b'setCreated' not in afunix, (
    'junixsocket in the jar still calls Socket.setCreated() - the JBWAPI copy '
    'was not overridden, and the OpenBW client will fail to attach on Java 9+')
assert any('AFUNIXSocketAddress' in n for n in names), \
    'junixsocket override not packaged (no AFUNIXSocketAddress class)'
assert 'org/newsclub/net/unix/AFSocket.class' in names, \
    'junixsocket 2.x base class missing - the package is a mix of 1.0.x and 2.10.1'
# No 1.0.x native may survive next to the 2.10.1 classes: the loader picks
# the wrong one and the client dies with UnsatisfiedLinkError.
stale = [n for n in names if 'junixsocket-native-1.' in n]
assert not stale, 'stale junixsocket 1.0.x native in the jar: %s' % stale[:3]
assert any('junixsocket-native-2.10.1.so' in n for n in names), \
    'junixsocket 2.10.1 native .so missing from the jar'
# The native-image descriptors decide which .so the loader picks. Without
# them the loader reports the architecture as unsupported even with the
# library present (measured 2026-10-07).
assert any(n.startswith('META-INF/native-image/') and n.endswith('resource-config.json')
           for n in names), \
    'junixsocket native-image descriptors missing - the loader cannot pick a native'

size_mb = os.path.getsize(out_jar) / 1024.0 / 1024.0
print('OK: %s  %.1f MB  %d entries  mode=%s  (%d harness/simulator classes not shipped)'
      % (out_jar, size_mb, len(names), mode, skipped))
EOF