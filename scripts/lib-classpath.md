# What ships in the game jar (and what must never ship)

`Atlantis.jar` is a fat jar started with `java -jar` — that is what scbw does
(`sc-docker/docker/scripts/play_common.sh`, `-jar "${BOT_EXECUTABLE}"`).
So everything the game loads has to be **inside** the jar, and everything it
does not has to stay **out** of it.

## Contents (6.3 MB fat / 2.1 MB + lib/ thin)

| What | From | Needed by |
|---|---|---|
| `atlantis/**`, `main/**`, `jfap/**`, `starengine/**`, `bweb/**`, `jbweb/**`, `jps/**` | compiled from `src/` | the bot itself |
| `tests/fakes/**`, `tests/unit/helpers/**`, `tests/acceptance/WorldStubForTests` | compiled from `src/` | **15 production classes import the test harness** (see below) |
| `bwapi/**`, `bwem/**`, `com/sun/jna/**`, `win32-*/**` | `lib/JBWAPI-Rav.jar`, whole | the BWAPI bridge, JNA (jnativehook needs it), natives |
| `com/esotericsoftware/**`, `org/objenesis/**`, `org/reflectasm/**`, `org/minlog/**` | `lib/kryo-5.6.2.jar`, `lib/kryo-deps/*` | `atlantis.util.object.ObjectToFile` |
| `com/github/kwhat/jnativehook/**`, `org/slf4j/**` | `lib/jnativehook-2.2.1.jar`, `lib/slf4j-api-2.0.12.jar` | `atlantis.keyboard.AKeyboard` |
| `javax/vecmath/**` | `lib/vecmath.jar` | `atlantis.util.Vector` |

## Never in the jar

| What | Why not |
|---|---|
| `Maps/**` (36 MB) | the bot reads maps and build orders from the **filesystem** next to the jar (`ABuildOrderLoader.BUILD_ORDERS_PATH = "AI/build_orders/"`); nothing in production code calls `getResource*` |
| `lib/*.jar` as nested files | double packaging — the classes are already extracted at the root |
| `tests/**` **beyond what production imports** | production code reaches into `tests.fakes` / `tests.unit.helpers`, so those classes must be in the jar; the rest of the harness only needs to be *compiled* |
| JUnit, Mockito, ByteBuddy, Hamcrest, ArchUnit (~15 MB of classes) | test toolchain, never loaded by the game |
| `lib/lib-unused/jbwapi-2.1.0.jar` | unused, and it *shadows* the JBWAPI-Rav classes if it ever wins on the classpath |
| the artifact's own output | see below |

## How the size stayed stable (and how it blew up before)

Three independent mistakes, all of which had to be fixed — fixing only one of
them leaves the jar growing:

1. **The artifact output was inside a source folder.** `output-path` was
   `$PROJECT_DIR$/Atlantis.jar` while `$PROJECT_DIR$` itself was a
   `<sourceFolder>`. IntelliJ copies every non-Java file of a source folder into
   the compiler output, and the artifact contains `module-output` — so build *N*
   packaged build *N-1*. Measured: 62 MB → 467 MB → 540 MB, with an entry
   literally named `Atlantis.jar` (394 MB) inside the new one.
   *Fix:* output to `$PROJECT_DIR$/out/artifacts/` (`out/` is git-ignored) and
   keep exactly one source folder (`src`).
2. **Test libraries were extracted into the artifact** (17 `extracted-dir`
   entries) *and* the same jars were copied in as files under `lib/`, because
   `lib/` sat inside a source folder too.
   *Fix:* the artifact lists only the runtime libraries (table above).
3. **The CLI script inherited the previous deployed jar as its base**, so the
   bloat could never be cleaned up by rebuilding — `build-bot-jar.sh` now builds
   from scratch and asserts that no test data slipped in.

## Three things that look removable but are not

Both were found by running the game, not by reading the jar:

* **`com/sun/jna/**` (JNA, ~1.5 MB with natives) is inside `JBWAPI-Rav.jar`**
  and jnativehook needs it at runtime. Packaging "only bwapi/bwem" produced a
  jar that connected and then died with
  `UnsatisfiedLinkError: Native library (com/sun/jna/win32-x86/jnidispatch.dll)
  not found in resource path ([file:/.../AI/Atlantis.jar])` (`GAME_C628E0B7`).
  Package JBWAPI-Rav **whole**.
* **`tests/fakes/**` has to ship**: `AUnit` imports `tests.fakes.FakeUnit`,
  `Bullets` imports `tests.fakes.FakeBullets`, `ClearCountCache` imports
  `tests.unit.helpers.ClearAllCaches`, and two JUnit tests even live in the
  production tree (`src/atlantis/.../FindPositionForBaseNearestFreeTest.java`).
  Dropping `tests/` produced
  `NoClassDefFoundError: tests/fakes/FakeUnit` (`GAME_08792F08`). The test
  *frameworks* are still not needed — classes load lazily — but those classes
  are, so they stay until production stops importing the harness
  (`_AI/NEXT.md` #28).
* **`bwem/**` must be OUR compiled version.** `src/bwem/BWMap.java` is a fork of
  the BWEM API that adds `chokes()`, and `Chokes.chokes()` /
  `TooCloseToChoke` / `HasPosition` call it. If JBWAPI-Rav's `bwem` wins the
  merge, the bot connects to the game and then dies with
  `NoSuchMethodError: bwem.BWMap.chokes()Ljava/util/List;` (`GAME_BA5E7286`).
  Merge order is therefore: **our classes first, JBWAPI-Rav only fills the
  gaps** (`setdefault`), and the build asserts that the packaged `bwem.BWMap`
  really declares `chokes()`. An older comment in the script claimed the
  opposite layering - that comment was wrong, and the deployed jar it produced
  had our fork in it.

## Two shapes, both verified in a game

```bash
bash scripts/build-bot-jar.sh /path/Atlantis.jar            # fat jar, 6.3 MB, 1 file
bash scripts/build-bot-jar.sh /path/Atlantis.jar --thin     # 2.1 MB + lib/ (7.6 MB) next to it
```

`--thin` writes `Class-Path: lib/...` into the manifest, which `java -jar`
honours relative to the jar's own directory — the libraries then live in
`AI/lib/` and can be swapped without rebuilding the bot. Same code, same
`java -jar` launch, no launcher change needed.

## Which shape to use

* **fat** — one file to deploy, nothing can go missing in transit. Default.
* **thin** — smaller jar and replaceable libs, but the `lib/` folder is part of
  the deployment; deploy both or the bot dies with `NoClassDefFoundError`.