# E2E tests on a real engine (idea + ordered plan)

## 1. Goal

Play real games headlessly and assert things about them: we survive scripted
rushes (4pool, 9pool, cannon rush), we play long against real bots
(Steamhammer, UAlbertaBot, Locutus), we never crash. Two rhythms: fast
deterministic scenarios while developing, full-game sweeps before pushes and
nightly. Never a per-commit gate - a full game takes minutes and ladder bots
are not deterministic across versions.

Non-goals: replacing the JUnit suite (it stays the fast feedback loop),
deterministic perfection against learning opponents, any Windows-only
tooling in the loop.

## 2. Where we stand (measured, not assumed)

- `StardustDevEnvironment/` is a CMake C++ harness that already does this for
  C++ bots: OpenBW (headless engine) + BWAPI + GoogleTest + the opponent
  linked in (`test/Steamhammer.cpp`, `test/Locutus.cpp`, `test/RushDefense.cpp`
  with e.g. `RushDefense.Steamhammer9PoolSpeed`). In-process, `setLocalSpeed(0)`
  (as fast as the CPU), fixed map + fixed seed = reproducible, `fork()`s the
  opponent per game, saves replay + CherryVis + log next to the verdict.
  Full map: `DOCS/HOW-TESTS-WORK.md`.
- The same doc states the one blocking fact: the harness plays **linked C++
  `AIModule`s**. Atlantis is `java -jar` and cannot be plugged in as it
  stands; the sketched fix is a spawnable-bot runner variant (JVM next to the
  engine, BWAPI client protocol) instead of linking.
- `sc-docker/` (`scbw` container) plays real games under Wine with **any jar
  today**: `bots/Steamhammer/` (3.6.x, Zerg, AI_MODULE + BWAPI.dll) is already
  there, and past runs left `GAME_*` directories with replays, logs and
  `result.json`. Slow, real game loop, map/seed chosen by the harness - the
  opposite trade-off from Stardust, and working now.
- `scripts/build-bot-jar.sh` builds the deployable fat jar (`--release 8`,
  `java -jar` launch - the same way `scbw` starts a bot). Launch goes through
  `src/main/Main.java` + `GameLauncherFactory` (a launcher Strategy already
  exists: Chaos vs OpenBW), but `Main` still assumes Chaos surroundings
  (`GAME_LAUNCHER=CHAOS`, `AtlantisRaceConfig`, `bwapi.ini` rewriting,
  `ProcessHelper` taskkill).
- `src/starengine/` (~29 files: frame stepper, combat sim in `sc_logic/`,
  Swing canvas debug window, assets) plus its tests
  (`tests/acceptance/starengine/`: `DragoonsVsDragoonsTest`,
  `MoonFormationTest`, `bases/EnemyThirdBaseTest`) plus `isUsingEngine`
  branches in `AbstractWorldCreatingTest`, `FakeOnFrameEnd`, `FakeUnit`.
  Nothing outside `src/starengine` and `src/tests` imports it, but
  `scripts/build-bot-jar.sh` ships the package inside the game jar.
- Tier 0 already runs: `tests.acceptance.e2e` (`ZombieAttacksNearestUnit` +
  `ScenarioCombat` + `FourPoolDefenseTest`) plays Protoss-vs-4pool in the stub
  world with documented harness physics. Measured baseline: cannon falls
  ~150, zealot ~250, nexus ~880, probes never engage (filed as BUGS.md B-19).
  Same forces/timing/assertions carry over to the OpenBW tiers below.
- Stardust platform verified on this machine (2026-10-03): clean CMake build
  (`tests-steamhammer`, `tests-locutus`), `RushDefense.Steamhammer9PoolSpeed`
  runs a 2600-frame game in under 2 s wall time with replay+log artefacts.
  The DemoAI loses to the 9pool (nexus down 1:24) - the orchestration is
  proven, only the spawnable-Atlantis piece is missing.

## 3. Sources

- https://github.com/OpenBW/openbw - the headless engine (game dynamics,
  `data_loading.h` for the data layout, `replay.h` for replay playback).
  Build/use instructions live at https://github.com/OpenBW/bwapi.
