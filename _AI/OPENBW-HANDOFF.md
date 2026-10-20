# OpenBW handoff — independent engine-track tasks (NEXT #166)

Owner direction 2026-10-20: this model leads Atlantis logic; the other
model works this file only, independently, one task at a time. Each task
names its files, its test gate, and its evidence. Nothing here needs
Atlantis game code, and no task blocks another.

## Ground rules (non-negotiable)

- Single source of truth: `/sc-ai/OpenBW/openbw` (`feature/support-ums-maps`).
  The harness copy `StardustDevEnvironment/3rdparty/openbw` is a build
  artifact. Engine changes land via `scripts/build-openbw.sh --install`
  (with backups), never by blind rsync or plain `cp`.
- Their uncommitted harness work (`Server.cpp/h`, `GameUnits.cpp`,
  `BWData.cpp`, `bwgame.h`, `run-openbw-server.sh`) stays theirs until
  the done-signal below. Do not touch `src/main/Main.java`, `docs/`,
  or Atlantis bot code from this track.
- Real engine data only (CONVENTIONS §9): numbers come from the vendored
  binding or `bwapi` unit/weapon tests, never from memory.
- No verbatim StarEngine restore (revert `b60a7029` stands). No AIScript
  interpreter here (todo A1.2b is its own project, owner call).

## Baselines (re-verify before and after every task)

- `bash scripts/test-openbw-host.sh` → `BYTE_2_OK` + `MATCH_TICKING` +
  `MatchStart=YES` + `players=N/12`.
- `UMS_TEST_DATA_DIR=/tmp/scdata tests/run.sh --default-maps` → `ALL PASSED`
  (the `/tmp/scdata` MPQ symlinks fix case-sensitive names).

## Tasks (any order, one at a time)

- **T1 — A3.3 revealers filter.** `unit_counts` still counts
  `Special_Map_Revealer` helpers in BWAPI's participating-player test
  (slot 1 reads 137 = 14 warriors + 123 revealers). Decide: filter
  revealers out of that test, or keep and document. Files: slot report
  + participating test in `3rdparty/openbw` (harness tree). Gate: extend
  `tests/engine_behaviour_test.cpp` to pin the decision
  (warriors vs revealers asserted separately). Evidence: `run.sh` green
  both ways documented in `docs/openbw-todo.md` A3.3.
- **T2 — B2 reconnect test.** After the first client disconnects,
  `close(syncSocket)` leaves the listener closed, so a reconnect is
  expected to fail. Decide whether that is intended, then pin it.
  Files: `Server.cpp/h` + `scripts/test-openbw-host.sh` (or a second
  probe mode). Gate: the new test fails if the behavior ever changes
  silently. Evidence: host-test output pasted in the done-signal.
- **T3 — E3 slots extraction.** `BWData.cpp` is one ~1800-line unit
  (map loading, slot reporting, setup, frame pump). Extract the slot
  reporting into its own small leaf unit, no behavior change. Gate:
  `run.sh` green + clean build. Evidence: diff stat + test output.
- **T4 — A3.1 map-failure survey.** List which UMS maps `error()` out on
  `fixme rescue passive` / `fixme unknown flag`, with the exact error
  line per map. No fix, survey only. Files: engine logs only.
  Evidence: table in `docs/openbw-todo.md` A3.1 (their file, their edit).
- **T5 — B3 real-CHK fixture.** Build a map fixture from a small real
  `scenario.chk` with one field flipped (the synthetic-CHK attempt died
  on `place_completed_unit failed`, abandoned by time-box — do not retry
  synthesis). Gate: `engine_behaviour_test` loads it and asserts the
  flipped field. Evidence: `run.sh` green with the fixture.
- **T6 — C1/E2 script split (do once, it is the same split).**
  `scripts/run-openbw-ums.sh` (242 lines) mixes preflight, host start,
  publish wait, bot run, parsing, verdict. Split into `start-host.sh`
  (launcher + live-game wait) and `run-bot.sh` (bot against a published
  game), current script as thin orchestrator; parsing stays owned by
  `scripts/parse-run.sh`. Gate: one `M&M_v_ZealDrag` game still prints
  one verdict line (`KILLED:/LOST:/TIMES_HIT:/FRAMES:`). Evidence: that
  line + exit code.
- **T7 — C3 single SHM cleanup.** Stale `/dev/shm` cleanup is triplicated
  (`run-openbw-ums.sh`, `build-openbw.sh`, `run-openbw-server.sh`).
  One function in `kill-leftovers.sh` (or a `--shm` flag), all three
  callers use it. Gate: `--dry-run` never fails + one real game clean.
  Evidence: game log tail showing no stale-state failure.

## Measurement only (no engine change)

- **M1 — Wine A1.4b Victory semantics.** Map with 2 slots, one trigger:
  explicit `Victory` for player 1 at frame N. Report what happens to
  player 2 (continues? Defeat? draw?) with the game-table + slot lines
  as evidence. Do NOT change the propagation rule before this number
  exists. Any later change is gated on `use_map_settings` so melee stays
  bit-identical. Needs owner-approved Wine run (one game per box).

## Done-signal protocol

When a task is done, send exactly: `OpenBW engine track done: <T-ids>,
tests: <which gates ran + result>, evidence: <where>`. Then stop — do
not merge, do not rsync, do not touch the install flow. This side
re-runs the host smoke + `tests/run.sh` and reviews the diff before
anything is taken over. Partial completion is reported per task, never
as a bundle.
