# E2E tests on a real engine

Status: **the OpenBW loop works; the mega-test does not, and §3 says exactly why.**
Rewritten 2026-10-08 (the previous version still described the client as unable to
attach, which stopped being true that day).

**On the old stage numbers:** this version replaces the Stage 0-6 list with
Steps A-E, because Stages 0-3 were almost entirely about getting a Java client to
attach to OpenBW - which is done. Other files still say "Stage 1" (the scbw tier),
"Stage 3" (the OpenBW runner) and "Stage 5" (StarEngine removal); read those as:
scbw tier = §2.2, OpenBW runner = §2.1 + Steps A-B, StarEngine removal = §2.4.

---

## 1. Goal

Play real games headlessly and assert things about them: we survive scripted
rushes (4pool, 9pool, cannon rush), we play long against real bots
(Steamhammer, UAlbertaBot, Locutus), we never crash.

The end state is **one mega-test**: a single OpenBW game run that exercises most
of the bot at once - economy, production, combat, map analysis, base and choke
data - and asserts the basics (buildings queued, expansion up, no exception,
frames advanced). One such run is worth more than dozens of unit tests, because
it proves the whole stack together on real engine physics
(`_AI/CONVENTIONS.md` §10).

Two rhythms, never one: fast deterministic scenarios while developing
(seconds-to-minutes), full-game sweeps before pushes and nightly. **Never a
per-commit gate** - a full game takes minutes, and ladder bots are not
deterministic across versions.

**Non-goals:** replacing the JUnit suite (it stays the fast feedback loop),
deterministic perfection against learning opponents, any Windows-only tooling in
the loop, and any new engine of our own.

---

## 2. Where we stand (measured, not assumed)

### 2.1 The OpenBW loop is closed

`scripts/run-openbw-e2e.sh` runs the whole lifecycle in one command - host,
attach, play, tear down, verdict - and the client attaches. Measured 2026-10-08
on `maps/cog/(3)TauCross1.1.scx`:

```
Connection successful
### mapFileName = (3)TauCross1.1.scx
Analyzing map... Use build order: `Zealot into Goon`
MISSION @0:15 TO Sparta: TooFewZealots - Focus{name='MainChoke', ...}
HELLO_ATLANTIS - BWAPI attached, Atlantis is playing!
```

What that requires, all of it verified:

- **`BWAPI_CONFIG_CONFIG__SHARED_MEMORY=ON` on the host.** This is the one
  setting that made the client attach; without it the host serves a socket but
  publishes no game registry and the client loops on "No server proc ID". Set by
  the script and by `OpenBWHost`. Root cause and the two wrong conclusions it
  corrects: `_AI/CHALLENGES/OpenBW.md`.
- **The bot ends its own game.** `FORCE_END_GAME_AFTER_REAL_SECONDS` (default
  600 s) must sit *below* the host's timeout, or the host dies first, the client
  is orphaned and the run reports failure after playing. The script sets three
  nested timeouts (game < bot kill < host kill) and refuses a configuration
  whose host timeout would exceed the 360 s cap (CONVENTIONS §13). Verified:
  `bot exit code: 0` (was 143/SIGTERM).
- **`BWAPI_DATA_PATH` and `AI/build_orders` resolved from the directory the jar
  runs in**, or the bot plays with no production at all and the log says nothing.

### 2.2 The scbw tier has run, and produced real verdicts

`scripts/run-e2e.sh --parse-only` reads `~/.scbw/games/GAME_*/result.json`
(read-only, CONVENTIONS §8) into `_AI/e2e/<date>.md`. One real table exists
(`_AI/e2e/scbw-2026-10-04_165556.md`), and it is a signal with teeth: nine games,
Atlantis as Protoss vs Steamhammer and Marine Hell, **two of them crashed**
(`is_crashed` plus a traceback in the bot log), the rest decided or timed out.

This tier is slow and needs the licensed game in a container, so it is
owner-run. It is kept because it is the only place real StarCraft physics and a
real ladder opponent appear.

### 2.3 The stub-world tier runs in the fast loop

`tests.e2e` plays scripted rush scenarios with harness physics. Both are green
with baselines pinned (`FourPoolDefenseTest`, `NinePoolDefenseTest`), and they
are the B-19 regression net. Harness physics (units move in straight lines,
damage per tick) is enough for defence mechanisms and **not** enough for "does
this micro win the fight" - that is what OpenBW is for.

### 2.4 `src/starengine/` is gone

Removed 2026-10-04: OpenBW replaces it, and repairing a second engine is not
worth it. All three of its tests were assertion-free smoke runs, so what was
lost is the *option* of asserting on a simulated fight, not coverage. The game
jar stopped shipping the test harness in the same commit.

---

## 3. What stands between us and the OpenBW mega-test

Four things. The first is the real blocker; the rest are smaller but each one
makes a run report the wrong thing.

### 3.1 Placement on OpenBW (the blocker - the rewrite has landed, not yet cut over)

