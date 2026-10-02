# How the tests in StardustDevEnvironment work

This is a map of `./tests-steamhammer --gtest_filter=RushDefense.*`: what the
binary is, what a test *is*, and what the filter actually selects. It is written
for the Atlantis migration, so the section "what this means for us" is the point
of it.

## 1. What `./tests-steamhammer` is

A compiled C++ program, built by CMake into `StardustDevEnvironment/build/test/`:

```
StardustDevEnvironment/
├── CMakeLists.txt              top level: openbw/bwapi, zstd, src, test
├── src/                        DemoAIModule - the "our bot" that ships with the env
└── test/                       the test runner
    ├── CMakeLists.txt          <- defines the tests-steamhammer / tests-locutus binaries
    ├── BWTest.{h,cpp}          the harness: one "game" per call to run()
    ├── Maps.{h,cpp}            map metadata (sscai / aiide collections)
    ├── Steamhammer.cpp         TEST(Steamhammer, ...) - full games vs Steamhammer
    ├── Locutus.cpp             TEST(Locutus, ...)    - full games vs Locutus
    └── RushDefense.cpp         TEST(RushDefense, ...) - scripted scenarios
```

It is **not** a bot launcher and **not** a JVM harness. Four things are linked
into the binary: **OpenBW** (a headless StarCraft engine), **BWAPI** (the C++
binding), **GoogleTest** (the test framework) and **Steamhammer** (the opponent,
as C++ source, `3rdparty/opponents/Steamhammer`). So:

* there is no StarCraft window, no Wine, no Docker, no `scbw.play`;
* the game is played **in-process**, as fast as the CPU allows (`setLocalSpeed(0)`);
* three MPQ files must sit next to the binary (`build/test/BROODAT.MPQ`,
  `STARDAT.MPQ`, `patch_rt.mpq`) - that is where OpenBW gets the game data.

## 2. What a test is

A GoogleTest `TEST(SuiteName, TestName)` whose body describes a **scenario** in a
`BWTest` struct and then calls `test.run()`:

```cpp
TEST(RushDefense, Steamhammer9PoolSpeed)
{
    BWTest test;
    test.map = Maps::GetOne("Python");       // fixed map -> deterministic
    test.randomSeed = 30841;                 // fixed seed -> deterministic
    test.opponentRace = BWAPI::Races::Zerg;
    test.opponentModule = []() {             // the opponent, as a C++ AIModule
        auto module = new UAlbertaBot::UAlbertaBotModule();
        Config::StardustTestStrategyName = "9PoolSpeed";
        return module;
    };
    test.frameLimit = 5000;                  // give up after 5000 frames
    test.expectWin = false;                  // we only care about the callback
    test.myInitialUnits = { ... };           // our scripted starting units
    test.opponentInitialUnits = { ... };     // enemy's scripted starting units
    test.onEndMine = [](bool won) {          // assertions run here
        EXPECT_TRUE(hasAProbe);
    };
    test.run();
}
```

`run()` (`test/BWTest.cpp`) does, in order:

1. **Picks the map** - the one given, or a random one from `Maps::Get("sscai")`
   when the test only sets `test.maps`.
2. **Picks the seed** - `randomSeed == -1` means "draw one at random", so a test
   without an explicit seed is *not* reproducible.
3. **Orders the scripted units into frames** (`scheduleInitialUnitCreation`):
   workers and overlords first, then (units waiting for creep, spaced 1000 frames
   apart), then pylons, then non-combat buildings, then combat buildings, then
   everything else. Without this the scenario would be illegal StarCraft and
   OpenBW would refuse it.
4. **`fork()`s the opponent into a child process.** The child runs Steamhammer
   with the requested strategy; the parent runs "our" side. Both processes talk
   to the same in-process OpenBW game through `BW::GameOwner`:
   `createMultiPlayerGame` -> `setRandomSeed(seed)` -> `startGame()` -> the test
   steps frames with `game.nextFrame()` in lockstep. Each side sets
   `setLocalSpeed(0)` in `afterOnStart`, i.e. "run as fast as possible".
5. **Runs the frame loop** until one of: game over, `frameLimit` frames
   (`h->leaveGame()`), or `timeLimit` seconds of wall clock. An exception thrown
   during a frame is caught, logged with a backtrace, and ends the game - a test
   never hangs.
6. **Asserts** on the parent side: `if (expectWin) EXPECT_TRUE(won);`, plus
   whatever the test's `onEndMine` callback does (that is where
   `EXPECT_TRUE(hasAProbe)` lives).
7. **Saves the evidence**: `replays/<Suite>_<Test>_<YYYYmmdd_HHMMSS>_<PASS|FAIL>.rep`,
   the CherryVis directory renamed next to it as `<replay>.rep.cvis`, and the log
   file moved to `<replay>.rep.log/`. So a failing run leaves you a replay you can
   open, plus the data Stardust's instrumentation produced.
8. **Reaps the opponent**: waits up to 5 s for the child to exit, then SIGKILL.

A crash of *our* side (SIGSEGV/SIGFPE/SIGABRT) is caught by a signal handler
that fails the test and prints a backtrace; a crash of the opponent is reported
but does **not** fail the test.

