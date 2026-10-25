# OpenBW / UMS work - task list

Companion to `openbw-run-regression.md` (the investigation log). This file is
the backlog: what is done, what is next, and what is deliberately not being
done. Keep items small and verifiable; every entry should name the evidence
that proves it.

Legend: `[x]` done and verified, `[ ]` open, `[?]` needs a decision.

---

## A. Engine (harness tree: 3rdparty/openbw)

### A1. UMS unit loading and triggers (branch `feature/support-ums-maps`)

- [x] `UNIT`: skip records flagged disabled in StarEdit (`valid_flags & 4`).
- [x] `MRGN`: reset `st.locations` before reading regions.
- [x] Trigger condition 4 (Switch) tolerated instead of aborting.
- [x] Trigger action 105 (Run AI script at location) ignored instead of aborting.
- [x] **A1.1** Conditions 15/16/21/23 implemented on a real death table
      (`unit_deaths`, filled in `kill_unit`, group ids 229-232 honoured).
      Verified directly: killing a Dragoon gives `deaths 0 -> 1`, `any=1`,
      `men=1`, `buildings=0`.
- [ ] **A1.1b** Condition 15 (Leader) semantics are assumed, not checked against
      a map where the leader decides the outcome. Test before trusting.
- [x] **A1.2** Switches are real state: condition 4 reads `switches[256]`,
      action 5 (set switch) implemented. Action 105 still a no-op by design
      (needs an AIScript interpreter + AIS/ALST chunks) but is now **counted**
      in `state::skipped_ai_script_actions`, so "scripts ignored" is visible.
- [ ] **A1.2b** Implement the AIScript interpreter (AIS/ALST chunks). Own
      project; without it map-driven unit behaviour does not exist.
- [x] **A1.3** Map triggers are attached to their players again. The gate
      accepted only `controller_occupied`, but after the UMS remap a map's
      "Computer" players are `controller_computer_game`, so every trigger was
      dropped. Measured: 0 -> 5 running triggers per playable slot on
      `1Drag_v_1Zeal.scm` (66a). The earlier "triggers=6" was Melee.trg on its
      own; map triggers were simply never attached.
- [x] **A1.4 investigation** Done (70). Audited: nobody ever wins -> the
      `winners` propagation block never fires on this map, and the melee
      detector is not involved. Frame-312 defeat is **map-declared**: trigger 1
      is `Elapsed time 300 s -> Defeat`, trigger 0 is `Command 18 -> Defeat`.
      The propagation rule is therefore NOT what ends this map.
- [ ] **A1.4b** The open half: what vanilla does to other players when one gets
      an explicit `Victory`. Needs the Wine measurement (2 slots, one Victory
      trigger). Do not change the rule before that. If changed, gate it on
      `use_map_settings` so melee stays bit-identical.
- [x] **A1.5** Triggers with an action list containing nothing executable no
      longer count as "trigger activity" (guard added; secondary to A1.4).

### A2. Server / client handshake (harness tree: bwapi/BWAPI/Source/BWAPI)

- [x] Accept an external client on every update (`checkForConnections()`
      unconditionally; the old guard stopped accepting once an in-process AI
      module set `externalModuleConnected`).
- [x] Send the connect handshake from `update()` (one `callOnFrame()` per
      update, after `updateSharedMemory()`).
- [x] Do not `shm_unlink()` the per-game segment (the client maps it by name).
- [x] Do not reset `eventCount` at the end of every update (MatchStart was
      never delivered).
- [x] Publish state while idle, so `playerCount`/`players` are correct for a
      client that attaches mid-game.
- [x] **A2.1** Split `Server::update()` into `publishFrameToClient()` and
      `acceptClientWhileIdle()` - single responsibility each, verified with the
      probe (same results before/after).
- [ ] **A2.2** Log line `Connected to AI Client process` never appears.
      Gate is `initializeAIModule()`'s `else if (server.isConnected())`, which
      needs `specifiedModule` empty and no auto-loaded module. `BWData` loads
      `TestAIModule`, so the in-process branch always wins. Low value (log
      only); do it if it keeps confusing future readers.

### A3. Known engine limitations (separate, larger)

- [ ] **A3.1** Maps that `error()` on `fixme rescue passive` / `fixme unknown
      flag` cannot be hosted at all.
- [x] **A3.2** MELEE on a UMS map: `map_places_units()` detects it, logs
      `using USE_MAP_SETTINGS instead of MELEE` and switches mode, instead of
      aborting mid-load with `ComputerAI not supported`.