The bot could not place its first Pylon on OpenBW, every run at the same frame:

```
0:39: Can't find place for `Pylon`, At 8 Pylon (READY_TO_PRODUCE)(#1)
(reason: Can't physically build here)
(Max search distance was: 36) near null
```

**Status (2026-10-08):** the placement subsystem has been **rewritten** -
`atlantis.placement` implements the Stardust-style catalogued planner
(`_AI/redesign/03_PLACEMENT.md`, S1-S6 for Protoss, 28 acceptance tests, and it is
wired into Production V2 through the cannon-fortification policy). What has **not**
happened is the **cut-over**: `APositionFinder` is still the default, and
`PLACEMENT=catalogue` has not been tried in a real game.

So the blocker is now a switch and a run, not a rewrite:

1. run the E2E with `PLACEMENT=catalogue` in the bot's ENV and see whether a
   Pylon is placed (this is the first thing to do, and it is the whole point of
   the rewrite);
2. if it places, cut over (make the catalogue the default) and delete
   `APositionFinder` with the Producer cut-over;
3. if it does not, the remaining gap is named in `03_PLACEMENT.md`'s
   "NOT FINISHED" block at the very top of that file.

### 3.1a What the rewrite does not cover yet (from that block)

- **Terran/Zerg placement** are stubs (addon availability, lift/land, creep).
- **The wall does not measure its gap** - a wall with a one-tile hole is accepted.
- The **Pylon pull-forward is coarse** (asks for a Pylon, no `shiftOne`/buffer).
- **`distanceToExit` is Chebyshev**, not a path.
- Expansion and DT-detection policy (C14/C15) are unplaced.

### 3.2 `JBWEB` does not exist on OpenBW

`JBWEB` is a JNI library; its natives are not on Linux, `InitJBWEB.init()` fails
and `AMap` catches it and continues. So ground-distance and the placement grid
that production reads on the Wine path are simply absent on OpenBW, and any
predicate that consults them answers nonsense there. Whatever the rewrite does,
it must not depend on a library that cannot load on the engine the tests run on.

### 3.3 No assertion framework on top of the run yet

`scripts/run-openbw-e2e.sh` reports a *verdict* (attached, frames, exit code,
log paths), not assertions. There is no scenario file, no "assert this by frame
N", and no broken-bot check (the test that proves the test can fail). The scbw
tier has a parse-and-table layer; OpenBW has none.

### 3.4 The opponent is still an open question

OpenBW is an engine, not a game copy - there are no Blizzard melee AI scripts in
it, so "single player vs native AI" is not available and should stop being asked.
Options, in the order worth trying:

- **A scripted deterministic rusher** - a bot whose every combat unit attacks the
  nearest enemy. It is the *best* 4pool/9pool oracle because its timing is exact,
  where a ladder bot "sometimes rushes", which is a hope and not an assertion.
- **Steamhammer / Locutus / UAlbertaBot** as opponents for sweeps. Stardust links
  Steamhammer and Locutus as C++ modules for its own tests; UAlbertaBot is Java
  and how it is linked must be verified in `StardustDevEnvironment/test/` before
  it is promised.

For the mega-test this does not block the first milestone: a game against a
scripted rusher on a fixed map is enough to assert "the whole bot ran and did not
crash".

### 3.5 Next steps, in order (the answer to "what now?")

Each step is small and ends with something a command proves.

1. **Try the new planner in a real game.** Run the E2E with `PLACEMENT=catalogue`
   in `bots/AtlantisOpenBW/AI/ENV`. Verification: a Pylon appears in `bot.log`
   instead of `Can't find place for Pylon`. This is the single most valuable run
   available, and it is a one-variable change.
2. **Add the first assertion layer.** `scripts/run-openbw-e2e.sh` should accept a
   scenario file (map, race, frame limit, expected facts) and exit non-zero when
   an expected fact is missing. Verification: a scenario passes, and a
   deliberately broken build fails the same scenario.
3. **Write the mega-test** against that layer: one game, fixed map, scripted
   rusher, asserting workers mined, a Pylon and a Gateway placed and built, the
   production queue advanced, no exception, frames advanced. Verification: green
   twice in a row, red against a known-broken build.
4. **Port one rush scenario** (4pool) with survival assertions, not `expectWin`.
   The stub-world twin in `tests.e2e` is the behaviour to reproduce.
5. **Only then** the sweeps, replay playback and the OpenBW-vs-scbw parity game.

Steps 1-2 are the whole of the next session's value; 3-5 are a tier, not a task.

What NOT to do first: no new opponent work, no determinism sweeps, and no more
placement refactoring - the planner needs a run before it needs anything else.

---

## 4. What an "E2E test" is here (four kinds, not one)

1. **Scenario tests**: fixed map + fixed seed, scripted or strategy-pinned
   opponent, frame limit, assertions at the end. Deterministic,
   seconds-to-minutes. Start with 4pool and 9pool defence.