## 3. What `--gtest_filter=RushDefense.*` selects

GoogleTest names every test `<Suite>.<Test>` and the filter is a glob over that
name:

```
$ ./tests-steamhammer --gtest_list_tests
Steamhammer.
  RunTwenty          <- 20 full games vs Steamhammer on random AIIDE maps
  4PoolHard
RushDefense.
  Steamhammer9PoolSpeed   <- the scripted 9-pool-speed defence scenario
```

* `--gtest_filter=RushDefense.*` runs **only the `RushDefense` suite** - today
  exactly one test, `Steamhammer.Steamhammer9PoolSpeed`... more precisely
  `RushDefense.Steamhammer9PoolSpeed`.
* `Steamhammer.*` is the opposite kind of test: `RunTwenty` plays 20 complete
  games on randomly drawn AIIDE maps with randomly drawn seeds, sets
  `expectWin = false` and just counts wins/losses in `onEndMine`. It is a
  regression/balance sweep, not a scenario.
* Useful variations: `--gtest_filter=RushDefense.* --gtest_repeat=10`,
  `--gtest_break_on_failure`, `--gtest_list_tests`, `--gtest_output=xml:out.xml`.

So the usual split is: `RushDefense.*` (and any new scenario suite) while you
develop - fast, fixed map, fixed seed, one game - and `Steamhammer.RunTwenty`
before you push, because it is the only thing that tells you whether you got
worse against real play.

## 4. What this means for Atlantis (the migration question)

The harness plays **two linked C++ `AIModule`s**. `BWTest::myModule` is a
`std::function<BWAPI::AIModule*()>`; when a test leaves it unset, the runner uses
the environment's own `DemoAIModule` (`src/`, "based on BWAPI's ExampleAIModule").
Atlantis is a Java bot started with `java -jar`, so it **cannot be plugged into
this runner as it stands** - there is no slot for a non-linked bot. The
`test/CMakeLists.txt` even says so:

> Future non-linkable bots (.dll/.exe/.jar) get their own runner that spawns them
> instead of linking (DIP: BWTest depends on an AIModule factory).

That comment is the design brief for your migration, in three steps:

1. **Make "our side" a spawnable bot instead of a linked module.** A new
   `add_opponent_tests(tests-atlantis Atlantis.cpp)` target, or a variant of
   `BWTest` that starts a child process running `java -jar .../Atlantis.jar` and
   connects it to the OpenBW game through BWAPI's client protocol
   (`BWAPI::BroodwarImpl_connect`). The engine side is already there: OpenBW
   hosts the game and speaks the same protocol `scbw` uses today, so this is a
   matter of running the JVM next to it instead of inside it.
2. **Keep Atlantis launcher-agnostic.** Today `Main.main()` expects a ChaosLauncher
   environment (`GAME_LAUNCHER=CHAOS` in `ENV`, `AtlantisRaceConfig`, bwapi.ini
   rewriting, `ProcessHelper.killStarcraftProcess()` calling `taskkill`). Under
   the test runner there is no ChaosLauncher and no StarCraft process to kill, so
   those code paths need a "hosted game" mode - the same switch the deployed
   container build already uses.
3. **Port the tests you care about, not the framework.** `RushDefense`-style
   scenarios map 1:1 onto what you already have as JUnit acceptance tests
   (`tests.acceptance.*`): a fixed map + seed, scripted units, a frame limit and
   assertions at the end. Those are exactly the tests worth keeping as
   integration tests once the bot runs headless - the difference is only who
   hosts the game (OpenBW here, the `scbw` container there).

Until step 1 is done, the two runners cover different ground and both are useful:

| | StardustDevEnvironment | scbw container (`scbw.play`) |
|---|---|---|
| Opponent | Steamhammer / Locutus, C++ | Steamhammer jar, plus anything else on the host |
| Speed | as fast as the CPU allows, in-process | real game loop under Wine |
| Reproducibility | fixed map + fixed seed | map/seed chosen by the harness |
| Our bot | **must be linked C++ today** | any jar (`java -jar`) |
| Artefacts | replay + `.cvis` + log next to it | `~/.scbw/games/GAME_*/` (replay, logs, `result.json`) |

## 5. Practical commands

```bash
cd StardustDevEnvironment
cmake -S . -B build -DCMAKE_BUILD_TYPE=Release
cmake --build build -j$(nproc)

cd build/test
./tests-steamhammer --gtest_list_tests                 # what exists
./tests-steamhammer --gtest_filter=RushDefense.*        # one scripted scenario
./tests-steamhammer --gtest_filter=RushDefense.* --gtest_repeat=5
./tests-steamhammer --gtest_filter=Steamhammer.RunTwenty  # the slow sweep
```

Artefacts land in `build/test/replays/` (`<Suite>_<Test>_<timestamp>_<PASS|FAIL>.rep`,
plus `.cvis` and `.log`). A scenario test that is not reproducible has to set
`randomSeed` explicitly - `randomSeed = -1` (the default) draws a new one on every
run.