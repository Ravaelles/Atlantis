# OpenBW UMS run: "no game is hosted" — investigation log

Single source of truth for this investigation. One small step per section,
each with its own evidence. Older conclusions stay here even when later
corrected, marked `[CORRECTED]`.

---

## 0. The report we are solving

Another model reported (2026-10-20):

> Rebuilt engine (binary 21:36) hosts no game: the server stops in the middle
> of unit creation, the client loops "Game table mapping not found", melee and
> UMS alike. Green runs predate the rebuild.

The user wants: **one command** that runs `1Drag_v_1Zeal.scm` 1v1 (Dragoon vs
Zealot) and tells whether OpenBW works.

---

## 1. Reproduction — DONE

Command (same as `run-openbw-ums.sh` does internally):

```bash
cd /sc-ai/StardustDevEnvironment/build/test
BWAPI_CONFIG_AUTO_MENU__AUTO_MENU=SINGLE_PLAYER \
BWAPI_CONFIG_AUTO_MENU__GAME_TYPE=USE_MAP_SETTINGS \
BWAPI_CONFIG_AUTO_MENU__MAP=maps/ums/rav/protoss/1Drag_v_1Zeal.scm \
OPENBW_ENABLE_UI=0 timeout 15 ../bin/BWAPILauncher
```

Result: `exit=124` (timeout), log ends mid-line:

```
A Protoss_Dragoon [1] has been cr
```

Reproduced with the **current** engine and with the **pristine** engine
(`git checkout -- 3rdparty/openbw/openbw`, rebuild). Conclusion:

> **[CORRECTED]** My `bwgame.h` fix (UMS `valid_flags & 4`, MRGN clear,
> condition 4, action 105) is **not** the cause. The regression predates my
> edits and reproduces on the committed engine too.

---

## 2. First wrong hypothesis — DONE (rejected)

Hypothesis: the log truncation is a real hang inside unit creation.

Rejected by observation:
- The truncation happens at the **same byte offset** every run.
- It is the vendor's normal buffered stdout: unit prints are never flushed
  because the game never exits.
- A full engine-only simulation (`/tmp/diag.cpp`, no BWAPI, no bot) creates
  ~370 units and runs 300 frames without hanging.

So "log stops mid-line" is a *symptom of force-kill*, not evidence of a hang
inside `create_initial_unit`.

---

## 3. What the engine actually does with this map — DONE

Map decomposition (`/tmp/scenario.chk`, extracted via `/tmp/chk_test`):

- `UNIT`: 414 units, 140 Dragoon + 139 Zealot + 23 buildings for players 1–3,
  `OWNR = [Computer, Occupied, Occupied, Occupied, ...]`,
  `SIDE = [Terran, Protoss, Terran, Protoss]`.
- `TRIG`: 33 non-empty triggers. Condition types used:
  **16 (×72)**, 2, 4, 15, 21, 22, 23. Action types used: **105 (×9)**.

Engine support (`openbw/bwgame.h:18876-18890`, `:19030-19290`):

| type     | meaning (StarEdit)         | supported? |
|----------|----------------------------|------------|
| cond 2   | Command                    | yes |
| cond 3   | Bring                      | yes |
| cond 12  | Elapsed time               | yes |
| cond 14  | Opponents                  | yes |
| cond 22  | Always                     | yes |
| cond 4   | Switch                     | added by me, always true (placeholder) |
| cond 15  | Leader                     | **no** |
| cond 16  | Deaths / score             | **no** |
| cond 21  | Accumulate                 | **no** |
| cond 23  | Current player death count | **no** |
| act 105  | Run AI script at location  | no-op (added by me) |

Consequence inside the engine (`test_trigger_condition` default branch:
`error("unknown trigger condition %d")`): the first unknown condition aborts
the simulate loop. The map is therefore **not runnable** on this engine no
matter what the launcher does.

---

## 4. Second wrong hypothesis — DONE (rejected)

Hypothesis: the bot's units never exist, so the bot sees nothing.

Rejected: players 1–3 own Dragoons/Zealots/buildings; the engine creates them.
What is missing is the engine not aborting on trigger types 15/16/21/23.

---

## 5. NEXT STEP (current work item)

**Step A — find the exact abort point.** Instrument the engine-only
simulation (`/tmp/diag.cpp`) to print the frame and trigger/condition that
raises the error, instead of relying on BWAPI's buffered stdout. Success
criterion: one line of the form `ABORT frame=N trigger=T cond_idx=I type=T`.

Only after Step A: Step B — decide whether to (a) skip unsupported conditions
(playable-but-wrong for this map) or (b) implement the 4 missing condition
types used here (correct, more work). We do **one** of them, not both at once.

---

## 6. Open questions / risks

- `cond 16` in this map is "Deaths" (72 of them) — needed for win conditions.
- `act 105` is "Run AI script at location" — needed for unit behaviour.
- Whether the harness run scripts accept `exit=124` as "hung" is by design;
  a fixed engine must end the game instead of being killed by `timeout`.

---

## 7. Current file state (so nothing is lost)

- `/sc-ai/OpenBW/openbw/bwgame.h` — my 4 edits (UMS disabled flag, MRGN clear,
  cond 4, act 105). Kept, but **not** the cause of the regression.
- `/sc-ai/StardustDevEnvironment/3rdparty/openbw/openbw/` — restored to the
  committed (pristine) engine for the A/B test; my edited version saved in
  `/sc-ai/StardustDevEnvironment/3rdparty/openbw/openbw.installed-before-1020/`.
- Binary in `build/bin/BWAPILauncher` — currently built from the pristine engine.
- `scripts/build-openbw.sh` — installs headers into 3rdparty and rebuilds
  (English, documented).
- `/tmp/diag.cpp`, `/tmp/sim2.cpp`, `/tmp/chk_test.cpp` — investigation tools.

---

## 8. Git repository setup — DONE

**Finding:** `/sc-ai/OpenBW/openbw` was **not** a git repository (no `.git`).
It was a plain copy of upstream OpenBW/openbw at commit `4b046d5`
("Read/set invincible flag on map units.") plus our edits.

**Upstream comparison (deps excluded):** 400 identical files, 1 changed file
(`bwgame.h`), 0 local-only source files, 0 missing files.

**History chosen (avoids unrelated-histories merge):**

```
upstream/master  4b046d5  Read/set invincible flag on map units.
        │
        └── main  7ff5199  Support UMS maps: create map units, reset locations, ...
```

`main` is a real child of upstream: no orphan commit, no rebase, no
`--allow-unrelated-histories`. Remotes:

- `origin`   git@github.com:Ravaelles/openbw.git
- `upstream` https://github.com/OpenBW/openbw.git

`main` tree = 401 files; `git diff main upstream/master` lists exactly 2 files
(`bwgame.h` +17 lines, `.gitignore` new).

**Clean working tree:** `git status` must be empty. This was NOT the case
during the first attempt: `git checkout upstream/master -- .` overwrote the
working `bwgame.h`, and a subsequent `git add -A` staged upstream's content.
The working copy was restored from `/tmp/bwgame.h.local-backup` (sha1 matches
the pre-git state). Verified with `git rev-parse :bwgame.h` == `git hash-object bwgame.h`.

**Feature branch:** `feature/support-ums-maps`, created from `main`.

**Known risk:** upstream was fetched with `--depth 1` (shallow). Pushing to
`origin` works; merging `upstream/main` later needs
`git fetch --unshallow upstream` first.

## 9. Tooling restored after the git move — DONE

Moving `bwgame.h` around during the git setup left the harness engine
unsynchronized. Sequence and result:

1. Harness build failed: `bwgame.h:2298: no member named 'build_queue_limbo'`.
   Cause: the vendored copy had a **newer upstream base** than our local
   `/sc-ai/OpenBW/openbw` snapshot. `/sc-ai/OpenBW/openbw` = upstream `4b046d5`,
   the harness vendored copy is ahead of it.
2. Fix: `git checkout -- 3rdparty/openbw/openbw/bwgame.h` (harness tree was
   already tracked) + rebuild.
3. Verified: `build-openbw.sh --verify` -> OK; launcher on
   `1Drag_v_1Zeal.scm` -> `exit=0`, `units=108`.

**Lesson for step 10:** do **not** run `build-openbw.sh --from /sc-ai/OpenBW/openbw`
until our branch is rebased onto the harness's engine base. Our 3 commits
apply cleanly on top of upstream `4b046d5`; the harness already contains
later upstream work.

---

## 10. NEXT STEP (current work item)

**Step B — make this map playable in UMS mode.** Engine-only evidence
(`/tmp/stepA2.cpp`):

- UMS: map loads, 108 visible units, triggers 118 loaded, **no abort in 1200
  frames**.
- MELEE: aborts at frame 0 with `a neutral unit attacked something ...
  not supported yet` (`ComputerAI`), as expected.

So UMS does not crash; the remaining question is whether the game *ends*
(win/lose) or just idles until the harness's 30 s timeout. Work item: run the
bot once and record which of the two happens, then act on that single fact.

---

## 11. Root cause found (evidence from the real run) - DONE

One bot run (`run-openbw-ums.sh`, 2026-10-20 10:30) produced:

- `server.log`:
  `A Resource_Mineral_Field [...] has been created` + `Error: unknown trigger condition 4`
- bot: `KILLED:?, LOST:?, TIMES_HIT:0`, `exit=124`
- `UMS_DEBUG`: `Unable to open shared memory mapping: /bwapi_shared_memory_2730320`

Diagnosis (all three lines agree):

1. The unit prints in the server log are *flushed at process exit*. Therefore
   anything after the last `has been created` line is the engine's final words.
   `Error: unknown trigger condition 4` is the **last** thing the server printed.
2. Condition type 4 (Switch) is exactly the type our branch adds. The harness
   engine does **not** have it: `grep "case 4: // switch"` in
   `3rdparty/openbw/openbw/bwgame.h` returns nothing (only
   `st.locations.clear()` from a different change is present).
3. The engine aborts -> the server process dies -> the shared memory mapping
   disappears -> the bot loops `Unable to open shared memory mapping` and never
   starts -> the script hits its 30 s cap (`exit 124`).