- https://github.com/bwapi/bwapi - reference values and protocol behaviour;
  see `_AI/CONVENTIONS.md` §9 for what each source is authoritative for.
- https://github.com/JavaBWAPI/JBWAPI - the binding Atlantis plays games with.
- `DOCS/HOW-TESTS-WORK.md` - the local map of the Stardust runner. Read it
  before touching anything in `StardustDevEnvironment/`.

## 4. What an "E2E test" is here (four kinds, not one)

1. **Scenario tests** (`RushDefense`-style): fixed map + fixed seed, scripted
   or strategy-pinned opponent (Steamhammer 9PoolSpeed, a 4pool script),
   frame limit, assertions at the end. Deterministic, seconds-to-minutes.
   This is the rush-defense answer. The opponent slot (`opponentModule`) fits
   anything from a full bot down to a ~30-line suicide rusher (onFrame: every
   combat unit attacks the nearest enemy) - the rusher is the *best* 4pool
   oracle because its timing is exact, while a ladder bot "sometimes rushes".
   Start with exactly two scenarios: 4pool and 9pool defense.
2. **Sweeps** (`RunTwenty`-style): N full games on random maps/seeds vs a real
   bot, counting wins. A regression/balance signal, not a scenario. Slow;
   run before pushes and nightly.
3. **Replay-driven tests**: recorded replays played back frame by frame
   (OpenBW supports it), asserting bot decisions at key frames - e.g. "pool
   scouted by frame X", "forge started by frame Y" in a game we once lost to
   4pool. Deterministic, no opponent needed, cheap. The highest value per
   minute of any kind here.
4. **Parity + determinism self-checks**: same seed twice must give the same
   verdict (validates the harness, not the bot); same scenario under OpenBW
   vs `scbw`/Wine occasionally (validates the engine - OpenBW is a
   reimplementation, trust it but verify).

## 5. The opponent question (answered by design, not by fallback)

OpenBW is an engine, not a game copy: there are no Blizzard melee AI scripts
inside it, so "single player vs native AI" is not an option and should stop
being asked. Bot-vs-bot is the design:

- For **rush defense**, a scripted deterministic rusher beats any ladder bot:
  a 4pool at fixed timing on a fixed map is reproducible; Steamhammer "sometimes
  4pools" is not an assertion, it is a hope. Ladder bots (Steamhammer,
  UAlbertaBot, Locutus) are for sweeps, where variance is the point.
- UAlbertaBot is Java: how Stardust links it as C++ (`UAlbertaBot::UAlbertaBotModule`
  in the `RushDefense` example) must be verified in `StardustDevEnvironment/test/`
  before promising it as an opponent - check first, assume nothing.
- Assertions should be graceful, not just win/lose: survival frames, workers
  lost by frame N, defense up by frame N, scout timing, crash-free (exit code,
  no exception in log). "Played long against X" is a legitimate verdict.

## 6. Ordered plan

### Stage 0 — decision (human, one line)

Do scbw-container E2E first (works today, unblocks the rush signal in days)
and OpenBW in-process second (fast and deterministic, needs C++ work, the
real platform). Keep both afterwards - different trade-offs, both useful
(see the comparison table in `DOCS/HOW-TESTS-WORK.md` §4).

### Stage 1 — scbw E2E script (days, no C++, no engine changes)

New `scripts/run-e2e.sh <bot-jar> <opponent> <maps>`: build via
`scripts/build-bot-jar.sh`, play fixed scenarios (Atlantis as Protoss vs
Zerg rush on Python-like maps; Atlantis as Terran likewise), frame limit,
then parse `~/.scbw/games/GAME_*/result.json` + logs into a verdict table
(win/loss, units killed/lost, survival frames, crash?). Start with the two
defense scenarios from §4 (4pool, 9pool). Record the first table as the
baseline in `_AI/` (numbers, date, versions). Manual/periodic rhythm. Done
when the same command reproduces the same table twice. The script also
enforces replay retention (last 10 per suite, §7).

### Stage 2 — Atlantis hosted-game mode (the enabler for everything OpenBW)

