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
- ~~`src/starengine/` (frame stepper, combat sim in `sc_logic/`, Swing canvas
  debug window, assets) plus its tests and the `isUsingEngine` branches~~ —
  **removed 2026-10-04** (the owner's ruling: OpenBW replaces it, so repairing a
  second engine is not worth it). See Stage 5 for what went with it and what was
  measured; the game jar stopped shipping the test harness in the same commit,
  which was the reason to do it now rather than after OpenBW lands.
- Tier 0 already runs: `tests.e2e` (`ZombieAttacksNearestUnit` +
  `ScenarioCombat`) plays the two scripted rush scenarios in the stub world
  with documented harness physics, both green with their baselines pinned.
  Baselines re-measured from a **clean** run on 2026-10-03 after the B-19 fix -
  the first versions came from an instrumented run, which is not a baseline
  (NOTES.md, "Scenario E2E probes are actuators, not sensors"):
  - `FourPoolDefenseTest` (6 lings from x=26): **the defence holds**. The lings
    die between frames 51 and 249, the cannon survives on 10 hit points after 11
    strike rounds, the zealot never takes a hit, the nexus never loses one of its
    1500 + 750, and each of the four probes lands exactly one strike - the
    mechanism B-19 was about, which used to be zero strikes.
  - `NinePoolDefenseTest` (8 lings from x=32, the 9pool twin): **the base still
    falls**, and that is pinned on purpose - this scenario is about the mechanism.
    The cannon trades three lings before falling at frame 192 (was ~163) and the
    probes land six strikes between them (was none). The probes all die at the
    end, which the assertions deliberately do not pin.
  - The world harness now drops dead units from the unit lists, the way the
    engine does. Without it every corpse stayed selectable for the rest of the
    scenario, which is where "Probe AttackUnit got target.hp = 0" came from.
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

### Stage 1 — scbw E2E script — **script written 2026-10-04, first real game pending**

`scripts/run-e2e.sh` exists, split into the half that needs the owner's container
(`--bot-jar`, runs `scbw run`) and the half this repository can verify on its own
(`--parse-only`, reads `GAME_*/result.json` + logs into `_AI/e2e/*.md`). Two
things came out of writing it rather than guessing at it:

  - **The `result.json` schema is unverified.** This repo has never read one, and
    CONVENTIONS §9 (never state a game fact from memory) covers schemas as much as
    numbers. So the parser recognises a handful of key spellings, prints every key
    it saw next to each game, and writes `schema-unknown` for anything else instead
    of inventing a verdict. The first real run rewrites those key lists.
  - **The parse half has a test; the play half cannot have one here.**
    `--self-test` builds four synthetic games — a win, a loss, an exception in the
    log, and an unknown schema — and asserts the table it gets back. scbw itself
    needs the licensed game plus a container (`~/.scbw/docker/game.dockerfile`), so
    the play half stops with an explanation and exit code 2 rather than pretending.

CONVENTIONS §8 was amended (2026-10-04) to allow reading `~/.scbw/games`
read-only for this tier, writing nothing outside the workspace.

Still to do, in order: run one real game with the jar this repo builds, paste the
first table into `_AI/e2e/` as the baseline (numbers, date, versions), fix the
schema if it differs, then the fixed scenario pairs from §4 (4pool, 9pool) and the
retention rule. "Done" is unchanged: the same command reproduces the same table
twice.

The plan it implements:

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

### Stage 5 — StarEngine removal — **done 2026-10-04, ahead of Stage 3**

StarEngine results do not resemble the game closely enough to assert on, so it
goes. The plan was to port its three tests to the OpenBW stepper first; the
measurement said there was nothing to port:

| what the three tests did | |
|---|---|
| `MoonFormationTest.moonShape` | printed positions and issued `MOVE_FORMATION` - no assertion |
| `DragoonsVsDragoonsTest.goonsUseReasonableManagers` | ran the commander and printed one unit's action - no assertion |
| `bases/EnemyThirdBaseTest` | a base-building scenario, again printing, not asserting |
| `useStarEngine()` | commented out in all three, so they were already running in the stub world |

So all three were smoke runs with zero assertions, and what was actually lost is
the *option* of asserting on a simulated fight until OpenBW is here. That is a
smaller loss than it looked, which is why the removal did not wait for Stage 3.

What went in the commit:
1. Deleted `src/starengine/` (26 files, 1209 lines, including the Swing canvas -
   replays + CherryVis already cover debugging, see `DOCS/HOW-TESTS-WORK.md` §2.7)
   and `tests/starengine/` (the three assertion-free smoke runs above).
2. Removed the `isUsingEngine` branches (`AbstractWorldCreatingTest`,
   `FakeOnFrameEnd`, `FakeUnit`) and `Env.markUsingStarEngine`/`isStarEngine`,
   whose only writer was the launcher and whose only reader was a commented-out
   line in `Select`.
3. `FakeUnit`'s two StarEngine enums (`EngineUnitState`, `AttackState`) became
   plain flags: nothing but the simulator ever set them, so every stub world saw
   `false` and still does.
4. `scripts/build-bot-jar.sh` now drops `tests/**` and `starengine/**` from the
   payload and *fails the build* if any packaged class still names `tests/` in
   its constant pool. Measured on the same machine: 3755 entries before, 3610
   after - 119 harness classes and 26 simulator classes that the game never
   needed - and the production-side scan found 0 references, which is why the
   drop is safe rather than hopeful.
5. Full suite + ArchUnit green with no `starengine` reference left in `src/`
   except one sentence in `FakeUnit` explaining what the flags used to be.

**Still open, and it is the reason the OpenBW tiers matter:** nothing can assert
on a *simulated* fight yet. `tests.e2e` plays scripted scenarios with harness
physics (units teleport-straight-line, damage per tick), which is enough for
defence mechanisms like B-19 and not enough for "does this micro win the fight".
Stage 3 is where that comes from.

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