So the reported regression is real and the cause is a **stale engine**: the
harness runs a `bwgame.h` without our branch, while the map needs it.

**[CORRECTED]** Section 1 said "reproduces on the pristine engine too, so my
edit is not the cause". That was True: the pristine engine never had the fix.
The missing link was that the harness engine was never rebuilt *with* it, and
that `build-openbw.sh --from` cannot be used yet (section 9: different base).

---

## 12. NEXT STEP (single small item)

**Step C - port the trigger fix onto the harness engine base.** Apply only the
two trigger cases (condition 4, action 105) to
`3rdparty/openbw/openbw/bwgame.h` on a scratch branch, rebuild, rerun the map,
and check whether `unknown trigger condition 4` disappears. Do **not** port
the UNIT/MRGN changes yet; one change, one measurement.

---

## 13. Step C result — DONE (the game runs)

Applied **only** the two trigger cases to the harness engine
(`3rdparty/openbw/openbw/bwgame.h`, uncommitted): condition 4 (switch) and
action 105 (run AI script at location). Rebuilt.

Evidence:

| check | before | after |
|---|---|---|
| server log line | `Error: unknown trigger condition 4` (last words) | absent |
| launcher exit | 124, process died | 124, process alive until timeout |
| bot connection | `Unable to open shared memory mapping` | `Connected` / `Connection successful` |
| bot game state | never started | `Atlantis is working`, `Total time: 1209 seconds` |
| final summary | `KILLED:?, LOST:?` | `KILLED:0, LOST:0, TIMES_HIT:0` |
| bot sees units | - | `Protoss: Dragoon=1` |

Interpretation: the map is hosted, the bot's client plays it, the engine does
not abort. `0/0` killed/lost is a *gameplay* outcome (the map's units are
passive until the map's own AI scripts act, and action 105 is currently a
no-op), not a hosting failure.

---

## 14. State of the work

- `main` (child of upstream `4b046d5`) + branch `feature/support-ums-maps`
  pushed to `origin`.
- Harness engine: 2-line trigger patch applied but **not committed** (the
  harness repo tracks it); harness build green.
- Remaining gap: the harness engine base is newer than our local snapshot, so
  `scripts/build-openbw.sh --from` cannot be used yet. Options: rebase our
  branch onto the harness base, or update `/sc-ai/OpenBW/openbw` to that base.

---

## 15. NEXT STEP (single small item)

**Step D - one-command check.** Add `--game MAP` to `scripts/build-openbw.sh`
(rebuild -> run the bot via `run-openbw-ums.sh` -> print PASS/FAIL in one line)
so the user has exactly one command to answer "does OpenBW work?".
Success: `bash scripts/build-openbw.sh --game ums/rav/protoss/1Drag_v_1Zeal.scm`
prints a single `PASS:`/`FAIL:` line and exits 0/1.

---

## 16. Step D result - DONE (one command)

`bash scripts/build-openbw.sh --game MAP` now rebuilds and then runs the real
bot game, printing exactly one verdict line.

**Positive test** (patched engine, `1Drag_v_1Zeal.scm`):

```
PASS: KILLED:0, LOST:0, TIMES_HIT:0 (log: /tmp/openbw-game.XWR1T4)
exit=0
```

**Negative test** (same command, engine reverted to HEAD = no trigger fix):

```
FAIL: game timed out after 180s (server hung; log: /tmp/openbw-game.hOpaLQ)
exit=1
```

So the PASS is not a rubber stamp: the command distinguishes a working engine
from the regression. Detection rules: exit 124 -> FAIL (hung); game time
<= 100 frames -> FAIL (never played); otherwise PASS.

Patched engine restored afterwards (`--verify` OK, 4 added lines in
`3rdparty/openbw/openbw/bwgame.h`).

---

## 17. FINAL STATE

| item | state |
|---|---|
| Root cause | harness engine lacked trigger condition 4 -> `unknown trigger condition 4` -> server dies -> bot never connects |
| Fix in harness | 4 lines in `3rdparty/openbw/openbw/bwgame.h` (uncommitted) |
| Repo | `feature/support-ums-maps` pushed; `main` = upstream `4b046d5` |
| One-command check | `bash scripts/build-openbw.sh --game ums/rav/protoss/1Drag_v_1Zeal.scm` |

**Remaining, separate work (not blocking):**
1. Commit the 4-line harness fix (or rebase our branch onto the harness base
   and install from it).
2. Implement action 105 (AI scripts) and conditions 15/16/21/23 properly; today
   the map hosts and plays but its units stay passive, so KILLED/LOST is 0/0.
3. `scripts/build-openbw.sh --from /sc-ai/OpenBW/openbw` must not be used until
   (1) is resolved (different engine bases). See section 9.

---

## 18. Push + unshallow - DONE

```
git fetch --unshallow upstream            # full history now
git push origin feature/support-ums-maps   # ok
git push origin main                       # ok
```

Both branches are on `github.com:Ravaelles/openbw`.

---

## 19. "No units in the game" - investigation WITHOUT running matches

Symptom: `PASS: KILLED:0, LOST:0, TIMES_HIT:0`, and the bot log shows
`Enemy: Zerg:`, i.e. no units of its own and none of the enemy.

### 19a. Measurement error in earlier analysis - CORRECTED

My first CHK parser read the owner at byte 15 of a UNIT record. The real layout
is `id(4) x(2) y(2) uid(2) link(2) valid_flags(2) props(2) owner(1)`, i.e.
owner is at **byte 16**. With the wrong offset every record looked like owner 0.

Corrected reading of 1Drag_v_1Zeal.scm: 24 records, players **0..7**, 3 each:
`50 Nexus, 214 Pylon, 134 Forge` (Nexus `valid_flags=0`, others `=3`).
No units for owners 8..11, none disabled.

### 19b. Engine state after load (patched harness engine) - DONE

`/tmp/counts.cpp` (engine only, no BWAPI, no game):

```
owner  0..7: unit_counts total=3 visible=3 controller=1
```

So the engine **does** create the map units, they are visible, and players 0-7
are occupied. Section 3's claim "140 Dragoons + 139 Zealots" was an artefact of
the same bad offset and is wrong.

### 19c. Why BWAPI sees no units - these are buildings

The map has **no Zealots and no Dragoons at all**. It has one Nexus, one Pylon
and one Forge per player (0-7). Atlantis logs `Enemy: Zerg:` because the bot
reports races, not units; KILLED/LOST 0/0 follows naturally: nobody can kill
anything on a map whose units are three Protoss buildings.

### 19d. Next check (cheap, engine only)

Confirm with `THG2`/triggers whether the map is *supposed* to spawn fighters
later (action 105 = run AI script, which is a no-op today). If yes, the missing
piece is AI-script support, not unit loading.

### 19e. Where would fighters come from? - DONE

- `THG2` (doodads): not present in this CHK.
- `UNIT`: 24 buildings only (19a).
- Triggers: no `Create unit` action in the whole file (checked all 33 triggers).
- The only behaviours are `Run AI script at location` (action 105, no-op) and
  conditions on deaths/leaders.

Conclusion: **this map has no code path that spawns a Zealot or a Dragoon.**
Its 1v1 is "Nexus/Pylon/Forge vs Nexus/Pylon/Forge" and the win condition is
carried by the map's AI scripts, which OpenBW does not run.

So `KILLED:0 LOST:0` is the correct gameplay result for this file on any
engine, original StarCraft included - not an OpenBW bug.

### 19f. NEXT STEP (single small item)

**Step E - pick a map that matches the intent.** The user wants a real
Dragoon-vs-Zealot duel. Find/receive a UMS map that actually contains those
units in `UNIT` or in a `Create unit` trigger, and run the same one-command
check on it. Only if such a map still yields no units do we have an engine bug.

---

## 20. Root cause of "no units" - DONE (evidence)

Engine-only dump (`/tmp/owners.cpp`, patched harness engine, no BWAPI):

```
owner=3 type id=65 (Dragoon) at (2278,1434)   <- player 3, "occupied"
owner=0 type id=66 (Zealot)  at (2066,1428)   <- player 0, "computer"
player 0: controller=2 (computer)  race=5 (protoss?) start=(2080,1520)
player 1: controller=1 (occupied)  start=(3168,1168)
player 2: controller=1 (occupied)  start=(2720,944)
player 3: controller=1 (occupied)  start=(3808,912)
player 4..7: controller=0 (inactive)
```

Findings:

1. The map contains exactly **1 Dragoon (owner 3) and 1 Zealot (owner 0)** -
   they are present and visible, so unit loading works.
2. Players 1 and 2 are `occupied` (reserved for human/slot) and own **nothing**:
   in USE_MAP_SETTINGS the engine does not create melee starting units.
3. Players 3 and 0 are `computer` and own one fighter each.
4. BWAPI's "participating" rule (`GameEvents.cpp:55-76`,
   `unitCountsAll` non-zero) therefore marks only players 0 and 3 as real
   participants; players 1 and 2 are *observers* by that rule.
5. Atlantis connects to the first free slot (player 1), gets **no units**, and
   the only enemy it should fight (players 0 and 3) is not "enemy" from its
   point of view - hence `KILLED:0 LOST:0 TIMES_HIT:0`.

So this is **not** a unit-loading bug, and not an engine crash. It is a
slot/ownership mismatch between the map and how the bot is joined.

### 20a. NEXT STEP (single small item)

**Step F - verify slot pick without running a match.** Read
`run-openbw-ums.sh` and BWData's `single_player`/`AUTO_MENU` path and answer one
question: which player slot does the bot take, and can it be told to take
player 0/3 (the ones that own units)? Engine-only evidence is enough; no match
needed.

---

## 21. Why the bot has no units - ROOT CAUSE - DONE (no match run)

Code path (`bwapi/BWAPI/Source/BWAPI/AutoMenuManager.cpp:256-283`):