`Main.main()` must survive without Chaos: no `GAME_LAUNCHER=CHAOS`
assumption, no `bwapi.ini` rewriting, no `taskkill` when hosted (extend the
existing `GameLauncherFactory` switch - the deployed container build already
has one). Done when the unchanged fat jar plays a game started by someone
else (scbw first, Stardust runner later). No behaviour change in
Chaos-launched games - verify with one scbw game before/after.

### Stage 3 — spawnable-bot runner in Stardust + ported scenarios (weeks)

Per `DOCS/HOW-TESTS-WORK.md` §4: a `BWTest` variant (or new target, e.g.
`tests-atlantis`) that spawns `java -jar Atlantis.jar` and connects it via
the BWAPI client protocol instead of linking. Then port the scenarios worth
keeping - 4pool/9pool/cannon-rush defense with survival assertions, not
`expectWin` - one suite, fixed map+seed each. Done when
`RushDefense.*`-equivalent runs green for Atlantis and a deliberately
broken bot (e.g. previous release) fails it.

### Stage 4 — sweeps, replays, determinism

- `RunTwenty` equivalent vs Steamhammer (and Locutus/UAlbertaBot once the
  UAlbertaBot question in §5 is answered): wins count, recorded per push.
- Replay-driven tests for famous losses (needs a replay corpus - start with
  our own `GAME_*` replays).
- Same-seed-twice determinism check of the runner itself.
- Occasional OpenBW-vs-scbw parity game.

### Stage 5 — StarEngine removal (only after Stage 3 steps for its tests)

StarEngine results do not resemble the game closely enough to assert on, so
it goes - but its *tests* partly test Atlantis logic (formations), not the
engine, and those survive by moving to the OpenBW stepper:
1. Port `MoonFormationTest`, `DragoonsVsDragoonsTest`, `EnemyThirdBaseTest`
   to the new stepper (or to stub-world where they do not need resolution).
2. Delete `src/starengine/` (including the Swing canvas - replays + CherryVis
   already cover debugging, see `DOCS/HOW-TESTS-WORK.md` §2.7).
3. Remove the `isUsingEngine` branches (`AbstractWorldCreatingTest`,
   `FakeOnFrameEnd`, `FakeUnit`) and stop shipping the package in
   `scripts/build-bot-jar.sh`.
4. Full suite + ArchUnit green with no starengine references left
   (`grep -r starengine src/` empty).

### Stage 6 — cadence (write it down when Stage 1 lands)

Scenario suite on demand while developing; sweep before pushes / nightly.
Never gate commits on full games. Who runs what, where, is decided here -
not in this idea doc.

## 7. Metrics / assertion catalog (for scenario and sweep tests)

Win/loss; units killed and units lost totals per side; survival frames vs
frame limit; workers lost by frame N; static defense completed by frame N;
scout arrival frame; first enemy tech seen; crash-free (process exit code,
zero exceptions in `bot.log`); replay + log artefacts next to every verdict
(copy the Stardust convention: `<Suite>_<Test>_<timestamp>_<PASS|FAIL>`,
retention: last 10 replays per suite, older deleted by the runner script -
replays are already saved by default, the policy is the only new part).

## 8. Open questions (verify-first, not assume-first)

- How UAlbertaBot is linked into Stardust (`test/CMakeLists.txt`,
  `3rdparty/`) - Java bot as C++ module needs an adapter somewhere.
- Which maps form the fixed pool (start: whatever Stardust's `Maps::Get`
  already serves, e.g. Python + AIIDE set).
- Frame limits and wall-clock budgets per scenario (measure, do not guess).
- Whether `scbw` result verdicts distinguish crash/timeout/loss richly enough
  for the table, or the script must parse logs too.

## 9. What NOT to do

- No per-commit full-game gates; no "must beat Steamhammer X-Y" as a merge
  rule (opponent versions drift, maps drift).
- No new engine of our own, no fixing StarEngine's combat sim to "be more
  like the game" - that road ends where it started. Replace, do not repair.
- No OpenBW work before Stage 1 exists: a slow real signal today beats a
  fast perfect harness next quarter.