2. **Sweeps**: N full games on random maps/seeds against a real bot, counting
   wins. A regression/balance signal, not a scenario. Slow; before pushes and
   nightly.
3. **Replay-driven tests**: recorded replays played back frame by frame (OpenBW
   supports it), asserting bot decisions at key frames - "pool scouted by frame
   X", "forge started by frame Y" in a game we once lost to a 4pool.
   Deterministic, no opponent needed, cheap. The highest value per minute of any
   kind here.
4. **Parity + determinism self-checks**: the same seed twice must give the same
   verdict (validates the runner, not the bot); the same scenario under OpenBW
   and under `scbw`/Wine occasionally (validates the engine - OpenBW is a
   reimplementation, trust it but verify).

---

## 5. The plan from here

Ordered, each step with a verification. This replaces the old Stage 0-6 list,
which was mostly about getting a client to attach - that is done.

### Step A - finish the OpenBW run as a *reliable* tool

Two smaller items, both measured, both ours:

- the first Pylon placement (3.1) - blocked on the `PositionFinder` rewrite;
- assertions on top of the run (3.3): a scenario file, an expected-outcome
  check, and a deliberately broken bot that must fail it.

**Done when:** a scenario runs green on OpenBW and a broken bot fails it.

### Step B - the mega-test (the goal of this document)

One OpenBW game, fixed map, fixed seed, scripted rusher opponent, bounded
frames, asserting at the end: workers mined, a Pylon and a Gateway were placed
and built, the production queue advanced, an expansion was attempted, no
exception in the log, frames advanced past N. It fits the fast-loop budget or it
becomes an owner-only scope (CONVENTIONS §10, §12).

**Done when:** the mega-test runs green, is stable across two consecutive runs,
and fails against a known-broken build.

### Step C - port the rush scenarios

4pool and 9pool defence with survival assertions (not `expectWin`), as one suite
with a fixed map and seed each. The stub-world pair in `tests.e2e` is the
behaviour to reproduce; the difference is the physics underneath.

### Step D - sweeps, replays, parity

- Sweep vs Steamhammer (and Locutus; UAlbertaBot once its linkage is verified),
  wins recorded per push.
- Replay-driven tests for famous losses (start with our own `GAME_*` replays).
- Same-seed-twice determinism check of the runner.
- Occasional OpenBW-vs-scbw parity game.

### Step E - cadence

Scenario suite on demand while developing; sweep before pushes / nightly. Never
gate commits on full games. Who runs what, where, is decided here - not in this
idea doc.

---

## 6. Metrics / assertion catalogue

Win/loss; units killed and lost per side; survival frames vs frame limit;
workers lost by frame N; static defence completed by frame N; first Pylon and
first Gateway placed by frame N (the mega-test's core); scout arrival frame;
first enemy tech seen; crash-free (process exit code, zero exceptions in
`bot.log`); replay + log artefacts next to every verdict.

Artefact convention follows Stardust: `<Suite>_<Test>_<timestamp>_<PASS|FAIL>`,
retention "last 10 replays per suite", older deleted by the runner script. The
scbw tier's tables live in `_AI/e2e/` and are governed by its own README.

---

## 7. Sources

- https://github.com/OpenBW/openbw - the headless engine (dynamics,
  `data_loading.h`, `replay.h`). Build/use: https://github.com/OpenBW/bwapi.
- https://github.com/bwapi/bwapi - reference values and protocol behaviour;
  `_AI/CONVENTIONS.md` §9 says what each source is authoritative for.
- https://github.com/JavaBWAPI/JBWAPI - the binding Atlantis plays with.
- `DOCS/HOW-TESTS-WORK.md` - the Stardust runner, read before touching
  `StardustDevEnvironment/`.
- `_AI/PLAN-OPENBW.md`, `_AI/CHALLENGES/OpenBW.md` - how the attach was solved.
- `_AI/POSITION-FINDER.md` - why placement fails, and the plan to rewrite it.

---

## 8. Open questions (verify first, not assume)

- How UAlbertaBot is linked into Stardust (`test/CMakeLists.txt`, `3rdparty/`).
- Which maps form the fixed pool (start with whatever Stardust's `Maps::Get`
  serves, plus the owner's `(3)TauCross1.1`).
- Frame limits and wall-clock budgets per scenario - measure, do not guess.
- Whether the stub-world scenario assertions transfer unchanged to OpenBW
  physics, or need re-baselining (they were calibrated on harness physics).

---

## 9. What NOT to do

- No per-commit full-game gates; no "must beat Steamhammer X-Y" as a merge rule
  (opponent versions drift, maps drift).
- No new engine of our own, and no repairing `StarEngine`'s combat sim to "be
  more like the game" - that road ends where it started. Replace, do not repair.
- No patching `PositionFinder` further: it is scheduled for deletion and rewrite
  (`_AI/POSITION-FINDER.md`). Adding to it makes the rewrite bigger and the
  diagnosis muddier.
- No model-run Wine/StarCraft: OpenBW is the E2E engine (CONVENTIONS §14).