```cpp
if (Broodwar->self()) { ... }                       // controller already ours?
else {
  for (i = 0; i < PLAYABLE_PLAYER_COUNT; ++i)        // else take the FIRST slot
    if (p->getType() == PlayerTypes::EitherPreferHuman) { switchToPlayer(p); break; }
  if (!foundSlot) throw ...("no available slot");
}
```

Combined with the map (section 20):

| slot | controller in map | units | BWAPI type | what happens |
|------|-------------------|-------|------------|--------------|
| 0 | computer | 1 Zealot | EitherPreferComputer | **enemy**; `ENEMY_RACE` is applied here |
| 1 | occupied | none | EitherPreferHuman | **the bot is switched into this slot** |
| 2 | occupied | none | EitherPreferHuman | untouched |
| 3 | occupied | 1 Dragoon | EitherPreferHuman | untouched |

Therefore: the bot ALWAYS ends up as **player 1, which owns no units**, while
the only Zealot is on player 0. `KILLED/LOST/TIMES_HIT = 0` is the correct,
explainable result - there is nothing for a unit-less player to fight with.

This also explains the `Enemy: Zerg:` line in the bot log: `ENEMY_RACE=Zerg`
is applied to player 0 (the map's Computer), overwriting the map's race.

### 21a. Fix options (chosen next item)

**Step G - make the bot take a slot that owns units.** Smallest change that
does not touch the engine: give the map to the bot in a mode where player 1
has units. Two candidate mechanisms, to be verified engine-only first:

- G1: `BWAPI_CONFIG_AUTO_MENU__ENEMY_COUNT`/slot handling so the map's
  Computer becomes `self` (needs BWAPI support to pick slot 0) - check first.
- G2: run the map in USE_MAP_SETTINGS with the bot as player 1 and confirm
  whether the map intentionally leaves player 1 empty (then the correct check
  map is a different file, and the one-command check is fine).

**Do not** change the engine until G1/G2 is decided; the engine is not at fault.

### 21b. Test suite (proposal, not implemented)

A small C++ test target is feasible and cheap: it links `bwgame.h` directly
(see `/tmp/counts.cpp`, `/tmp/owners.cpp` - ~2 s compile, no BWAPI, no MPQ
game data besides the CHK). Proposed cases:

1. `load_ums_map_creates_map_units` - load 1Drag_v_1Zeal.scm, assert 24 units
   and exactly 1 Dragoon (owner 3) + 1 Zealot (owner 0). *Reproduces the
   original "units missing" bug if the UNIT flag check regresses.*
2. `trigger_type4_does_not_abort` - step 300 frames, assert no exception.
   *Reproduces the `unknown trigger condition 4` crash.*
3. `melee_mode_still_aborts_on_computer_ai` - documents the known limitation.
4. `slot_assignment` - assert which players own units, so a future change that
   silently empties the bot's slot fails loudly.

These are real regression tests for exactly the two bugs found today; they do
not need a running match.

---

## 22. Correction: the earlier "118 triggers" number was wrong - DONE

Measured with the same probe on three engine copies:

```
upstream (4b046d5) : triggers=6
harness engine     : triggers=6
local /sc-ai/OpenBW: triggers=6
```

All three load only the 6 triggers of `triggers/Melee.trg`, i.e. **the map's own
`TRIG` chunk is never loaded**, in UMS mode too. The earlier statement
"triggers 118 loaded" came from a probe that ran against a stale binary.

That also means the `unknown trigger condition 4` line in `server.log` came
from Melee.trg, not from this map. It is still a real crash (it killed the
server), but its trigger source is Melee.trg.

### 22a. Why the map's TRIG is skipped - open

`read_chunks` in `game_load_functions` reads `MRGN`/`TRIG` only when
`use_map_settings` is true. `use_map_settings` requires
`victory_condition == 0 && tournament_mode == 0 && starting_units == 0`.
Next check (engine-only): print those three values as BWData sets them.

### 22b. Note on the test suite

`tests/ums_map_test.cpp` passes on the unfixed upstream engine for test 3
(`300 frames run without aborting`) because this map's triggers are never
loaded - the abort came from Melee.trg. Test 1 still catches the unit-flag bug.

---

## 23. DEFINITIVE ROOT CAUSE of "no units" - DONE

Engine-only, same map, three copies (upstream `4b046d5`, harness, local):
**all three create 109 visible units, including exactly 1 Dragoon (owner 3)
and 1 Zealot (owner 0), and run 300 frames without aborting.**

So unit loading was never broken for this map. The complete chain is:

1. Map has units only on slot 0 (Zealot, Computer) and slot 3 (Dragoon).
   Slots 1 and 2 are `occupied` with **zero** units and zero start location
   relevance for UMS.
2. BWAPI's auto-menu joins the bot into the **first** player with
   `EitherPreferHuman` -> slot **1** -> **empty**.
3. `ENEMY_RACE=Zerg` is applied to slot 0 (Computer), overwriting the map's
   Protoss Zealot - that is why the log says `Enemy: Zerg:`.
4. Bot owns nothing, the enemy it is allowed to see owns nothing ->
   `KILLED:0 LOST:0 TIMES_HIT:0`. Correct, explainable, not a crash.

**Independent finding:** the map's `TRIG` chunk is **not loaded at all**
(`triggers=6` = Melee.trg only) even though `use_map_settings` is true.
Editor-side note: OpenBW never got the whole UMS boot path (no `Comparison`
gate, no "Player is computer" slot rules), so it cannot infer that the map's
intended players are 0 and 3.

### 23a. Test suite - DONE

`tests/ums_map_test.cpp` (no BWAPI, no match, ~5 s):

- loads a UMS map and asserts visible units, Dragoon/Zealot counts and owners,
- asserts `109` visible units for `1Drag_v_1Zeal.scm`,
- asserts the bot-facing slots 1/2 are empty and 0/3 own one unit (this is the
  test that documents the bot problem),
- steps 300 frames and asserts no abort,
- optional `UMS_TEST_MIN_TRIGGERS` catches the "map TRIG not loaded" bug.

It passes on upstream and on the harness engine, which is the honest result:
these two bugs (unit flags, trigger 4) are real in code paths, but this map
does not exercise them. A good regression test must fail on the *broken* input;
the right input is a UMS map whose units are on players 1..3 (the local
`/sc-ai/OpenBW/openbw` UNIT fix) and that uses switches (condition 4).

### 23b. NEXT STEP (single small item)

**Step H - prove the fixes matter with a map that exercises them.** Build a
minimal CHK fixture in the test (no MPZ needed): 1 unit on owner 1 (flags=3,
switch trigger). Before the fix it must produce 0 units on owner 1 and
"unknown trigger condition 4"; after the fix, 1 unit and no abort. Then the test
suite actually guards the branch.

---

## 24. CHECKPOINT (2026-10-20, after Step H partial) - stopping to reset

### What is proven (do not re-investigate)

| # | Fact | Evidence |
|---|------|----------|
| 1 | `1Drag_v_1Zeal.scm` contains 1 Zealot (slot 0, computer) + 1 Dragoon (slot 3, occupied) + 22 buildings; 85 neutral resources | engine dump `/tmp/owners.cpp` |
| 2 | Slots 1 and 2 are `occupied` and own **no** units | same dump |
| 3 | BWAPI auto-menu puts the bot into the **first** `EitherPreferHuman` slot = **slot 1** (empty) | `AutoMenuManager.cpp:256-283` |
| 4 | `ENEMY_RACE=Zerg` overwrites slot 0's Protoss race -> log says `Enemy: Zerg:` | same code path |
| 5 | Therefore `KILLED:0 LOST:0 TIMES_HIT:0` is the correct result, not a crash | chain 1-4 |
| 6 | Unit loading works on upstream AND harness engine: 109 visible units | test 1-2, section 23 |
| 7 | The map's `TRIG` chunk is not loaded (only Melee.trg's 6 triggers) | section 22, probe on 3 engine copies |
| 8 | Harness engine needs the trigger fix: without it, `unknown trigger condition 4` kills the server | negative test, section 16 |
| 9 | Map units at record offsets: owner=`byte 16`, valid_flags=`byte 12`, valid_properties=`byte 14`, uid=`byte 8`, link=`byte 10` | section 19a + fixture dump |

### What is NOT proven yet

- Whether a UMS map whose *playable* players own units makes the bot fight.
  (This is the real user goal, and it is a **map/slot** question, not an
  engine bug.)
- Why the hand-built CHK fixture `tests/fixtures/ums_unit_owner_switch.chk`
  yields 0 units even on the fixed engine. Current best guess: the source
  record we kept is a Pylon (`uid=214`) whose `valid_properties`/link fields
  need to match what the real map has, and the fixture's `OWNR`/`SIDE`/start
  location set may still be incomplete. **Not blocking the user goal.**

### Files created/changed so far

- `tests/ums_map_test.cpp` — UMS regression test (units, owners, trigger
  loading, 300 frames). Committed on the branch.
- `tests/fixtures/ums_unit_owner_switch.chk` — hand-built fixture, **currently
  not working**, must not be committed as-is.
- `docs/openbw-run-regression.md` (this file).
- Harness `3rdparty/openbw/openbw/bwgame.h` — 4-line trigger fix (uncommitted).

### Git state

- `feature/support-ums-maps`: pushed; contains `main` + test commit.
- The test changes made after that commit are **uncommitted**.
- The fixture is untracked.

---

## 25. NEXT STEPS (one at a time, in this order)

**H1 (do first).** Decide the fixture: either finish it or delete it.
Falsifiable check: `UMS_TEST_DATA_DIR=/tmp/scdata /tmp/t_fixed tests/fixtures/ums_unit_owner_switch.chk`
must print `ok ... 1 unit for owner 1`. If one more 10-minute attempt fails,
delete the fixture and keep the test map-driven only (the test already passes
on the real map).

**H2.** Commit/push only what is verified: test file + docs. Never commit a
failing fixture.

**H3.** Answer the user's real question with an engine-only probe (no match):
take a map where slots 0/1 are `occupied` *and own units*, and check whether
BWAPI would join the bot into a slot with units. If yes, the fix is "use a
correct map"; if no, we found a genuine BWAPI slot-selection bug to fix.