- [x] **A3.4** `dyingUnits` emitted `UnitEvade`+`UnitDestroy` every frame a
      dying unit stayed in the list (`wasAccessible` never cleared). Fixed in
      `GameUnits.cpp`; live verification pending on the client harness (65c).
- [ ] **A3.5** `shot-fired` vs `HIT` ratio (69k:17): not a bug - `ShotTwin` logs
      every attack acquisition on purpose, `HitTwin` only real HP loss. If a
      cleaner metric is wanted, add a `fired-on-cooldown` row.
- [ ] **A3.3** `unit_counts` includes `Special_Map_Revealer` units. The slot
      report prints them separately now, but BWAPI's "participating player"
      test still counts them, so a slot with 140 revealers and 0 warriors looks
      "present". Decide: filter revealers out of that test, or keep and
      document.

---

## B. Tests

- [x] `tests/ums_map_test.cpp`: map loads, visible units, Dragoon/Zealot
      counts, owners, trigger loading, 300 frames without abort.
- [x] Keeps count of `unit_counts` minus map revealers (pins the 137-vs-14
      arithmetic).
- [x] **B1** `scripts/test-openbw-host.sh`: starts the launcher, runs the probe,
      and requires `BYTE_2_OK` + `MATCH_TICKING` + `MatchStart=YES` +
      `players=N/12`. Verified both ways: PASS on the fixed engine, 4/4 FAIL
      (exit 1) with `Server.cpp` reverted to HEAD.
- [ ] **B2** Add a test for "second client after the first disconnects":
      `close(syncSocket)` leaves the listener closed, so a reconnect is
      expected to fail. Decide whether that is intended, then pin it.
- [x] **B4** `tests/engine_behaviour_test.cpp` + `tests/run.sh`: seven checks in
      one run (units, revealers, trigger attachment, switches, deaths, 300
      frames, ignored AI scripts). Verified to fail on reverted fixes.
- [ ] **B3** Add a map fixture built from a real map (not synthesised): copying
      a small real `scenario.chk` and flipping one field is far cheaper than
      building a valid CHK from scratch (an earlier attempt died on
      `place_completed_unit failed`).

---

## C. Scripts (Atlantis/scripts; not committed, owned jointly)

- [x] `build-openbw.sh --game MAP`: rebuild + real bot game + one PASS/FAIL line.
- [x] `bwapi-publish.sh`: `PUBLISHED` / `STALE` / `NO GAME` based on live pid.
- [x] `kill-leftovers.sh`: argv0-anchored, case-insensitive guards; `--dry-run`
      never fails.
- [ ] **C1** `run-openbw-ums.sh` is ~200 lines and mixes: preflight, host
      start, publish wait, bot run, log parsing, verdict. Split into
      `start-host.sh` (starts launcher and waits for a live game) and
      `run-bot.sh` (runs the bot against an already published game), with the
      current script as the thin orchestrator.
- [x] **C2** `scripts/parse-run.sh` owns run parsing; `build-openbw.sh --game`
      calls it. Verified on synthetic + real runs, `?` for unresolved values.
- [ ] **C3** Stale `/dev/shm` cleanup is duplicated in three scripts
      (`run-openbw-ums.sh`, `build-openbw.sh`, `run-openbw-server.sh`). One
      function in `kill-leftovers.sh` (or a `--shm` flag) would do.

---

## D. Process

- [x] Single log file (`openbw-run-regression.md`) updated after every step.
- [x] One small step at a time; abandon an approach after a hard time box
      (the synthetic CHK fixture was dropped that way, not silently).
- [x] No commits in Atlantis without the owner's explicit go-ahead.
- [ ] **D1** When the client side settles, re-run the full matrix once and
      record it here: `1Drag` (all revealers, no warriors), `M&M_v_ZealDrag`
      (real 1v1), one broken map (A3.1), and a reconnect.

---

## E. Code structure (SRP applied where we touched code)

- [x] **E1** `Server::update()` split into `publishFrameToClient()` (attached:
      snapshot -> 2/1 exchange -> commands) and `acceptClientWhileIdle()` (idle:
      process events -> publish state -> accept). Three of our regressions came
      from these two behaviours being fused in one function; each is now named
      after its single responsibility. Behaviour unchanged, proven by the probe
      before/after.
- [ ] **E2** `Run` the same split on `run-openbw-ums.sh` (see C1) - it still
      mixes preflight, hosting, waiting, running and parsing.
- [ ] **E3** `BWData.cpp` is a single ~1800-line translation unit holding map
      loading, slot reporting, game setup and the frame pump. Extracting
      `slots` reporting into its own small unit would make the file navigable;
      low risk because it is a leaf function.

---
