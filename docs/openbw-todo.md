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
- [ ] **A1.1** Implement condition types used by real maps: 15 (Leader),
      16 (Deaths), 21 (Accumulate), 23 (Current player death count).
      Evidence today: `error("unknown trigger condition %d")` on those maps.
      Test: `tests/ums_map_test.cpp` with a map using them must not abort.
- [ ] **A1.2** Implement action 105 for real (AI scripts), so map-driven unit
      behaviour exists. Today a no-op; maps play but units stay passive.
- [ ] **A1.3** Load the map's own `TRIG` chunk. Today only `triggers/Melee.trg`
      is loaded (`triggers=6` on every map, measured). Either the UMS boot path
      is incomplete or `use_map_settings` is false when TRIG is read. This is
      a big behavioural gap and needs its own investigation.

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
- [ ] **A3.2** MELEE mode on a UMS map aborts at frame 0 (`ComputerAI` not
      supported). Correct behaviour would be to refuse the wrong mode with a
      clear message instead of an error mid-load.
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
- [ ] **B1** Turn the probe into a checked test: a script that starts the
      launcher, runs `tools/bwapi-handshake-probe`, and fails unless the output
      contains `BYTE_2_OK` + `MATCH_TICKING` + `MatchStart=YES`. Right now this
      is a manual step, so a regression in the pump would not be caught.
- [ ] **B2** Add a test for "second client after the first disconnects":
      `close(syncSocket)` leaves the listener closed, so a reconnect is
      expected to fail. Decide whether that is intended, then pin it.
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
- [ ] **C2** `--game` parsing (`Total time`, `Units killed/lost`, `HIT`) lives
      inline in `build-openbw.sh`. Move it to a small `parse-run.sh` so both
      `--game` and future tools use one implementation.
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