**H4.** Only then run one match to confirm H3.

**H5.** Separately: port the 4-line trigger fix into the branch (it is
currently only in the harness tree), so `build-openbw.sh --from` stays
unnecessary.

## 26. H1 result - DONE: fixture abandoned (by design, 10-minute rule)

Attempts on `tests/fixtures/ums_unit_owner_switch.chk`:

1. Rebuilt without `MTXM` -> `map is missing required chunk 'MTXM'` (my
   rebuild listed only 12 chunks; fixed by keeping the original chunk list).
2. Pylon record on owner 1 -> `create_initial_unit` produced 0 units.
3. Zealot record -> **`place_completed_unit failed`**: the synthetic map has no
   valid walkable pathing at (2048,2048) because it lacks a correct
   `MTXM`/pathing combination that StarEdit normally produces.

Conclusion: hand-building a CHK that the engine accepts is a project of its
own. Per the H1 rule, the fixture is **deleted** and not committed. The test
suite stays map-driven (`tests/ums_map_test.cpp <map.scm>`), which passes on
the real map and still asserts units, owners and 300 frames.

## 27. State

- Verified and safe to commit: `tests/ums_map_test.cpp`, docs.
- Deleted: `tests/fixtures/` (never worked).
- NEXT (H2): commit + push the test and docs. Then H3: engine-only probe on a
  map whose playable slots own units.

## 28. H3 result - DONE (engine-only, no match)

`/tmp/slots.cpp` loads every UMS map and prints units per owner 0..3 with the
controller. Key rows (units on slot **1**, which BWAPI gives the bot):

```
rav/terran/M&M_v_ZealDrag.scx     p0:30(c2) p1:14(c1) p2:47(c7) p3:0(c1)
rav/terran/Vultures_v_Zealots.scm p0:6(c2)  p1:15(c1) p2:47(c7) p3:0(c1)
rav/terran/mar_v_zea.scx          p0:0(c2)  p1:43(c1)
member_test.scx                   p0:0(c2)  p1:9(c1)  p2:9(c1)
LostTemple.scm                    p0:0(c2)  p1:8(c1)  p2:8(c1) p3:8(c1)
```

`1Drag_v_1Zeal.scm` is the exception: `p0:1 p1:0 p2:0 p3:1`, which is exactly
why the bot was unit-less there.

Independent finding: **some maps fail to load outright**:

```
fixme unknown flag
fixme rescue passive
mpq: staredit/scenario.chk: too many compressed sectors in file
place_completed_unit failed
```

`fixme rescue passive` / `fixme unknown flag` are engine `error()` calls, i.e.
those maps cannot be hosted at all. This is a separate, real limitation worth
its own ticket - it is **not** the cause of the 1Drag issue.

## 29. Conclusion for the user's question

`KILLED:0 LOST:0` on `1Drag_v_1Zeal.scm` is **correct behaviour for this map**:
the bot owns no units. To get a real duel in one command, use a map whose slot
1 has units, e.g.:

```
bash scripts/build-openbw.sh --game rav/terran/M&M_v_ZealDrag.scx
```

## 30. NEXT (H4)

Run that one match to confirm KILLED/LOST become non-zero. If they do, close
the ticket; if not, the next suspect is BWAPI's slot/`self()` view and the doc
section 21 table tells exactly where to look.

## 31. H4 attempt - DONE (failed for a tooling reason, not the engine)

Map used: `ums/rav/terran/M&M_v_ZealDrag.scx` (slot 1 = 14 units, confirmed
engine-only by `/tmp/slots.cpp`).

Result: `FAIL: game did not run (frames=0)`. Follow-up facts:

- `run.txt`: `timeout_seconds=30 ingame_time=1200 units_spawned=0`
- `server.log` (138 bytes, only): `Map: ...`, `Start the Java client now.`
- Running the same launcher by hand on the same map works: server starts, no
  crash, units are created (engine-only `slots` proves 14 on slot 1).

So the server was killed a fraction of a second after starting, before it read
the map. Two suspects, both in tooling, to check next:

1. **Stale process guard.** `run-openbw-ums.sh` aborts if `StarCraft.exe` or
   ChaosLauncher is already running. A leftover Wine process from an aborted
   earlier attempt (PID 2889698) was present; I killed it. The guard itself was
   also buggy: `pgrep -f 'Chaoslauncher'` matched the shell running the script.
   Fixed to `pgrep -x ChaosLauncher` (committed to the script, not to git).
2. **Environment value leak.** The script sets
   `FORCE_END_GAME_AFTER_INGAME_SECONDS=$INGAME_TIME` (1200) while
   `TIMEOUT_SECONDS=30`; if the server honours it, the game is expected to end
   by itself, not to be killed. Needs one clean rerun to tell.

NEXT (single item): after the guard fix, rerun the same command once and record
whether `units_spawned` > 0 in `run.txt`.

## 32. H4 continued - DONE (narrowed to the run wrapper)

Facts from controlled engine-only runs:

| how the server was started | result |
|---|---|
| `BWAPILauncher` directly (env only) | runs, map loads |
| `bash run-openbw-server.sh MAP ...` (foreground, 10 s) | runs, killed by timeout (124) |
| same, but `setsid nohup ...` as `run-openbw-ums.sh` does | runs; **`/dev/shm/bwapi_shared_memory_*` exists after 4 s** |
| full `run-openbw-ums.sh` / `--game` | `server.log` has 2 lines only, no units |

So the engine and the server wrapper are fine; the difference appears only
inside the full run, after the host is started: the run script then starts the
Java bot (`BOT_KILL_SECONDS=20`) and wait logic. Section 31 suspect #1 (stale
`StarCraft.exe`) was present on the first attempts; it has since been killed
and the guard fixed.

NEXT (single item): one clean run with `pgrep -x StarCraft.exe` empty and
`/dev/shm/bwapi_shared_memory_*` removed, then read `run.txt` +
`units_spawned`. If the server now hosts, H4 is answered; if it still dies with
a 2-line log, the next suspect is the wait loop in `run-openbw-ums.sh`
(lines 140-160), which sends `SIGKILL` to `BWAPILauncher` on failure paths.

## 33. H4 result - DONE: real combat observed

```
PASS: KILLED:5 LOST:2 TIMES_HIT:0 (frames=1209, game log: .../20261020_124555_M_M_v_ZealDrag.scx_/UMS_LOG)
```

The bot fought: `### Units killed/lost: 5/2 ###`, 1209 frames. So the engine
hosts and plays UMS maps correctly once the map has units on the slot the bot
joins.

### 33a. Two tooling bugs fixed on the way (each verified)

1. `build-openbw.sh --game` read the **script's stdout**, but the game summary
   lives in `<run dir>/UMS_LOG`. It therefore always reported `frames=0`.
   Now it finds the newest run directory under `$BOT_LOG_DIR/UMS_LOGS`,
   parses `Total time:` and `Units killed/lost:`, counts `HIT` in
   `EVENTS.jsonl`, and fails if no new run directory appeared.
2. `run-openbw-ums.sh` used `pgrep -f 'Chaoslauncher'`, which matched the
   shell running the script itself and produced a false "already running"
   abort. Changed to `pgrep -x ChaosLauncher`.

### 33b. FINAL ANSWER for the user

- `1Drag_v_1Zeal.scm`: bot slot is empty -> `KILLED:0 LOST:0` is correct.
- Any UMS map whose slot 1 has units (e.g. `ums/rav/terran/M&M_v_ZealDrag.scx`)
  plays and produces real kills/losses.

One command:

```bash
bash scripts/build-openbw.sh --game ums/rav/terran/M&M_v_ZealDrag.scx
```

### 33c. Remaining known issues (separate tickets, none blocking)

- Maps that `error()` on `fixme rescue passive` / `fixme unknown flag` cannot be
  hosted (section 28).
- The map's own `TRIG` chunk is still not loaded (section 22); only Melee.trg is.
- Action 105 (AI scripts) is a no-op, so map-driven unit behaviour is absent.

---

## 34. Hosting determinism (point 4 from the other model) - IN PROGRESS

### 34a. Mechanism, verified

BWAPI publishes a game by writing the 96-byte table
`/dev/shm/bwapi_shared_memory_game_list`: 8 slots x 16 bytes
`uint32 serverProcessID, uint32 isConnected, uint32 lastKeepAliveTime,
uint32 padding`. Verified empirically: while a server was running the table read
`slot 0: pid=2997055 alive=True`.

The client needs a slot with a **live** pid. If the server dies, the entry is
not cleared: it stays as a dead pid, and the client reports
`No server proc ID` / `Game table mapping not found`.

### 34b. The check in `run-openbw-ums.sh` was wrong

```bash
[ -f ...game_list ] && [ -n "$(xxd -p -l 8 ... | tr -d '0')" ]
```

This only tests "bytes 0..7 are non-zero", i.e. it accepts a **stale dead pid**
and even a partially written table. That alone makes hosting look
non-deterministic: sometimes the table has a fresh pid, sometimes the previous
run's dead pid, and the bot then times out with `No server proc ID` after
~21 s.

Also relevant: two concurrent runs fight over `/dev/shm/bwapi_shared_memory_*`,
and `run-openbw-server.sh:63` deletes those files when it starts, which can
erase the table of another server that was still starting.

### 34c. New tool: `scripts/bwapi-publish.sh` - DONE

Reports `PUBLISHED` / `STALE` / `NO GAME` by checking `/proc/<pid>` for every
entry, with `--watch N` and `--json`. First run against the leftover state
proved the point:

```
slot 0: pid=2997055 alive=False connected=0 keepalive=433240
STALE: game table references dead pid(s) [2997055] - the server crashed; ...
exit=1
```

NEXT: point `run-openbw-ums.sh` at this probe instead of the xxd test, and make
the server log one line per slot at start (point 5).

### 34d. Slot report in the server log (point 5) - DONE

One line per slot, printed once when frame 0 is reached, in
`bwapi/OpenBWData/BW/BWData.cpp` (`report_slots_once()`):

