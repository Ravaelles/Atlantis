# E2E tests on a real engine

Status 2026-10-18: **the OpenBW loop works and is bounded (150 s wall, 20
game minutes); the bot produces** (Pylons, Gateways, Core in rosters since
2026-10-13; script-clean openings since 2026-10-17). Placement builds since
the 2026-10-11 library patch. **Current blocker for any scenario with an
opponent: none spawns** (NEXT #94) — melee games arrive enemyless, so
kill-trade assertions cannot pass and the survival scenario waits on the
same item. What runs green today: opening/macro scenarios against no
effective opposition (buildings, expansion, crash-free, frames advanced).

The old stage numbering is gone; this file uses §-sections. Where another
file says "Stage 1" (the scbw tier), "Stage 3" (the OpenBW runner) or "Stage 5"
(StarEngine removal), read: scbw tier = §2.2, OpenBW runner = §2.1, StarEngine
removal = §2.4 (deleted).

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
- **The bot ends its own game, on either limit.**
  `FORCE_END_GAME_AFTER_REAL_SECONDS` (wall clock) and
  `FORCE_END_GAME_AFTER_INGAME_SECONDS` (game clock) both end it cleanly while
  the host is alive. CONVENTIONS §17 fixes these at 120 s and 20 game minutes and
  the script refuses a wider value. Verified: `bot exit code: 0` (was 143/SIGTERM),
  and a 7-game-minute run now finishes itself in ~5 s of wall clock (measured
  2026-10-09).
- **`BWAPI_DATA_PATH` and `AI/build_orders` resolved from the directory the jar
  runs in**, or the bot plays with no production at all and the log says nothing.

### 2.2 The scbw tier has run, and produced real verdicts

`scripts/run-e2e.sh --parse-only` reads `~/.scbw/games/GAME_*/result.json`
(read-only, CONVENTIONS §8) into `_AI/e2e/<date>.md`. One real table existed
(`_AI/e2e/scbw-2026-10-04_165556.md`, deleted by the owner in `e657b83f`;
nine games, Atlantis as Protoss vs Steamhammer and Marine Hell, **two of them
crashed** (`is_crashed` plus a traceback in the bot log), the rest decided or
timed out) - no table is kept in the repo today.

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
worth it. Its three tests were assertion-free smoke runs, so what was lost is the
option of asserting on a simulated fight, not coverage.

---

## 3. What stands between us and the OpenBW mega-test

Four things. The first is the real blocker; the rest are smaller but each one
makes a run report the wrong thing.

### 3.1 Placement on OpenBW (was the blocker - closed)

The rewritten planner places Pylons on OpenBW (`Can't find place` zero);
the dispatch loop behind it closed as NEXT #48. Gaps live in one place:
`_AI/redesign/03_PLACEMENT.md` ("NOT FINISHED" at the top - Terran/Zerg
stubs, wall gap unmeasured). Trap kept: an ENV key only works if
`Env.applyKeyAndValueToFlag` has a case for it (the `PLACEMENT` flag was
once read from `System.getenv` and silently ignored).

### 3.2 `JBWEB` does not exist on OpenBW

JNI natives absent on Linux; the placement path must never depend on it
(`_AI/redesign/04_MAPLIB.md` tracks the own-code replacements).

### 3.3 The first assertion layer exists (2026-10-09)

`scripts/run-openbw-e2e.sh` now takes optional expectations and exits non-zero
when one is not met: `EXPECT_MIN_INGAME_SECONDS`, `EXPECT_MIN_KILLED`,
`EXPECT_MAX_KILLED`, `EXPECT_MIN_RESOURCE_BALANCE`. They read the facts
`GameSummary` already prints, so no scenario file is needed - see
`PLAN-OPENBW.md` for the table and an example.

What is still missing from a real harness: a scenario *file* (one place per
scenario instead of env vars), and a deliberately broken bot that must fail the
same scenario. The assertions have been shown to fail correctly on a real run
(`EXPECT_MIN_KILLED=12` against a bot that killed 0), which is half of that
proof.

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

1. ~~Try the new planner in a real game.~~ **Done** - a Pylon is placed with
   `PLACEMENT=catalogue`, but the current runs still log
   `Can't find place for Pylon` (measured 2026-10-09). See the blocker below.
2. ~~Add the first assertion layer.~~ **Partly done (2026-10-09)** - the runner
   takes expectation env vars and exits non-zero when one is not met
   (§3.3). Broken-build half DONE 2026-10-13 (grid forced to refuse:
   Pylon=0 everything, SCENARIO FAILED; reverted same turn and re-verified
   PASS). Still missing: a scenario file.
3. ~~Fix the Pylon, then the mega-test.~~ **Done 2026-10-13** - Pylon and Gateway
   placed and built in every recent run (Pylon=5-6, Gateway=3, Core+Dragoons+gas
   when they pass), green many in a row (7/9 PASS, both misses honest timing
   gates), red against broken proven both halves (absurd-99-Gateways fails, a
   deliberately broken grid build SCENARIO FAILED, reverted same turn and
   re-verified PASS). What remains of the mega-test is step 4 (survival
   numbers), not placement.
4. **Port the 7-minute survival scenario** with a near-even trade:
   `EXPECT_MIN_INGAME_SECONDS=420`, `EXPECT_MIN_KILLED=12`,
   `EXPECT_MAX_KILLED=40`, `EXPECT_MIN_RESOURCE_BALANCE=-200`. Blocked on
   NEXT #94: melee games spawn no enemy, so kill-trade assertions cannot
   pass however clean the opening plays. Unblocks the day the enemy
   spawns — the assertions are ready.
5. **Only then** the sweeps, replay playback and the OpenBW-vs-scbw parity game.

**The blocker (2026-10-18):** the enemy never spawns (NEXT #94), so no
scenario with kill-trade assertions can pass. Placement is closed as a
blocker (library patch + last-resort ladder); the runner is bounded
(CONVENTIONS §17) and asserts.

What NOT to do first: no new opponent work, no determinism sweeps, and no more
placement refactoring until a Pylon actually lands in a run.

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

Done (placement + assertion layer + broken-build proof, all measured) -
what remains of the mega-test is step 4 in §3.5 (survival numbers), not
placement.

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
`bot.log`); replay + log artefacts next to every verdict. Per-building
warp rows with actual vs build-order supply: `BUILDINGS.json` (ledger in
`atlantis.game.util`, collected next to the logs by both runners).
High-volume diagnostics (worker gates, stuck workers, board opinions)
additionally land as JSON lines in `EVENTS.jsonl` (`atlantis.util.log`,
one row per event under a `LogKey`, queried with
`scripts/query-events.py`) - the console lines stay as the human tail.

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
