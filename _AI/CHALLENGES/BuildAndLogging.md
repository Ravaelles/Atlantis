# Build, IDE and logging traps on Linux

Everything here cost real time in one session (2026-10-07) and none of it is
about game logic. Read this before concluding that "the code does not run".

## The one that wasted the most: the game played a DIFFERENT jar than we built

- **Symptom:** prints added to the sources never appeared in the game; the bot
  looked like it never executed the changed code at all. Later, production
  "did not work" while the fix was already in the sources.
- **Two independent causes, both had to be fixed:**
  1. **Two builders.** IntelliJ had an artifact (build-on-make) writing one jar,
     while `scripts/run-wine-full.sh` started a hardcoded
     `Z:\sc-ai\Atlantis\bots\AtlantisP\AI\Atlantis.jar`. The IDE wrote one path,
     the game loaded another.
  2. **A hardcoded Windows path.** Even with a correct target path, the script
     passed a literal `Z:\sc-ai\...` jar to the Wine JVM, so the built jar was
     never the one launched.
- **Fix:** one builder (`scripts/build-bot-jar.sh`), one configurable path
  (`JAR_OUT` at the top of `run-wine-full.sh`, default
  `$HOME/.scbw/bots/AtlantisP/AI/Atlantis.jar`), rebuilt before every game, and
  the Wine path DERIVED from it with `winepath` (fallback: map `/` to `Z:`).
- **Rule:** remove the IntelliJ artifact and turn off build-on-make for it. The
  script is not interchangeable with the IDE artifact: only the script ships
  junixsocket 2.10.1 with its native payload, declares `Multi-Release: true`,
  strips the old 1.0.x package and the stale NAR/maven metadata, and asserts all
  of that. See `OpenBW.md` for why those five details matter.

## "My println does not appear" - it did, 13 524 times

- **Symptom:** the owner added prints, ran a game, saw none of them, and
  concluded the commander was never invoked.
- **Cause:** the bot is a **separate process** (a Windows JVM under Wine started
  by the script), so its stdout went to `out/wine/client.log`, not to the IDE
  console. On Windows the bot WAS the IDE process, so its stdout was the console.
  The prints were all present - `We.protoss() = true` alone had been printed
  13 524 times (84% of a 16k-line log), and the IDE console showed the tail of
  the file.
- **Fix:** the script `tee`s the bot's output - the log keeps everything and the
  same lines stream to the script's stdout, which the IDE shows live because
  `UnixChaosGameLauncher` uses `ProcessBuilder.inheritIO()`. `LIVE_OUTPUT=0`
  silences the console but still writes the log.
- **Rule:** when a print "does not work", `grep -c` it in
  `out/wine/client.log` BEFORE changing any code. The console is a tail, not the
  whole file.

## Two sources of truth for the race disabled a whole race's code

- **Symptom:** one Zealot and one Dragoon, then no units ever again with a full
  bank. `ProtossDynamicUnitProductionCommander.applies()` never ran.
- **Cause:** `Main.ourRace()` said Protoss while `bwapi.ini` still said
  `race=Terran`. StarCraft started us as Terran; `OnGameStarted` read the race
  back FROM THE GAME into `AtlantisRaceConfig.MY_RACE`; and every race branch
  followed `MY_RACE`. So `We.protoss()` was false for the entire game and all
  Protoss code - including the unit producer - was dead. Nothing errored.
- **Fix (owner's ruling):** `Main.ourRace()` is the single source of truth.
  `We.*()` reads it directly; `AtlantisConfigChanger` switches on it instead of
  reading the game; the launcher writes it into `bwapi.ini`; and
  `OnGameStarted` warns loudly if the game still started us as something else.
- **Rule:** one fact, one owner. When two places can answer the same question,
  the one that wins is the one you did not expect.

## A Java 9+ API in a TEST breaks the GAME JAR build

- **Symptom:** the whole project failed to compile with one error in a test
  file, while the test itself looked fine on a modern javac.
- **Cause:** the whole tree - tests included - is compiled in ONE `javac`
  invocation with `--release 8` to produce the game jar (CONVENTIONS §16). So
  `String.lines()`, `InputStream.readAllBytes()`, `List.of(...)`, `var` in a
  test break the bot, not just the test.
- **Fix:** use Java 8 constructs (`split("\n")`, a manual read loop, an
  `ArrayList` field). This happened twice in one session - once in
  `WineMapGameTypeTest`, once in `LauncherBuildsTheJarTest`.
- **Rule:** if `javac --release 8` is the build, every test is production code
  for this purpose.

## `pkill -f <pattern>` kills the shell that runs it

- **Symptom:** a command "ended without completion / SIGKILL" with no output at
  all - looked like a hang.
- **Cause:** `pkill -f BWAPILauncher` (or `-f "java -jar Atlantis"`) matches the
  full command line, which includes the shell running the pkill. The shell kills
  itself before printing anything.
- **Fix:** `pkill -9 -x BWAPILauncher` (exact process name). Same trap for any
  `-f` pattern that appears in your own command line.

## `bots/*/AI/ENV` and jars are git-ignored - edits vanish silently

- **Symptom:** an ENV edit appeared "undone", and `git checkout` on the file did
  nothing.
- **Cause:** ENV and the jars under `bots/` are ignored, so git neither tracks
  nor restores them. `git checkout` is a no-op there, not a revert.
- **Fix:** revert ENV edits by editing the file; check
  `git check-ignore -v <path>` before trusting git to restore anything there.

## A test can be stale in the same way a jar can

- **Symptom:** `LauncherBuildsTheJarTest` reported the jar as older than the
  sources - correctly - but it was checking `bots/AtlantisP/AI/Atlantis.jar`,
  which nothing writes any more after the build target moved to `~/.scbw`.
- **Fix:** the test now checks the jar the launcher actually plays.
- **Rule:** when a path moves, grep the tests for the old one. A test that
  guards an obsolete path reports the past.

## Checklist before concluding "the code does not run"

1. `grep -c "<the print>" out/wine/client.log` - is it there at all?
2. `ls -la <JAR_OUT>` vs `stat -c %y <the source you changed>` - is the played
   jar newer than the sources?
3. Is the IDE artifact still enabled (build-on-make)? It should not be.
4. `bash scripts/run-tests.sh` - does the suite still compile under
   `--release 8`?
5. Only then start reading game logic.