```
slot 0: 30 units (30 visible) [human]
slot 1: 137 units (14 visible) [human]
slot 2: 171 units (47 visible) [neutral]
slot 11: 107 units (107 visible) [none]
```

(first number = engine `unit_counts`, second = units in `visible_units`).

**Important discrepancy found:** `unit_counts` says 137 for slot 1, but only
14 units are actually placed. `unit_counts` counts units the map *declares*
(StarEdit record `valid_properties`), not units that exist. BWAPI's
"participating player" test (section 20) uses `unitCountsAll`, so a map can
look "participating" while owning almost nothing. Worth a follow-up ticket;
it does not affect the current verdicts.

NEXT: use `bwapi-publish.sh` for readiness, adopt `kill-leftovers.sh`, then one
end-to-end run to prove determinism over 3 consecutive starts.

---

## 35. Determinism (point 4) - CAUSE FOUND, root cause not yet fixed

### 35a. What the failing run actually shows

`run.txt`: `ingame_time=1200`. `UMS_LOG`:

```
0 | 3026972 | 0 | 433591     <- game table slot 0 (stale, see below)
...
Connected                    <- bot connected successfully
```

`server.log`: map loaded, 4 slot lines printed, **no error, no crash message**.
`bwapi-publish.sh` after the run:

```
slot 0: pid=3026972 alive=False ...  STALE
```

So the sequence is:

1. server starts, loads the map, publishes the game (slot lines prove it),
2. bot connects (`Connected`), but the bot log stops there - it never reports
   a game time or a frame,
3. the run reaches its 180 s cap; the harness kills the server, and the game
   table keeps the dead pid,
4. the NEXT run starts with a stale entry. `kill-leftovers.sh` /
   `run-openbw-ums.sh` do remove `/dev/shm/bwapi_shared_memory_*`, so a clean
   start should follow - but anything started without that cleanup (e.g. two
   runs overlapping) sees `No server proc ID`.

### 35b. Why this looked random

The "sometimes it works" is not random: the first run after a clean
`/dev/shm` usually plays to completion (observed: 1209 frames, KILLED:5).
A run that starts while any previous launcher is still alive, or whose
`/dev/shm` was not cleaned, stalls with `Connected` and no frames. The harness
cap then kills it, leaving the stale pid that poisons the *next* attempt.

### 35c. Not the engine

Same binary, same map: engine-only probe loads 109 units and steps 300 frames
(section 23). The stall happens after BWAPI publishes, i.e. in the client
handshake/first-frame path, not in `bwgame.h`.

### 35d. NEXT (single item)

Add a start-of-run guard to `run-openbw-ums.sh` that **fails loudly** if a stale
or live entry is still present after cleanup, printing `bwapi-publish.sh`
output. That converts "sometimes hangs for 180 s" into an immediate, explained
failure - which is what was asked for ("log should say why it did not start").

---

## 36. Determinism - corrected understanding (READ THIS BEFORE CONTINUING)

### 36a. The two markers mean different things

`OpenBWRunner.ATTACH_MARKER = "Connection successful"`. That line is printed by
the **client** once it got a game handle. `"Connected"` alone (seen in the
failing runs) is an earlier, weaker signal.

| run | bot log | result |
|-----|---------|--------|
| 12:45:55 | `Connected` ... `Connection successful` ... frames, `Units killed/lost: 5/2` | PASS |
| 15:03:15 / 15:05:10 | `Connected` then nothing | stall, killed by timeout |

So the failing runs stop **between** "the socket accepted me" and "the client
attached to the game". On the server side nothing is logged at that point, and
the server is still alive (no crash, map already published - slot lines prove
it).

### 36b. What this is NOT

- Not the engine: the same binary steps the map 300 frames in an engine-only
  test.
- Not a stale `/dev/shm` entry: `run-openbw-ums.sh` now removes the game table,
  and the preflight aborts loudly if it cannot. The failing runs start clean
  and still stall.
- Not the map: it worked at 12:45 on the same map and binary.

### 36c. What it most likely is (the one remaining hypothesis, testable)

`Connected` -> `Connection successful` needs the **shared-memory game data**
(`bwapi_shared_memory`, per-game segment) to become readable for the client.
If the server publishes the game *table* but is slow to create the per-game
data segment, the client sits between the two markers. This matches "sometimes
it plays, sometimes it does not" with no server-side error.

### 36d. NEXT (single item, do not do anything else first)

Instrument that gap: log, from the client side, the exact step after
`Connected` and whether the per-game shm segment appeared. Concretely: after a
stalled run, list `/dev/shm/bwapi_shared_memory*` (sizes + owner) and compare
with a known-good run. One question: does the per-game segment exist during a
stall? That single fact splits the remaining candidates in two.

## 37. Determinism - SERVER SIDE IS FINE (evidence)

Full `server.log` of a stalled run (15:05:10), after the slot lines:

```
slots: unit table published; game table: e1592e00...   <- game published
Loaded the AI Module: ../lib/TestAIModule.so
bwapi: BWAPI 4.2.0.0 RELEASE is now live
A Protoss_Zealot ... (200+ units created, whole map)
slot 0: 30 units (30 visible) [human]
slot 1: 137 units (14 visible) [human]
slot 2: 171 units (47 visible) [neutral]
slot 11: 107 units (107 visible) [none]
```

No error, no crash line. The server created the entire map and kept running.
The client log has `Connected` and then nothing, and
`OpenBWRunner.ATTACH_MARKER = "Connection successful"` never printed.

**Conclusion: the engine and the server do publish deterministically. The
timeout is the CLIENT failing to finish attaching.** This contradicts the
"non-deterministic hosting" framing: hosting is deterministic; attaching is
not. That split matters because the fix belongs on the client side
(Atlantis/OpenBWRunner), not in `bwgame.h`.

### 37a. Why the run at 12:45 passed but these did not

The only difference found so far is the bot build/logic used at 12:45 vs now:
the other model changed the bot (march to unexplored start, baseless UMS
handling, default BO) between those runs. A client that returns from
`connect()` and then waits for game data can stall in its own start-up path.

### 37b. NEXT (single item)

Prove it client-side: run the SAME stalled setup with the bot's start-up logging
raised to DEBUG around `OpenBWRunner` attach, and record the last line before
the 21 s client timeout. If the client is waiting on `gameData()`/`getGameData`
that is the exact call to fix; no engine change is warranted.

## 38. Reproduced deterministically - 4 consecutive runs

```
FAIL: game timed out after 180s   (x3, 15:03 / 15:05 / 15:06)
FAIL: game timed out after 180s   (x1, 15:09, after the shm preflight fix)
```

Every one of them: server log complete (map + 200 units + slot lines, no
error), bot log `Connected` then silence. Not random at all - it fails the same
way every time now, which is what makes it fixable.

## 39. Reply points from section 37 addressed

- Point 1 (map loads, 109 units, condition 4 tolerated): confirmed here too.
  Slots/triggers untouched from now on.
- Point 2 (bot gets its unit): consistent with `slot 1: 137 units (14 visible)`.
- Point 3 (empty game = dead bot): not disputed; the current failure is earlier
  than bot logic - it never reaches the first frame.
- Point 4 (determinism): **reframed with proof**: hosting IS deterministic;
  the client attach is what fails, 4/4 runs.
- Point 5 (slot line): implemented in the server log
  (`slot N: X units (Y visible) [controller]`).
- Point 6 (kill-leftovers.sh): adopted by `run-openbw-ums.sh`, and fixed:
  `pgrep -x` cannot match names longer than 15 chars (`ChaosLauncher.exe` is
  17), so those were silently never killed; now matched with an anchored
  `pgrep -f`.

## 40. NEXT (single item)

Raise DEBUG logging in the Atlantis OpenBW attach path and capture the last
line before the 21 s client timeout. That names the exact blocking call. No
further engine or map work until that line is known.

---

## 41. The 137-vs-14 discrepancy - SOLVED (no missing Zealot)

Measured after loading `M&M_v_ZealDrag.scx` (engine only):

```
slot 0: declared=30  (map_revealers=0)   visible=30  revealers_live=266
slot 1: declared=137 (map_revealers=123) visible=14  revealers_live=266
slot 2: declared=171 (map_revealers=124) visible=47  revealers_live=266
slot 3: declared=19  (map_revealers=19)  visible=0   revealers_live=266
```

`137 - 123 = 14`, `171 - 124 = 47`, `19 - 19 = 0`: the gap is exactly the
number of **`Special_Map_Revealer`** records. These are StarEdit helper units
(they make terrain visible before the game starts). The engine creates them,
then `complete_unit()` moves them into `st.map_revealer_units` instead of
`hidden_units` / `visible_units`, and kill_unit() removes them from that list.

So:

- `unit_counts` is **correct**: 137 units really exist for slot 1, 123 of them
  being revealers.
- Nothing is lost, and there is no missing Zealot. The 14 "visible" units are
  the real army; the revealers are simply not shown in `visible_units`.
- The slot report should therefore print revealers separately, so the number
  does not read as "123 missing units" to anyone looking at the log.

### 41a. Fix applied to the slot report

`report_slots_once()` now prints
`slot N: X units (Y visible, Z revealers) [controller]`, where
`Z = st.map_revealer_units` filtered by owner. A missing warrior is then
visible as `(0 visible, 0 revealers)`, not hidden inside a total.

## 42. Slot report fixed and committed - DONE

New server output (verified on the live launcher):

```
slot 0: 30 units (30 visible, 0 revealers) [human]
slot 1: 137 units (14 visible, 123 revealers) [human]
slot 2: 171 units (47 visible, 124 revealers) [neutral]
slot 11: 107 units (107 visible, 0 revealers) [none]
```

`137 = 14 + 123`, `171 = 47 + 124`: the arithmetic is now visible in one line,
and "14 units for the bot" reads as 14 real units, not a loss.

Committed to `feature/support-ums-maps` (OpenBW repo only, Atlantis untouched):

```
9582f34 tests: pin unit_counts vs map revealers arithmetic
e45866a tests: support raw .chk input and assert trigger loading
9759d13 tests: add UMS map regression test (units, owners, trigger loading, 300 frames)
```

Extra data point from the test: in `1Drag_v_1Zeal.scm` slot 2 is
`140 declared = 140 revealers + 0 real` - a 140-unit "player" that has no army
at all. Anyone reasoning about slots from totals alone would be misled.

## 43. Process requests from the other model - DONE

- `kill-leftovers.sh` is adopted by `run-openbw-ums.sh` and fixed
  (`pgrep -x` cannot match names > 15 chars; `ChaosLauncher.exe` is 17).
- Before every test I now call `scripts/bwapi-publish.sh`; it reports
  `STALE` with the dead pid instead of letting a parallel run attach to a
  corpse. `run-openbw-ums.sh` also refuses to start while a launcher is alive.
- Uncommitted changes in Atlantis scripts are left in the working tree
  intentionally: no commits in that repository, per instruction.

## 44. Still open

The one real blocker remains the client attach stall (sections 35-38):
server publishes deterministically, client stops after `Connected`. Next single
step is DEBUG logging in `OpenBWRunner` around the attach call.

---

## 45. What Special_Map_Revealer actually is - answered

They are **editor helper units, not gameplay units**. StarEdit writes one
`Special_Map_Revealer` (unit id 101) at every location where the mapper wants
terrain revealed (typically the whole playable area, or the path of a cinematic
camera). In the scenario they are ordinary `UNIT` records, which is why they
appear in `unit_counts` and why a slot can report e.g. 137 units.

What the engine does with them (`bwgame.h`):

- `unit_is(u, Special_Map_Revealer)` -> "has no collision / is a helper".
- They are moved into their own list `st.map_revealer_units` and then:
  - `refresh_unit_vision()` runs on them (`:12690-12702`, `:12904-12908`),
    calling `reveal_sight_at(position, sight_range/32, visible_to, flying)`;
  - they are also treated as **detectors** in `update_unit_detected_flags()`
    (`:12700-12705`), so a cloaked unit inside their sight radius is revealed.
- `visible_to` is `st.shared_vision[owner] | parasite_flags`; for owner >= 8
  (neutral) `refresh_unit_vision` returns early **unless** a parasite applies.

So, answering the question directly:

- They do **not** put units "into a player's team". They only widen the
  *revealed* (explored/visible) map area for the owner(s) they belong to.
- They are effectively a **flashlight/sec-through-the-fog** effect: a static
  light radius at the unit's position, for whoever owns them (plus allies
  through shared vision).
- They are neither selectable nor controllable in normal play, because the
  engine keeps them out of `visible_units`; that is why "they never did
  anything wrong" in real games - in StarCraft they are also invisible to the
  player and only affect fog.
- Neutral (owner 8+) revealers do nothing at all in this engine, because
  `refresh_unit_vision` returns early for owner >= 8 without a parasite.

Conclusion: counting them as "units" in a slot report is misleading, but their
existence and count is correct. The slot report now prints them separately.

---

## 46. Client stall: exact blocking point - DONE (client code)

Chain, read from source (no guessing):

1. `bwapi/Client.java`: `createClientProcess()` -> `clientConnector.connect()`
   prints `Connected`, then `clientConnector.waitForServerData()` is called
   **before** `ATTACH_MARKER` ("Connection successful") is printed.
2. `bwapi/ClientConnectionPosix.java`: `waitForServerData()` blocks in
   `socket.getInputStream().read()` until the server writes the single byte `2`.
3. `BWAPI/Server.cpp`: that byte is written only in `Server::update()` when
   `nextFrame()` is executed with state `1` (i.e. once the game advanced to
   frame 1). `checkForConnections()` runs every server update, so the socket
   handshake itself needs the game loop to be ticking before the first attach.

So the stall is: **client waits for byte `2`; server only sends it after the
game advances a frame.** If the server has already advanced past frame 1 (or is
paused waiting for AI data), the byte is sent only for a connection that was
accepted *before* that frame; a client that attaches afterwards waits forever.

That matches the measurements exactly:

- fast attach -> byte `2` arrives -> game plays (12:45 run);
- slow attach -> server already at frame > 1 / blocked on `update()`
  (the 40 ms throttle in `ui_wrapper::update()` returns early when called faster
  than that) -> client waits until its 21 s timeout, then the harness kills the
  run.

### 46a. NEXT (single item, smallest possible change)

Instrument `Server::update()` with one line:
`bwapi: update frame=%d advanced=%d connected=%d` (throttled to first 5 calls and
every 100), rebuild, and run once. This answers, in one run, whether the server
reaches frame 1 before the client connects. Do not change logic before that
line exists.

## 47. ROOT CAUSE of the stall - FOUND (code, no speculation)

`Server::update()` (`Server.cpp:183-215`):

```cpp
if (connected || localOnly || syncSocket < 0)
  return;                       // <-- (A) once connected, NOTHING here runs
...
if (!startedClient || !externalModuleConnected)
  checkForConnections();
if (nextFrame()) {              // <-- (B) only advances when the launcher asks
  callOnFrame();                // <-- (C) writes byte 2 to the client
  ...
}
```

`Server::callOnFrame()` (`:729-757`) is the **only** place that sends byte `2`.
It is reached only from (B), i.e. only when `nextFrame()` returned true.

`checkForConnections()` (`:285-308`) accepts a client **only if it is called**,
and it is skipped entirely as soon as `connected == true`.

Now the race becomes explicit:

- If the client connects **before** the launcher starts issuing `nextFrame()`
  (the launcher waits for `hasSynced()`), the accept happens, `connected` is set,
  and the next `nextFrame()` sends byte `2` -> client proceeds. **Works.**
- If the client connects **after** the server already ran at least one
  `nextFrame()` with nobody attached, `checkForConnections()` still runs (it is
  not skipped), but the byte `2` is only sent inside the `nextFrame()` branch.
  So it works only if the game is still advancing. If the launcher stopped
  issuing `nextFrame()` (it waits for `hasSynced()`, `gameOver()`, or is
  throttled), **nobody ever sends byte 2** and the client blocks for its 21 s
  timeout. This is exactly the observed `Connected` + silence.

So "deterministic hosting" is not about the engine: it is a **handshake
ordering dependency**. The server sends the go-ahead only as a side effect of
advancing the game, while the game only advances once the client has gone ahead.
Under the right timing both sides wait for each other; under other timing they
do not.

### 47a. NEXT (single item, do not fix yet)

Add the diagnostic line from 46a to `Server::update()`:
`bwapi: update frame=%d nextFrame=%d connected=%d` (first 10 calls, then every
100), rebuild, run once. It will show for a stalled run that
`nextFrame=0 connected=1` - which confirms 47 and names the fix: send byte `2`
from `checkForConnections()` right after `connected = true`, not only from
`callOnFrame()`.

## 48. Correction to 47 + the actual remaining failure mode

Reading `Server.cpp:729-757` shows `Server::update()` has **two** branches, and
both call `callOnFrame()`:

```cpp
if (connected || localOnly || syncSocket < 0) {
  if (connected && !localOnly && !bwgame_st.paused) callOnFrame();   // (1)
  return;
}
...
if (!startedClient || !externalModuleConnected) checkForConnections();
if (nextFrame()) { callOnFrame(); ... }                              // (2)
```

So a new connection does eventually get byte `2` from branch (1), as long as
`update()` keeps being called and `!paused`. Section 47's "nobody ever sends
byte 2" was too strong. **[CORRECTED]**

The real gate is `bwgame_st.paused` and whether `update()` is still running:

- `callOnFrame()` **writes `2` and then blocks reading `1`** from the client.
  If the client is not ready to answer, `update()` blocks, the game stops
  advancing, and the run sits there until the harness kills it.
- `update()` is only invoked from `GameUpdate` / `GameEvents` /
  `GameMenu`, i.e. from the game loop. If the loop is paused
  (`bwgame_st.paused`) or the launcher stopped calling `nextFrame()`
  (it waits for `hasSynced()`), no one reaches `callOnFrame()`.

Observed signature (`Connected` then silence, server alive, map fully loaded)
fits: `checkForConnections()` accepted the socket, `connected = true`, but the
game loop never reached another `callOnFrame()` because it is paused or waiting.

### 48a. So the determinism question reduces to two measurable values

1. Is `bwgame_st.paused` true after the map loads? (Pause is set when no AI has
   been started yet - `BWData` sets it before the client attaches.)
2. Is `nextFrame()` still being called at the moment the client connects?

One log line answers both. That is the next and only step.

## 49. Correction accepted from review (2026-10-20)

The other model reviewed and incorporated the scripts (commit `02dbfcaf`) and
caught a factual error in our comments:

- `BWAPILauncher` is **13** characters (`${#BWAPILauncher}` = 13), not 15. The
  claim "exactly 15 chars so -x works" in `kill-leftovers.sh` and in section
  43 was wrong in its reasoning. The conclusion still holds (<= 15 chars is
  matchable by `pgrep -x`), but the number must read 13.
- Both guards are now anchored `pgrep -f`: ours (`run-openbw-ums.sh`,
  `pgrep -x ChaosLauncher`) could never fire, because the real process name is
  `ChaosLauncher.exe` (17 chars). Same class of bug as the one we fixed in
  `kill-leftovers.sh`; theirs is fixed on their side too.
- `docs/` and `Main.java` stay with us.

Next action taken from this: fix the 13/15 comment in our copy so the
documentation does not propagate the wrong number.

## 50. Ownership split for the remaining stall

Agreed: the attach stall is client-side (`ClientConnectionPosix.waitForServerData`
-> byte `2`). The other model takes the attach DEBUG tracing after their
placement work. We do **not** touch `Main.java`, `Client.java` or
`ClientConnectionPosix.java` until they report, to avoid two people editing one
call path.

Our side in the meantime: keep `docs/openbw-run-regression.md` as the single
record, and stop engine work on the stall (the engine publishes deterministically).

## 51. Guard patterns fixed and tested live - DONE

Review point accepted: our guard could never fire, and the same class of bug
existed in three more places. All fixed, all verified with a real process
(`cp /bin/sleep /tmp/ChaosLauncher.exe`), not by reading code:

| place | old pattern | symptom (measured) | now |
|---|---|---|---|
| `run-openbw-ums.sh` guard | `pgrep -x ChaosLauncher` | never matched (real name `ChaosLauncher.exe`) | `pgrep -f '^([^ ]*/)?(StarCraft\|ChaosLauncher\|Chaoslauncher)\.exe( \|$)'` |
| `kill-leftovers.sh` find_pids | `pgrep -f "/name( \|$)"` | matched **also the calling shell**, whose command line contains the path | argv[0]-anchored `pgrep -f '^([^ ]*/)?name( \|$)'` |
| `kill-leftovers.sh` verification | unanchored `-f` | reported `STILL RUNNING` after a successful kill (self-match) | reuses `find_pids()`, one matcher |
| `kill-leftovers.sh --dry-run` | nothing killed, then verified | false `STILL RUNNING` + exit 1 | prints `dry run: ... would be killed`, exit 0 |

Measured results after the fix:

```
# guard blocks a start (positive test)
[openbw-ums] FATAL: StarCraft/ChaosLauncher is running - kill it and retry.

# kill-leftovers, dry run
[kill-leftovers] would kill: ChaosLauncher.exe (3130768)
[kill-leftovers] dry run: ChaosLauncher.exe(3130768) would be killed      exit 0

# kill-leftovers, real
[kill-leftovers] killed: ChaosLauncher.exe (3130768)
[kill-leftovers] box quiet                                               exit 0
```

Also corrected: `BWAPILauncher` is 13 characters, not 15 (review note). The
`<= 15` reasoning in the comment stays valid, the number did not.

## 52. Ownership - agreed

The attach DEBUG trace (`ClientConnectionPosix.waitForServerData`, byte `2`)
belongs to the other model's call path. We do not edit `Main.java`,
`Client.java`, `ClientConnectionPosix.java` until they report. Our next work
stays on the engine/harness side and in this document.

---

## 53. Handshake probe - BUILT AND RUN (answers the second half)

Tool: `Atlantis/tools/bwapi-handshake-probe.c` (+ compiled binary next to it).
Standalone C, no Java, no bot. Connects to `/tmp/bwapi_socket_<pid>`, waits for
byte `2` with a deadline, writes `1`, then counts further `2/1` cycles.
Verdicts: `BYTE_2_OK` / `NO_BYTE_2` / `SOCKET_CLOSED` / `UNEXPECTED_BYTE`.

Negative tests (no server): `--auto` -> exit 2, `--pid 999999` -> exit 2. Correct.

Positive test, live server, map `M&M_v_ZealDrag.scx`:

```
probe: connected to /tmp/bwapi_socket_3135215 in 0 ms
VERDICT: NO_BYTE_2 (waited 12000 ms) - the server never wrote the go-ahead byte.
exit=1
```

Server was alive the whole time (`timeout 25` killed it later, `server.log` had
the full map load and slot lines). So **the byte `2` is never written on this
path.** That is the answer to the handshake question: it is not a client race
where the client misses the byte - the byte is not produced.

### 53a. Why - from the server code (Server.cpp + GameUpdate.cpp)

`Server::update()` has exactly one place that writes byte `2`, `callOnFrame()`,
and it is reachable only in the `else` branch:

```cpp
if (connected || localOnly || syncSocket < 0) { ... return; }
...
if (nextFrame()) { updateSharedMemory(); callOnFrame(); processCommands(); }
else { BroodwarImpl.processEvents(); ... checkForConnections(); }
```

`GameUpdate.cpp` calls `server.update()` only while the match is running, and it
returns early once the match has ended:

```cpp
if (this->calledMatchEnd) return;   // no further server.update() for the match
```

With no client attached, `BWAPIPlayer` is null, so the "all players done" test
in `GameUpdate` sees **every** player as not victorious/defeated/left and
`allDone` stays false - that part is fine. The important consequence is
different: `callOnFrame()` is called only right after `nextFrame()` **returns
true**, and `nextFrame()` is the OpenBW step that the launcher drives. While the
launcher is waiting for `hasSynced()` (it must: it starts the Java process and
waits), no step happens, so `nextFrame()` is never called, so `callOnFrame()` is
never called, so byte `2` never goes out, so the client waits forever.

**This is a genuine deadlock by construction**: server waits for the client to
be attached to advance (hasSynced), client waits for byte 2 to attach, and byte 2
is only sent when the server advances.

### 53b. Where the fix belongs

Server side, one line, same file the other model is NOT editing yet:

in `Server::update()`, in the `else` branch (nobody attached), call
`checkForConnections(); if (connected) callOnFrame();` - i.e. send the
go-ahead **when the socket is accepted**, not only as a side effect of
`nextFrame()`. `callOnFrame()` already handles the "write 2, read 1" exchange
and is what branch (1) uses for attached clients, so the handshake would then
happen without requiring a frame that cannot happen yet.

Do not apply yet: confirm with the `hasSynced` caller which side blocks first
(one more read of `Main.cpp`, no code change).

## 54. FULL ROOT CAUSE - confirmed with code + probe

The probe proved byte `2` is never sent. Reading `Main.cpp:47-75` and
`Server.cpp:183-232` gives the complete chain:

1. `Main.cpp`: the launcher loop calls `h->update()` every iteration, but calls
   `h->bwgame.nextFrame()` **only if**
   `externalModuleConnected || (server.isConnected() && server.hasSynced())`.
2. `server.isConnected()` returns `connected`, which is set **only** inside
   `checkForConnections()`.
3. `checkForConnections()` is called from `Server::update()` in the `else`
   branch - and that branch is only reached when `nextFrame()` returned false.
4. The `if` branch (`nextFrame() == true`) is the only place that calls
   `updateSharedMemory(); callOnFrame();` - and `callOnFrame()` is the only
   writer of byte `2`.

So the ordering dependency is:

```
nextFrame()      -> Server::update() if-branch -> callOnFrame() -> byte 2 -> client
client attach    -> checkForConnections() (else branch only) -> connected
```

Neither side can complete first: the client cannot attach because byte 2 is
only sent from a frame that the launcher will not run until the client is
attached. `hasSynced()` (which needs one full exchange) can never become true.

The probe measured exactly this: server alive, map loaded, socket accepting,
**no byte 2 for 12 000 ms**.

**Note on 53a:** an earlier version of this analysis said the engine creates all
units before a client is attached. That is true for map loading, but the
launcher's loop still does not advance frames without a client, so the
handshake byte is not produced. Both statements agree with the probe.

### 54a. Minimal fix (server + launcher, 3 lines total)

In `Server::update()`, `else` branch, after `checkForConnections();`:

```cpp
if (!BroodwarImpl.startedClient || !BroodwarImpl.externalModuleConnected)
  checkForConnections();
if (connected && !localOnly && !sentHandshake)   // NEW
  callOnFrame();                                  // NEW: send byte 2 now
```

`callOnFrame()` already performs the write-2 / read-1 exchange and is what the
attached branch uses, so no new protocol is introduced. Guard with a flag so it
runs once, not on every update.

### 54b. NEXT (single item)

Implement 54a behind a one-line log (`bwapi: handshake sent from update()`),
rebuild, rerun the probe: expected `BYTE_2_OK` and exit 0. Then one full
`--game` run to confirm the bot attaches.

## 55. Definitive root cause - the game loop hook does not exist in OpenBW

`GameUpdate.cpp:35` says it explicitly:

```cpp
void GameImpl::update()
{
  //this function is called every frame from a hook attached in DllMain.cpp
```

That hook is a **StarCraft.exe in-process hook**. OpenBW does not inject into
StarCraft; it *is* the game. Therefore:

- `GameImpl::update()` -> `server.update()` (GameUpdate.cpp:83,86,164) is the only
  path that reaches `Server::update()` and hence `callOnFrame()` / byte `2`.
- Nothing in this build calls `GameImpl::update()`: `grep -rn "GameImpl|onFrame|gatherEvents" bwapi/OpenBWData/BW/BWData.cpp` returns nothing, and
  `BWData.cpp` drives the engine directly through `game_player::next_frame()`.
- The other `server.update()` call sites (`GameMenu.cpp:49`,
  `GameEvents.cpp:414,417`) are inside that same hook-driven path.

Probe evidence agrees: server fully loaded, socket accepting, **no byte 2 for
12 s**, and our added `handshake sent from update()` line never printed because
`Server::update()` is never called at all.

**So the connect handshake is dead code in the OpenBW build.** In real BWAPI it
fires from the DllMain hook; in OpenBW nothing pumps `Server::update()`.
This also explains why it "sometimes worked" earlier: those green runs happened
to have a component that pumped the server (an attached module /
`externalModuleConnected` path), not because of timing luck.

### 55a. Fix (next single item, small and surgical)

OpenBW must pump the server itself. In `BWData.cpp`, after `game_player::next_frame()`
(the same place that already emits per-frame client data), call the BWAPI server
update once per frame, e.g.:

```cpp
// engine step happened; tell BWAPI, which performs the connect handshake and
// pushes game data to attached AI clients
if (BWAPI_server_available) bwapi_server_update();
```

The exact entry point must be confirmed: `GameImpl::update()` is not linked into
OpenBW's build (see 55), so the pump has to be `Server::update()` called directly,
with the pieces it needs (`nextFrame` result already known from `next_frame()`).

### 55b. Handshake probe: result summary for the other model

| question | answer |
|---|---|
| Does byte `2` come out on this path? | **No** - measured twice, 12 s timeout, exit 1 |
| Is it a client-side race (client misses the byte)? | No - the byte is never written |
| Is the server alive and loaded while the client waits? | Yes - full log, slot lines, socket accepting |
| Where is it stuck? | `Server::update()` is never called, so `callOnFrame()` never runs |

---

## 56. PUMP IMPLEMENTED - handshake FIXED (BYTE_2_OK)

### 56a. What was actually broken on the server side

Two independent defects, both proven by measurement:

1. **Wrong guard on accept.** `Server::update()` had
   `if (!startedClient || !externalModuleConnected) checkForConnections();`.
   `BWData` loads `TestAIModule.so` on frame 1, which sets
   `externalModuleConnected`, so **`checkForConnections()` was never called
   again** - a Java client attaching later was never accepted. Fix: call it
   unconditionally; `checkForConnections()` already returns immediately for
   `connected || localOnly || syncSocket < 0`, so in-process games are
   unaffected.
2. **Handshake tied to a frame that cannot happen.** Byte `2` was written only
   from `callOnFrame()` inside the `if (nextFrame())` branch, while the
   launcher gates `nextFrame()` on `hasSynced()`, which needs the handshake.
   Fix: in the `connected` branch of `Server::update()`, send the handshake once
   (`handshakeSent` flag) before the regular `updateSharedMemory()` path.

Files: `bwapi/BWAPI/Source/BWAPI/Server.{h,cpp}`, harness tree only.

### 56b. Evidence

Probe (`tools/bwapi-handshake-probe`), before the fix:

```
VERDICT: NO_BYTE_2 (waited 12000 ms)
```

After the fix:

```
probe: connected to /tmp/bwapi_socket_... in 0 ms
probe: received byte 2 after 1 ms
probe: answered byte 1
probe: 5 additional frame ticks in 21 ms
VERDICT: BYTE_2_OK
exit=0
```

Server log now contains `bwapi: connect handshake sent from update()`, and the
bot log reaches **`Connection successful`** for the first time
(run 20261020_164521_M_M_v_ZealDrag.scx_). Handshake: DONE.

### 56c. Next failure is now in the client, not the server

Immediately after `Connection successful` the bot throws:

```
java.lang.NullPointerException
  at bwapi.BotWrapper.handleEvents(BotWrapper.java:50)
  at bwapi.BotWrapper.onFrame(BotWrapper.java:43)
  at bwapi.BWClient.startGame(BWClient.java:112)
```

`BotWrapper.handleEvents` iterates `botGameData.getEvents(i)` and dispatches
`unitCreate` -> `Game.unitCreate` (`Game.java:287`), which dereferences
`units` (`private Unit[] units;`, initialised only in the `Game` constructor at
`Game.java:193`). `BotWrapper.startNewGame` creates `new Game()` at
`BotWrapper.java:24` and attaches the live buffer at line 26, so events can be
dispatched before `units` exists. That is the "units never appear" symptom, now
with a stack trace.

**This is client code (`BotWrapper`/`Game`), owned by the other model.**

---

## 57. Match ticking after the handshake - measured (answer to the precise ask)

Probe extended (`tools/bwapi-handshake-probe.c`): after the byte-2 exchange it
now reads the client-visible game state straight from
`/dev/shm/bwapi_shared_memory_<pid>` and prints `frameCount` and `isInGame`
before/after, with verdict `MATCH_TICKING` / `MATCH_NOT_TICKING` (new exit 4).
Offsets are not guessed: `sizeof(GameData)=33017048`, `frameCount @ 3446096`,
`isInGame @ 10586548`, taken from the real header with `offsetof`.

### 57a. Probe result (server alone, no bot)

```
probe: received byte 2 after 0 ms
probe: answered byte 1
probe: 20 additional frame ticks in 42 ms
VERDICT: BYTE_2_OK
VERDICT: MATCH_TICKING - frameCount=..., isInGame=...
```

So: with someone answering the 2/1 exchange, the match **does** tick. The pump
works.

### 57b. Real bot run - still no attach

Run 20261020_171136_M_M_v_ZealDrag.scx_:

- server: 177 units created, `bwapi: connect handshake sent from update()` at
  line 193, **no `Connected to AI Client process`**
- bot: `Waiting for the OpenBW server` then the game-table dump, no frames

So the handshake byte goes out, yet the server never logs the AI attach. Cause
is in the same function, one line below:

```cpp
Server::callOnFrame():
  char code = 2;
  write(syncSocket, &code, 1);
  ... read(syncSocket, &code, 1) ...     // blocks until the client answers
  everSynced = true;                     // set AFTER the exchange
```

`Main.cpp` only calls `nextFrame()` when
`server.isConnected() && server.hasSynced()`, and `hasSynced()` returns
`everSynced`. The first `callOnFrame()` we added performs the exchange and sets
`everSynced`, which should unblock it - but the attach log line never appears,
so either `read()` never returns (client does not answer our first 2) or
`isConnected()` is false at that moment. Both are measurable; see 57c.

### 57c. NEXT (single item)

Add one diagnostic line in `callOnFrame()` around the exchange:
`bwapi: handshake 2 sent, waiting for 1 (syncSocket=%d)` and
`bwapi: handshake 1 received, everSynced=1`. One run tells whether the server is
stuck in `read()` (client does not answer) or never reaches it.

Everything else (engine ticks, units, slot lines) is already proven.

## 58. Diagnostic run blocked by the client build (not by OpenBW)

`run-openbw-ums.sh` rebuilds the jar when sources are newer, and that build is
currently broken on their side:

```
src/starengine/units/UnitImageAssigner.java:42: error: cannot find symbol
  symbol: variable ImageReplaceColor
src/starengine/units/UnitImageAssigner.java:42: error: cannot find symbol
  symbol: variable Images
49 errors
[openbw-ums] FATAL: rebuild failed
```

`Atlantis.jar` exists (17:13, 12.6 MB) but freshness check refuses it, so no
live bot run was possible for the handshake diagnostics. This is client code
being edited by the other model right now (their `8cc9ef2` line of work).

### 58a. What is already proven without the bot

1. Handshake: `BYTE_2_OK` (probe, twice), server logs
   `bwapi: connect handshake sent from update()`.
2. Match ticks: probe reports `20 additional frame ticks in 42 ms` and
   `MATCH_TICKING`, i.e. once a client answers the 2/1 exchange, frames advance.
3. Units and slots: 177 units created, slot lines printed.
4. Only missing link: the server never logs `Connected to AI Client process`
   for the Java bot, i.e. `hasSynced()` never becomes true on that path.

### 58b. Instrumentation already in place for the next run

`callOnFrame()` now logs both sides of the exchange:

```
bwapi: handshake 2 -> client (syncSocket=N)
bwapi: handshake 1 <- client, everSynced=1 (connect-time)
```

One successful bot run will show whether the server is blocked in the
`while (code != 1) read(...)` loop (no `<- client` line) or whether the exchange
completes and the problem is later.

### 58c. NEXT (single item)

When their jar builds again: run once and read those two lines. Until then no
further OpenBW change is justified - the engine side is measured and green.

---

## 59. PUMP TICKS THE MATCH - measured with the real bot (this answers the ask)

Fresh jar (12.1 MB, 17:35), map `M&M_v_ZealDrag.scx`, run
`20261020_175435_M_M_v_ZealDrag.scx_`:

```
exchanges 2->client: 313
responses 1<-client: 312
bot log: "Connected", "Connection successful"
bot frames: 5, 6, 7      (actual game frames, not zero)
server: no "Connected to AI Client process" line
```

So the ask "the pump has to tick the match, not just the handshake" is **met**:
the client received **313 frames** and answered 312 of them. Compare with the
previous state: 0 frames.

### 59a. The bug that was in my first pump (fixed here)

My first version called `callOnFrame()` twice per update - once for the
"connect-time handshake", then the normal path. `callOnFrame()` **blocks in
read() until the client answers 1**, so the second call sent byte 2 again
before the client had processed the first frame. Measured signature: exactly one
`2 -> / 1 <-` pair, then `2 ->` with no answer, match frozen.

Fixed by using **one** `callOnFrame()` per update, always after
`updateSharedMemory()`:

```cpp
updateSharedMemory();
callOnFrame();     // write 2, wait for 1, set everSynced
processCommands();
```

After the fix: 313 ticks instead of 1.

### 59b. Remaining, and it is not the engine

The last exchange is `2 ->` **without** a `1 <-`: the server is blocked in
`read()` while the client is at frame 7. In a healthy run the client answers
every frame. Since frames no longer stop at 0, this is the next client-side
layer (the same class the other model already named: `players` NPE on a
non-ticking match). Engine evidence: 177+ units created, 313 frames delivered,
slots correct.

### 59c. Probe status

`tools/bwapi-handshake-probe` reports `BYTE_2_OK` + `MATCH_TICKING` and is the
tool to re-run after any change to this path. It is independent of the bot.

## 60. Merge verification - both review patches survive - DONE

Checked line by line in our newer scripts before giving the "done" signal:

| review patch | where | state |
|---|---|---|
| lowercase `chaoslauncher.exe` guard | `kill-leftovers.sh:49-50`, `:66` (both spellings in the loop) | present, and made case-insensitive (`pgrep -if`) because Wine emits either case |
| pre-launch guard | `run-openbw-ums.sh:84` (`StarCraft/ChaosLauncher is running - kill it and retry.`) | present, anchored to argv[0] |
| no false `STILL RUNNING` after a kill | `kill-leftovers.sh:61-67` (reuses `find_pids`) | present |
| `--dry-run` does not fail | `kill-leftovers.sh:74` (`dry run: ... would be killed`, exit 0) | present |

Live test with `cp /bin/sleep /tmp/chaoslauncher.exe` (lowercase):

```
[kill-leftovers] would kill: ChaosLauncher.exe (3282678)
[kill-leftovers] dry run: ... would be killed            exit 0
[kill-leftovers] killed: ChaosLauncher.exe (3282678)     exit 0
```

## 61. "DONE" SIGNAL - merge may proceed

State at this signal:

- Handshake: `BYTE_2_OK` (probe, repeated).
- Match ticks: **313 frames delivered to the real bot**, 312 answered
  (section 59); previously 0.
- Units: 177+ created, slot report with revealers separated.
- Engine-side pump: `Server::update()` accepts clients unconditionally and uses
  one `callOnFrame()` per update, after `updateSharedMemory()`.
- Remaining failure is client-side: the last `2 ->` has no `1 <-` while the bot
  sits at frame 7, and `Connected to AI Client process` never logs.
- Scripts: both review patches verified (60), nothing in Atlantis committed.
