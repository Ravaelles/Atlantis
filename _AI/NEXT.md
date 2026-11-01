# TODO — Atlantis (open issues)

Single source of truth for open work. Direction: `_AI/00-GOAL-APPROACH.md`;
operational learnings: `_AI/NOTES.md`. Per-game evidence lives in the
gitignored `_AI/WINE-MACRO-DATUMS.md`; this backlog keeps current conclusions
and open questions (CONVENTIONS §26). Production V2 migration docs are deleted
(the system is live by default); remainders are #146 (resite chain) and #147
(stateless demand).

## Phases (the quarter)

- [x] **0 — Foundations.** Bounded headless loop, fast suite + frozen ArchUnit
  store, Java 1.8, twins + query tooling. Met.
- [ ] **1 — Production correctness (top priority).** The opening plays itself,
  every issuance attributable. Substantially met; remaining #95, #96.
- [ ] **2 — Decision board completion.** Per-gate legacy deletions, one game of
  proof each; exit: one issuer, one log. (Next steps 1–2, #91.)
- [ ] **3 — Combat cluster.** Structure done; behavior pending live proof.
  Blocked on #94 (no enemy on OpenBW).
- [x] **4 — Placement cutover.** Catalogue default since 2026-11-01
  (A/B III: 0 cancels vs 4214; confirming game 20261031_124722: 0 cancels),
  legacy planner in `src-old/`. Remainder: resite chain (#146).
- [x] **5 — Production V2 LIVE.** V2 is the default (no OFF path); dynamic
  commanders deleted. Open gaps ride their own items: expansion siting
  (#150), dual-authored Pylon demand (#144), stateless demand (#147).
- [ ] **6 — Measurement and learning.** Scenario battery + parameter search;
  exit: an unattended tuning loop that improves a frozen table (#185).
- [ ] **7 — Expansion.** Second goal vocabulary, sweeps vs real bots. Nothing
  starts early.

Work rules: never batch rewrites; never gate commits on full games; never
trade a measurement for an explanation (§3a).

## Current owner priorities
0. Mission-driven command (#232 - one named Mission owns the cluster;
   posture/tactic/approach derive from it; blocked missions downgrade
   explicitly, never silently).
1. Decision reasons in CherryVis (#214 - the army dies without a trace).
2. Fast, self-terminating OpenBW sims (§17).
3. OpenBW/E2E testing (`IDEA-E2E-TESTS.md` §3.5).

## Process (normative)

- **#232 — Mission-driven command (priority 0).** Full spec:
  `_AI/MISSION-DRIVEN-COMMAND.md`. One named Mission owns the cluster
  (HOLD_MAIN / HOLD_MAIN_CHOKE / HOLD_NATURAL_CHOKE / STAGE_MIDDLE /
  HOLD_MIDDLE / ATTACK_ENEMY_BASE); posture, tactic, approach and
  behaviors derive from it instead of reading live state independently.
  Technical fallbacks report upward and force re-decision; blocked
  missions downgrade explicitly, never silently. Slices: shadow decider
  + twin → divergence measurement → one mission enforced at a time →
  fallbacks report upward → delete shadowed deciders. Each slice its
  own commit, suite green, game proof where named. Includes the
  post-mortem of how command scattered (7 mechanisms) and proposed
  process rules P1–P7.
  SLICES 1a/1b LANDED 2026-10-30/31 (`ArmyMission` vocabulary + pure
  `decide` + `ShadowTwin`, sampled every frame from `BattleCommander`,
  steering nothing; `ArmyMissionTest` table pins + `ArmyMissionShadowTest`
  wiring pins). SLICE 2 DATUM 2026-11-01 (game 20261030_202204, retained):
  posture flaps ATTACK/DEFEND 40+ times f13981-f18085 with the army ~90
  tiles out and no contact (CLUSTER foe -1 in 130/184), 417
  FORMATION_ORDER rows in the same window (reform every ~10 frames),
  shadow mirrors the flap 1:1 (it reads posture - congruence by
  construction, not an independent signal). Worst offender: the flap
  itself (indecision - no mission sticks for 4000 frames while the line
  mills around unseen). Enforcement order per spec: HOLD_MAIN_CHOKE
  first (forbid the out-march, downgrade blocked attacks).
  SLICE 3a LANDED 2026-11-01 (code + world pin, suite green; live proof
  OPEN): `MissionHold` seam + `holdForMission` gate in `act()` (FIGHT,
  no target, mission forbids leaving) veto the ghost-creep and
  collapse-march legs and hold the choke standoff instead, twinning
  change-only `MISSION_ENFORCED` (+300f heartbeat);
  `HoldMainChokeEnforcementTest` 3/3 (veto holds, control marches,
  one row per frame). PROOF GAME 20261101_151428 (TauCross PvZ,
  VICTORY f9090, supply-50 exit, via `atlantis.sh` headless): trigger
  NEVER fired - min dChoke 12, shadow only HOLD_MAIN/ATTACK_ENEMY_BASE.
  The line never dresses within AT_STATION of the choke in this
  vehicle, consistent with the 8/94 choke-focus census. The veto is
  live-safe (game won, twins flow) but unproven; needs a melee-choke
  pressure vehicle, not another macro.

- Every item has a **stable number** (`#1`, `#2`, ...). Numbers are never
  reused and never renumbered, so an old commit or chat answer can always be
  mapped back to the item it referred to.
- **When an item is closed, delete its line from this file** (do not move it
  to a "done" section — the git history is the archive) and say in the commit
  message `Closes #<n>` with the reason it is actually done (test result,
  game-run id, ArchUnit output).
- New items are appended with the next free number, grouped by area.
- Work proceeds in any order; prefer items that unblock the most downstream
  work, not the easiest ones.
- One logical change per commit, per `_AI/CONVENTIONS.md` §6.
- An item is only closed when it is **verified by execution** (build, test
  run, or game run), not when the code merely compiles.

## Next steps (ordered - this section survives context compression)

1. **Retreat doctrine live proof** (#204, landed + pinned): the next quiet
   game proves the retreat ladder in combat.
2. **Per-gate legacy deletions**, one game of proof each: no candidate yet.
   Deletions wait for routine feeder coverage; board-invisible production
   (Forge, cannons) is capability, not duplication. Latest verdict: no routine
   feeder coverage; detailed tally and re-sweeps are in
   `_AI/WINE-MACRO-DATUMS.md` §1–§2.
3. **Decision absorption** (#91): the board takes eligible decision sites one
   at a time. Landed: worker Core veto, enemy-race naming, Stargate branches,
   wall-in/cannon/Forge demand. Robotics/observer demand and the gateway
   threshold thicket stay with their current owners until each has game proof.
4. **Pylon closure:** Wine run with the current jar for the first-Pylon proof
   (#84); corner-stall game for doctrine-vs-leash (#81). OpenBW control: the
   first Pylon warps in every retained run - the pipeline is healthy, Wine's
   failure is builder-side.
5. **Game-proof backlog** (needs a quiet run): 700 s+ rich run (#71), L2/L3
   (#85), builder-variance leftovers (#74).
6. **Second goal vocabulary** (Terran or midgame) triggers the `goals/` split;
   until then no new packages - the vocabulary grows on owner direction.
7. **Live-proof queue** (one quiet game at a time, never alongside the suite):
   #194 >10-Dragoon ban instance, #188 blocking
   geometry, #197 trapped-Dragoon break-out, #200 Zerg
   main hold, undetected-target silence (needs a cloaked foe). Owner
   2026-10-31: no opportunistic macros for rare shapes. Owner 2026-11-01:
   the agent may run Wine games headless on its own call and choose the
   vehicle freely (lurker-with-gas, melee-choke, island/UMS queue).
8. **Owner data before live verdicts:** #202 (minerals at the stall, dominant
   twin rows, Gateway count/state), #96 (painter labels + missions game),
   #34 (UMS opponent map).
9. **Long-term ordering:** #45 deletion order, rewrite order #144-#147, #8 V2
   LIVE runs, stages E–J below.
11. **Sweep remainder:** executable slices land one commit each;
   game-proof and owner-data shapes wait in steps 7-8. #156 parked to
   2026-11-09; #144–#147 wait on parity runs (review 2026-11-09); #160/#185
   wait on K-of-N data (review 2026-11-09).

## Goal board track (active)

- **#91 — GoalBoard: ranked arbitration without OpenBW.** Pure board
  (`atlantis.decisions.board`, zero engine imports: `BoardState` + ladder +
  in-flight commitment with emergency break); domain vocabulary next door in
  `decisions.goals.protoss` (CONVENTIONS §21). Consumers live: advisor,
  emergency-only worker driver, feeder (supply/gateway/core/assimilator +
  sustained army rows when the pumps go silent), wallet (price classes,
  funded subset, fail-open at the single construction door), commitment
  (`top` gets the queue + construction in-flight set). No new arbitration
  gate in legacy producers, ever (ratchet). Next: per-gate deletions.
  The board is the Strategist half of #45 without the timeline scheduler
  (deferred: every measured starvation was a priority inversion).

## Test health
Do not store suite counts here; measure with `scripts/run-tests.sh` when needed.
Before cleaning `out/`, inspect it: it can contain archived game logs as well
as build output. See `_AI/NOTES.md` for runner and ArchUnit hygiene.

- FirstPylonTiming/FirstGatewayTiming superseded and deleted 2026-11-01
  (30-issue package): they drive `produceBuilding` directly, bypassing the
  V2 engine that feeds the dispatcher chain live - green only with leaked
  engine state, red solo. Same behavior is pinned through the live flow:
  site pick-ahead by `V2EarlyBuilderDispatchTest` (headroom bank window),
  warp timing by `SimOpeningToFiftyTest` goldens (Pylon f1980, Gateway
  f3720), bank discipline by the bank-cap assert.

- **#211** The acceptance tier is red at HEAD `2167421e7` (18 of 294 failing,
  measured 2026-10-26): CatalogueSiteAdapter 5, RequestBuildingNear 3,
  ProtossDynamicUnitProduction 2, ProtossDragoonProduction 2,
  PostureTrappedArmy 2, AUnit 2, Queue3 1, CountInQueue 1 - the same per-class
  set as the `0b2198f29` baseline, so recent work regressed nothing.
  Runner-integrity holes closed (`3e7136a8e`). Open: whoever owns the 18
  re-runs the tier until green; `run-full-tests.sh` stays red until then
  and that is honest. MEGA-DATUM 2026-10-28: `run-acceptance-tests.sh`
  at HEAD = 259 ok / 22 failed; the 6 classes outside #211's set
  (Queue3+1, GasTileIsGeyserTile 2, Queue2 1, QueueInProgressInvariant 1,
  ResourcesReserved 1, RequestBuildingNear delta 3->1) re-run on a pristine
  worktree at `c3103aeb2` fail IDENTICALLY (8/8 same tests) - pre-existing,
  zero regression from this session. (RequestBuildingNear 3->1 healed
  between 2167421e7 and c3103aeb2, before this session - not claimed.)
   DATUM 2026-10-29: two more acceptance failures verified pre-existing
   on the pre-Phase-4 tree (identical stacks): DynamicProductionCommanderTest
   NPE (null raceSpecific factory when not Protoss) and CommanderTest ctor -
   both pin dead legacy commanders, die with Phase 4. Not regressions.
   DATUM 2026-11-01 (#211 triage): `DynamicProductionCommanderTest` deleted
   (pins a Terran world on Protoss-only factories - unreachable live since
   the bot plays Protoss-only; the commander itself stays for #91);
   `CommanderTest` declares Protoss (race rule, same NPE root);
   `RequestBuildingNear` x3 fixed solo (catalogue success path now names
   "OK" like the legacy path did - behavior was already right, only the
   twin was stale); placement NPEs fixed (`TooCloseToMineralsOrGeyser`
   null-game guard, 9/9). Remainder mapped, not touched: AUnit x2
   (mission-default semantics -> #232 slices), ProtossDynamicUnit x2
   (gateway thicket -> #91 game proof), QueueInProgressInvariant +
   ResourcesReserved (legacy queue semantics -> #147 deletion wave).
   RE-TRIAGE 2026-11-01 (post-migration, solo runs): ProtossDynamic*Test
   green solo (3/3 + 1/1) - suite-only reds, i.e. the order-flake
   mechanism, not product. QueueInProgressInvariant + ResourcesReserved
   FAIL SOLO (2 failed) on Queue.refresh()/order-status paths this
   session never touched (grep-verified: no findPositionForNewBuilding,
   resite, adopt, refresh-position or validatePosition calls) -
   confirmed pre-existing, still #147's deletion wave. AUnit x2 FAIL
   SOLO on squadless-unit mission defaults (isMissionAttack true->false)
   - real semantic drift toward #232, needs the mission-default slice,
   not a pin tweak.
   DATUM 2026-11-01 (time-budget stage): catalogue-flip fallout, fixed -
   `PlacementSupportTest` + 4 `PlacementPlannerTest` NPEs
   (`TooCloseToMineralsOrGeyser` reads live supply with no game running).
   Same contract as the standing-cover check: no game, no crowding
   (`Atlantis.game() == null` -> false, live unchanged). 9/9 green.
   DATUM 2026-11-01 (same run, isolated e2e): CatalogueSiteAdapter (was 5),
   ProtossDragoonProduction (was 2), PostureTrappedArmy (was 2), Queue3,
   CountInQueue, Queue2, GasTileIsGeyserTile, CommanderTest green - the
   remaining acceptance reds are RequestBuildingNear (3), AUnit (2),
   ProtossDynamicUnitProduction (2), QueueInProgressInvariant,
   ResourcesReserved, DynamicProductionCommander. Full triage rides the
   30-issue package.
   DATUM 2026-11-01 (audit v4, full suite on the resite HEAD): EXIT 0,
   budget PASS (only mega `SimOpeningToFiftyTest` 32.9 s listed, within
   40 s; mega count 2/3), suite wall ~224 s, 2172 ok / 15 failed across
   10 classes (BusyGateway, ArchBoundary, RequestBuildingNear,
   ProtossDynamicCombat, ProtossDragoon x2, AUnit, GasTileIsGeyser,
   CountInQueue, Queue3) - the known pre-existing set, zero
   resite-related failures. Fast gate + resite pins green solo.

## Architecture migration — stages E–J

- **E · #3** Move remaining `FoggedUnit` readers to `UnitSnapshot` only after
  World owns lifecycle; current readers are battle-track dependencies and wait
  for E/H.
- **F · caches** Complete: `UnitIndex` owns frozen per-frame lists; `Select`
  query caches and unexplained TTLs were removed. Historical inventory:
  `DOCS/SELECT-CACHES.md`.
- **G · #8 race strategies** `ProductionRacePolicy` + three race policies are
  landed and live-proven. V2 parity CLOSED 2026-11-01 (30-issue package):
  legacy finder vs catalogue on the same stride-60 opening warp identical
  rows at identical frames (Pylon f1980, Gateway f3720...) - siting is at
  parity, early warp is not pull-forward. Pinned by the goldens
  (`SimOpeningToFiftyTest`); the A/B probe was throwaway, not kept.
- **H · #9–#11** `A`, `Selection`, and `AUnit` have partial seam extractions.
  Continue only when a clear responsibility seam appears; cross-context readers
  stay until their dependency is fixed.
- **I · #12** Move packages to bounded contexts per
  `DOCS/ARCHITECTURE-CONTEXT-MAP.md`, mechanically and per context, after F–H.
- **J · #13** Reduce the frozen ArchUnit store to zero before making rules hard.
  Structural violations wait on E/H; information violations wait on G. Never
  move violations between rules. The authoritative gate is
  `scripts/run-architecture-tests.sh`; details: `_AI/work-orders/WO-13-mechanical-archunit-sweep.md`.

## Needs game evidence

- **#150** Construction-status visibility and stalled warps: opening gate,
  cid/oid twins, resite/adopt guards, builderless cleanup, and a sim-pinned
 2400f age cap are in place. Open: owner visual check of PREPARING, live
  overlap proof, and builder-dead freeze proof (`WO-150b`; HP trail wired).
  No retained game had shown FROZEN_WARP or recurrence of the old
  silent-stall class - until 20261102_003832 below. → details: `WINE-MACRO-DATUMS.md` §3. FROZEN DEMOTE 2026-10-30:
  the builder-dead freeze proof is the mechanism, not just the twin - a
  static-hp entity past 2400f with nobody working it demotes to
  NOT_STARTED (game 184529 cid 10: 8-minute IN_PROGRESS Pylon froze
  supply at 49/49). SIM CLOSED-LOOP 2026-10-30
  (`StalledWarpRecoverySimTest`): a frozen mid-warp Pylon re-arms only
  once the frozen branch proves it (2400f age + static hp) and a new
  warp completes after release. Open: live recovery proof.
  DATUM 2026-11-01 (game 010715, V2 LIVE VICTORY): demote->re-arm->warp
  RECOVERED live - Gateway cid3/oid98 DEMOTE_STALLED f5447
  (stalledAge=2400f, [112,17]) then BUILDING_STARTED f5671 (+224f,
  [111,17] builder133, same oid) - the stalled demand re-arms and warps
  instead of dying silent. HP-trail + builder-dead freeze proof (WO-150b)
  still open; the recovery half is now live-proven.
   DATUM 2026-11-02 (game 20261102_003832, loss f15541): silent-stall
   RECURRENCE, pre-warp or frozen-warp undetermined from twins.
   CyberneticsC STARTED f5154 [94,113], then 0 completions / 0 cancels /
   0 demotes in ~10k frames (17 further dispatches deduped to the same
   stuck row - 1 start all game). Zero Dragoons all game while gas
   banked to 1008; zealot-only army overrun f14274+. STALL rows park
   one builder at [94,117] f6107-6355 (still 253-501f), but no
   BUILDER_STATE twin (flag off) names the attachment - the demote
   gate (2400f + static hp + nobody working) never fires, consistent
   with a builder ATTACHED-but-useless loophole. Needs a BuilderLog
   game to name the mechanism; HP-trail (WO-150b) would distinguish
   frozen-warp from never-arrived.
   DATUM 2026-10-29 (game 184529, BuilderLog): 1 Nexus construction from
   3869 attempts / 388 reached, 0 warps, 0 cancels - one builder (#158), one tile
   ([91,115]), canBuild=false in all flag combos while affordable/explored/powered.
   Nothing exits an engine-refused tile (no re-site, no cancel). Refused-tile
   memory or exact base-location siting is the fix - #145 territory.

- **#151** Dynamic demand re-requests a proximity-covered tile: the [119,12]
  storm comes from the legacy finder chain, which lacks a standing-cover check.
  The spiral guard is a separate funnel; no local patch. The fix and acceptance
  test ride #145. Latest FFA: 8 repetitions in the affected game, none elsewhere;
  ENGINE DATUM 2026-10-28 (live 003802, V2 LIVE, 171 BUILD_BREAKDOWN rows on
  the same [119,12] Nexus site): engine `canBuildHere` noBuilder=false AND
  withBuilder=false while explored=12/12, unitsInRect=0 (not even minerals),
  terrainOnly=ok, hasPower=true, canAfford, distToBuild 1.8-2.0, canBuild*
  all false in every flag combination. The bot's own tile math says buildable;
  the engine refuses - so the finder deterministically re-serves an illegal
  tile and Refresh-Nexus chases it forever (3000+ BUILD_REFUSED_WHY, supply
  parked at 49). RESOLVED SAME GAME by relocation: the expansion warped
  f16700 at [103,37] supply 49 (not the cursed tile), died, rebuilt f20715
  same tile. Fix needs engine-validated siting or refused-tile memory,
  both in #145's scope - deliberately untouched here.
  DATUM 2026-11-01 (30-issue package): the #145.1 resite covers the
  standing-cover half of this (sibling-covered site resites at
  proximity-finish time instead of absorbing or looping); the
  engine-refusal half (tile reads buildable, engine says no) still
  wants refused-tile memory + a live game showing no recurrence.
  detailed ledger adjudication is archived in `WINE-MACRO-DATUMS.md` §3.
  MECHANISM LANDED 2026-11-01 (`RefusedTiles`, 2400f TTL): IssueBuildOrder
  notes engine-HARD refusals (builder present and still, engine gate says
  no - move-timing transients excluded); the catalogue declines refused
  tiles with its own twin reason instead of re-serving them. Self-
  correcting (transients age out, hard ones re-add). Pinned
  `refusedTileIsDeclinedWithItsOwnReason` + fast gate green. Live
  no-recurrence proof queued (needs an engine-refusal game).
  DATUM 2026-11-01 (94 retained runs): explicit BUILD_REFUSED rows are
  rare and early-era (11 games, max 31 in 151521, all pre-V2 openings) -
  no new repeat-tile chase exhibit; the documented exhibits (184529 Nexus
  3869 attempts, 003802 [119,12] 171 BUILD_BREAKDOWN) stand alone.

- **#235** Builder double-link deadlock (owner live game 2026-11-01 vs
  Steamhammer, TauCross, loss f7296): a Gateway held builder #27 from
  f1917 and never warped while a Pylon took the same builder at f3279 -
  neither construction ever built, #Move forever, zero #Build, zero
  cancels, supply frozen at the first Pylon. Root cause in code:
  `Construction.setBuilder` never unlinked the old construction, so
  two rows named one worker and every reader disagreed forever (the
  observed Manager↔Waiting 1-frame flap is
  `travelWhenReady` alternating on the disagreement).
  FIXED 2026-11-01: exclusive ownership enforced at the seam (old row
  unlinked on handover, re-arms through the normal builderless path -
  `ConstructionStatusChanger` re-arms builderless rows, overdue
  re-arms the rest), pinned red-first
  (`BuilderHandoverUnlinksOldConstructionTest`) + fast gate green.
  Open: live no-recurrence proof (a game that hands a busy builder to
  a new row and both proceed - the 20261101_165023 pressure vehicle
  is the natural witness, unplayed since the fix).

- **#236** Unreachable build sites (owner live report 2026-11-01): the
  first Pylon sited ~35 tiles from the main in a region with no land
  path (UMS `P_v_Lurkers.scx` via owner's `Main.java` map pick, also
  seen on `7th_rav.scx` as Pylon [105,68] + Nexus [121,114] with zero
  `CATALOGUE_SITE` rows). Two holes combined: (1) neither siting path
  checked ground connectivity - the legacy spiral's per-ring
  `groundDistanceTo` guard falls back to Euclidean when BWEM has no
  areas (UMS), and the catalogue ranker (`BuildLocationRanker`: travel
  + exit only, `ANYWHERE` centre unused) never checked it at all;
  (2) a corner base + boundary/mineral refusals walk the search far
  out (spiral radius up to 66). FIXED 2026-11-01: one final
  `ReachableTiles.between(main, site)` check at the end of each path,
  once per search (the costly query, after all cheap gates) -
  `CatalogueSiteAdapter.propose` declines `unreachable` to legacy,
  `FindPosition.findForBuildingRaw` declines `SITE_UNREACHABLE` to
  null; skipped when no base stands yet. Pinned
  (`disconnectedRegionDeclinesToLegacy`) + 48/48 placement pins
  green. Open: live proof on a real map (UMS maps have no BWEM areas
  by construction - expect declines there, not placements); the "why
  35 tiles" half (near-ring refusals pushing the search out) still
  needs a `BuilderLog=1` game with `CATALOGUE_SITE`/`PylonSite` rows.

- **#152** Dragoon kiting: instrumentation, targeting, escape and tuner are in
  place; the latency ladder improved hits while retaining wins. Open: 0–2-hit
  goal via #186, wall-aware steps after production, macro proof with attributable
  foes, and K-of-N tuner efficacy.

- **#149** Causeless early exit: one occurrence was the hoard tripwire by design;
  the other clean, enemyless exits have no bot-side cause. Engine-side termination
  remains open; keep `GAME_END` as witness. No recent recurrence: 9 retained
  games 20261027_183253-20261028_004959 end as losses at various frames or
  28800 time-ends, none clean-and-enemyless.
  DATUM 2026-11-01 (94 retained runs): 2 clean enemyless early exits, both
  without foe rows or CLUSTER rows - 20261027_115321 f4500 loss and
  20261027_140305 f1920 WIN (supply 10). The f1920 win is the cleanest
  enemyless witness on record; engine-side termination still open, no
  bot-side cause to fix.

- **#71** Readiness gate: still lacks the deciding 700 s+, one-base, low-bank
  run. Wine's default supply horizon prevents reaching it; a per-run horizon is
  required. No blind tuning.
  → details: `WINE-MACRO-DATUMS.md` §4
   DATUM 2026-10-29 (game 184529): 872 in-game seconds one-base vehicle
   delivered, but the bank ran RICH (7516 max) - the low-bank deciding run
   still open.
   DATUM 2026-11-01 (94 retained runs, BUILDINGS+GAME_END): 32 one-base
   games reach f16800+; end-bank median 4577, only 2 wins among them.
   The low-bank long vehicle EXISTS now: game 20261029_154822 (Destination,
   loss f24095 ~1000 s, 1 Nexus, end bank 15 gas 420, 68/111 deaths are
   probes post-f18000 with 3 Dragoons/2 Zealots lost late) - one base,
   700 s+, low bank. Readiness-gate adjudication on this run is owner-side;
   no tuning without the verdict.

- **#81** Corner-stall Pylon search: evidence favors tile doctrine, not leash
  relaxation; corner Pylons land and supply unblocks. Last-resort ladder is
  pinned but has not fired in retained games.
  DATUM 2026-11-01 (94 retained runs, BUILDINGS): 624 Pylons warped, 238
  near-edge (x<15 or y<15) - corner/edge Pylons land in bulk, supply
  unblocks. Tile doctrine holds; the stall that would need the leash
  still never presents.

- **#84** Wine Pylon issuance: observed stalls self-recover; pipeline audits
  remain healthy. `WarpStartedTwinTest` covers the builder path and siteless
  CANCELs carry cid/oid/reason. Open: verify STARTED-twin naming on a legacy-
  minted warp; the remaining re-request-door gap is per-warp, not per-game.
  DATUM 2026-10-28: 0 legacy-minted warps in 4 audited V2 LIVE games
  (003802, 000314, 183253, 004959 - every warp carries a V2 `At 0` order),
  so the shape is unobservable in this mode; needs a non-V2 game or the
  legacy path to mint one.
  DATUM 2026-11-01 (retained legacy games): the vehicle EXISTS - 16
  pre-V2 runs carry no V2_DISPATCH rows (10/24-10/27: 173811, 190446,
  194356, 101834, 103451, 134423, 150342, 151521, 161816, 170444, 122015,
  122342, 133232, 203339, 222936, 121013). Legacy-minted STARTED naming
  VERIFIED: 173811 Pylon cid1/oid1 [112,29] builder13, Gateway cid45/oid2,
  Core cid47/oid5; 101834 9 warps with cid/oid/tile/builder; 122342 3
  warps + CONSTRUCTION_CANCEL. Every legacy STARTED row carries
  cid+oid+tile+builder+supply+minerals - the naming gap is closed. Open:
  only the per-warp re-request-door gap.
  → details: `WINE-MACRO-DATUMS.md` §5

- **#74** Wine builder variance: observed stalls recover; alternating handoff
  and 1.4-tile parking remain unobserved. Builder twins require `BuilderLog`,
  so flag-off games cannot settle this.
  DATUM 2026-10-28 (game 010715, BuilderLog ON, VICTORY): multi-builder
  sites are sequential replacement over minutes (Pylon 109,17: 6 builders
  f13950-15030, all IN_PROGRESS; no A-B-A alternation), 1.4-tile parking
  present but rare (17 STARTING rows on 2 sites). Rapid alternation still
  unobserved.
   DATUM 2026-10-29 (game 184529, BuilderLog): single-builder persistence -
   Nexus 3869 attempts / 388 reached on ONE builder (#158) on ONE tile, no handoff;
   Pylons sequential across 2 builders (replacement, not A-B-A). STUCK_WORKER
   1105 rows are one parked scout probe, not a builder stall. 1.4-tile parking
   unobserved (Nexus parks at 1.7).
   DATUM 2026-11-01 (94 retained runs): 32 games carry builder twins
   (BUILDER_STATE/BuilderLog), 62 are flag-off and cannot settle variance.
   No new handoff/parking shape in the twinned set beyond the two
   documented games; the unobservable majority stays unobservable.
   DATUM 2026-11-01 (BUILDER_STATE streak census, 32 twinned games,
   13364 rows): 16 SUSTAINED 1.4-parks (same builder+site, >=10
   consecutive rows at groundDist 1.2-1.6, never constructing).
   Strongest: game 130749 probe #124 holds Nexus site [91,115]
   motionless f6690-24180 (~17k frames, 580/584 rows still, all
   STARTING, 0 construction) - the game ends with the builder parked;
   195338 probe #138 same shape f12000-16950 (159/165 still); 200344
   probe #121 60 rows at the [119,12] storm tile. NO alternation in any
   streak (one builder per site throughout) - handoff unobserved in all
   32 games; parking proven common, not rare. Open: why it never issues
   (parking vs engine-refused tile - [91,115]/[119,12] smell refused).
  → details: `WINE-MACRO-DATUMS.md` §6

- **#85** Armor research is independently gated from weapons L3 (stub-pinned);
  current thresholds are owner tuning, not a defect. Singularity fires live, but
  post-Forge L3 and a longer research window remain unobserved.
  DATUM 2026-10-28: zero Forge warps in 9 retained games (183253 through
  010715) - the whole Forge line never presents in these openings; needs a
  build that takes one.
   DATUM 2026-10-29 (game 184529): detection goals fire once at f20940 on
   Hydra-Den evidence (correct trigger, 5 frames before the end) - Forge/Robo/Obs
   never warp all game. Still needs a mid-game trigger.
   DATUM 2026-11-01 (94 retained runs, BUILDINGS): 21 runs warp a Forge
   (10586-26350), only ONE warps an Observatory (115405 f21433, with 3
   Corsairs), ZERO warp Robotics_Facility / Observer / Cannon /
   Citadel / Archives anywhere. The Forge line presents; the full
   detection chain still never completes in any retained game.

- **#82** Terran off-site completion may leak (unobserved): the bot plays
  Protoss-only (D8), so the shape cannot appear. DATUM 2026-10-28: same
  class as #84 - 11 V2 LIVE games audited, Terran never played, zero
  off-site shapes; unobservable until a Terran game happens. Needs a game
  showing it. No blind fix.
  DATUM 2026-11-01 (94 retained runs): ZERO games mention Terran units
  anywhere in EVENTS - the shape is unobservable in the full tree, not
  just the 11 audited games.

- **#92** Second Gateway lateness is intermittent; no blind patch. The stable
  pattern is a +1-supply lag on #2/#3, not a multi-minute delay; all three
  Gateways warped in audited games. DATUM 2026-10-28 (10 retained games):
  GW#1 f1963-2157 supply 10-11, GW#2 f2502-3458 supply 12-17 everywhere -
  no multi-minute delay in any game; GW#3+ lands only in long games and is
  now V2-dynamic (proved 003802, see #192).
  → details: `WINE-MACRO-DATUMS.md` §7
  DATUM 2026-11-01 (94 retained runs, BUILDING_STARTED twins): 78 runs
  warp 2+ Gateways; GW#1->GW#2 gap median 1065f, p90 2400f, max 5909f -
  exactly one multi-minute outlier (20261031_213009: GW#1 s15 f3330 ->
  GW#2 s55 f9239), the gas-starved game (138 cancelled Assimilators, zero
  gas) whose stall #234's gas branch + gate3 latch have since closed.
  Intermittent stands; no blind patch.

- **#94** OpenBW combat remains blocked by engine-side enemy visibility: on
  `1Drag` the enemy deals hits but is invisible to the client. Hosting/handshake
  plumbing is fixed; Wine remains the duel-proof vehicle.

## Owner- or upstream-blocked

- **#97** Own map-analysis library (`redesign/04_MAPLIB.md`): M0–M4 model,
  defense/drop spots and anchors are implemented. Wall siting is proven; live
  HOLD proof still needs an attack onto the wall (sim pins cover funnel raids
  and ranged pressers). M5 extraction is mechanically ready; timing is owner-
  gated.

- **#95** Reserved-250 ghost: one deadlock showed a 250 reserve at supply 4,
  bank 74, with nothing READY; no author found and no recurrence. Correlate
  `reserved` with `DECISIONS.log` if it returns; `ORDER_NOT_READY` has a price-32
  visibility floor. → details: `WINE-MACRO-DATUMS.md` §8
  DATUM 2026-11-01 (94 retained runs): no `reserved:250` row anywhere
  (seen values cluster at 0/100/125/150/175/200/300) - still unobserved,
  nothing to correlate.

- **#96** Wine verification: owner-side checks remain for painter labels and a
  missions game. Macro artifacts and replay naming are verified; UMS duels
  intentionally collect EVENTS + WINE_LOG. Replay saving remains flaky
  engine-side; CherryVis is opt-in via `CHERRYVIS_ENABLED=true`
  (booleans read `true`/`1` on, `false`/`0` off, any case; garbage logs once
  and keeps the default - `Env.parseBool`, landed 2026-10-25). On docker runs
  use `scripts/run-scbw-with-cvis.sh` (scbw.play has no --cvis flag).

- **#34** Real opponent on OpenBW needs a scripted UMS map with a computer slot
  (owner-authored; tier stays owner-run). `USE_MAP_SETTINGS` probes yielded
  either a melee start with no enemy or a frame-0 exit. The sim zombie covers
  reproducible defense in-process only, never engine proof.

- **#65 / #68** CherryVis code fixes are in (types, legend, seed, writer path);
  what remains is the owner opening a real trace in the viewer.

- **#78 / #79** Replay desync is upstream (`OpenBW/openbw#27`, 2019, still
  open). The live log is truth; replays are for viewing positions only.

## Design and strategy

- **#145–#147** Rewrite order, clean-room: each lands specced, stub-pinned,
  default-flipped with a game proof, then the legacy files move to `src-old/`.
  (#144 closed by e203c6121 with the 104542 VICTORY proof. WO-144 supply-gate
  roster complete on the new jar: 183253 (long) + 190104 (short) + 192937
  (full length, f28800) - duplicate-free openings everywhere, no warp at
  cap, Pylon@8 at exactly 8 in all three. Authorship races both ways
  (legacy built Gateway#1 in 183253, V2 in 190104/192937) - mixed operation
  in LIVE mode stands, it is WO-147's question. Deletions per the WO order
  are the owner's call. OWNER ORDER 2026-11-01: full migration GO -
  cut the old production and the old placement, then the full decision
  system, testable throughout. Slice plan (one commit each, suite green,
  game proof where named):
  Phase A (placement, #146) LANDED A1 (construction seam catalogue-direct),
  A2 (base seam + scope lift), A2b/c? no - A3 (dead Terran depot +
  nextPosition), A4a (StatusChanger seam), A4b (modifier anchoring),
  A4c (pure-catalogue base + fallback-chain deletion), A4d (dead
  findPositionForNew), A4e (dead bunker path + modifier package),
  A4f (dead bunker/creep/Zerg branches), A4g (precedence + dead
  modifierFor). OPEN: sited-fallback removal ATTEMPTED + REVERTED
  (load-bearing - waits B-phase sited ownership); APositionFinder
  class deletion (blocked on the same + cannon path); cannon
  catalogue migration (no cannon vehicle in 98 games).
  Phase B (demand, #147): B0 flow map + B1a/B1b dead-race dispatch
  deleted. NEXT: B1c first live producer family (needs per-gate game
  proof) or B2 reader migration; owner call on family order.
  #232 slice 3a LANDED (enforcement gate + twin + pins; live proof
  OPEN - trigger never fired). #232 slice 3b LANDED 2026-11-01
  (HOLD_NATURAL_CHOKE forbids the out-march with its own standoff,
  mirror-pinned; live proof OPEN like 3a). #232 slice 4 LANDED 2026-11-01
  (fallbacks report upward, sim-pinned per fallback, enforcement
  deferred to its mission per spec): ghost give-ups
  (`GHOST_GIVE_UP`: no-ghost/lost-far/wall-grind), collapse latch
  transitions (`COLLAPSE_LATCH`: latched/released/dropped), retreat
  floor overrides (`RETREAT_FLOOR`: clamped/ahead-hold, per rung),
  no-contact hold ownership (`NO_CONTACT_HOLD`: held/throttled-stand).
  #201 Corsair driver LANDED (sim/world
  pins; game proof needs a Stargate game). #203 brake twin LANDED.
  #223 V2_DETECTION_GOALS twin LANDED (attribution instrument).
  B0 LANDED 2026-11-01 + B1a/B1b dead-race dispatch deleted.
  B1c LANDED 2026-11-01 (authorship census by cid/oid join, 98 games):
  UNITS are V2-only - Zealot 0/1026 + Dragoon 0/884 legacy-door warps,
  zero exceptions. BUILDINGS are dual-authored live (Pylon 255/661,
  Gateway 120/274, CyberneticsC 38/97, Nexus 22/50, Forge 18/28,
  Assimilator 30/85 via the legacy door). Exotics (Arbiter/Reaver/
  Shuttle/DT/Archon/Scout/Carrier) have 0 warps AND 0 dispatches but
  stay - ReaverChainTest/TemplarChainTest pin them; deletion needs
  the V2-goal home first. B1-PYLON LANDED 2026-11-01: the legacy
  per-base Pylon producer (`ProducePylonNearEveryBase`, minted with no
  supply-need gating) is deleted; openings stay on BO rows, blocks on
  the V2 supply-pylon goal (pinned `blockedSupplyFeedsTopPylon`), and
  expansions on catalogue support-Pylon requests (pinned
  `unpoweredSitingQueuesSupportPylon`: unpowered siting queues a HIGH
  Pylon instead of serving the tile). Commander call removed + class
  deleted. Suite: Pylon batch 28/0. PROOF 2026-11-01 (vehicle game
  20261101_211851 TauCross PvZ, frozen jar WITH deletion, loss f8527):
  all 3 Pylons V2-dispatched on time (f1131 [99,108] 8/9, f3287
  [110,124] 15/17, f6565 [81,113] rebuild at 8/8 during collapse) -
  zero deletion-attributable gaps (the deleted producer fires only
  after f10080; game ended f8527). The f3300-6500 blocks are a
  probe-raid collapse (8 Probes die f3963-4338, minerals stranded),
  not supply-logic failure. OPEN: expansion-power half (no natural
  taken this game - support-Pylon at a natural still needs a live
  expansion game).
  B1-FORGE LANDED 2026-11-01: the V2 home (`ForgeGoal`, row-for-row
  mirror of the legacy timing table) now funds the row - `GOAL_TYPES`
  serves `forge`, pinned `hiddenThreatFeedsForgeRow` (DT evidence +
  no Forge + EnforcePylonFirst satisfied = standard-priority row; the pin
  caught a real door rule: power-needing buildings need a Pylon first).
  `ProduceForge.produce()` call + class deleted (sole caller was the
  dynamic commander). 27/0. Open: game proof (a DT-threat or timing
  game where the V2 row warps the Forge - opportunistic).
  B1-ASSIMILATOR LANDED 2026-11-01: the legacy queued gas once the Core
  was nearly done (<80 warp seconds left) - the V2 goal needed the same
  sight, so `BoardState.coreRemainingSeconds` (snapshot min over
  unfinished Cores, -1 when none) feeds `AssimilatorGoal` (standing OR
  nearly-done Core). Pinned: snapshot names 40s, feeder funds the row,
  #174710 no-second-row rewritten onto V2 inFlight (which covers
  PREPARING sites - stronger than the old next-2 cap). Legacy producer
  + class + reason plumbing deleted. 17/0. Open: game proof (gas
  timing vs legacy: Assimilator should warp as the Core lands). PROOF
  2026-11-01 (vehicle game 20261101_223235 TauCross PvZ, frozen jar
  WITH the exemption fix, loss f13200): ZERO `REFRESH_CANCEL` rows
  (was 25-39/game), 1 Assimilator warped, gas 688 at end (was 0.0).
  #237 (geyser self-block) CLOSED here and its item deleted per rule.
  B1-CORE LANDED 2026-11-01: `CyberneticsCoreGoal` (gateway standing,
  no Core) already had a feeder home (`core` served); pinned
  `gatewayExistsFeedsCoreRow` (goal-to-queue). The #58 requeue and the
  one-Core dedup are rewritten onto V2 (feeder feeds a missing Core,
  `inFlight` covers PREPARING sites - stronger than the old guards,
  which were blind to picked-early sites). `ProduceCyberneticsCore`
  call + class + dead assimilator-blocked diagnostic deleted. 14/0.
  Behavior note: V2 fires on Gateway+bank where legacy waited for
  supply/mineral gates - earlier Core possible; game proof watches
  the timing. Open: game proof (Core timing + expansion-Pylon +
  live #204 in one macro vehicle).
  PROOF 2026-11-01 (vehicle game 20261101_215027 TauCross PvZ, frozen
  jar WITH B1-Pylon/Forge/Core/Assim, WIN f8640): Core V2-dispatched
  f4296, warping f4355 (Gateway f2445 -> Core ~31 s later - healthy,
  no legacy wait needed); all 3 Pylons V2 on time; natural Nexus
  f6190 (expansion taken); Assimilator 70 requests / 33 starts / 0
  completions / gas 0.0 = #237 live hole (NOT a B1 regression -
  same siting door pre/post; since fixed + proven, item closed and
  deleted per rule). Forge untriggered (no DT evidence) -
  still open. #204: no losing fight, no floor rows - still open.
  NEXT BATCH (owner 2026-11-01: 30 more NEXT issues, headless games
  allowed): pressure-vehicle game first (one game feeds #232
  enforcement proof, #170/#171 choke proof, #200/#205 hold proof,
  #204 floor proof, #196 rescue, #188 blocking, #181 park,
  #203 brake twin + #203 attribution vehicle), then lurker hunt
  (#223 full chain + discovery twin), island vehicle (#213), long
  horizon (#192), Corsair driver fix (#201 traced), brake twin
  (#203), refused-tile memory (#151), Stage B scenarios (#186),
  retained datums (#74 parking census, #208 twin geometry, #234,
  #207), #214 blocked-vehicle note, #194 doctrine question to owner.
  B0 LANDED 2026-11-01 (read-only inventory, no code): 43 files mint
  legacy orders via AddToQueue (13 dynamic/protoss/buildings, 8
  requests/*, 6 strategy responses, rest scattered); ~20 files read
  Queue status (Have, BuildOrderOwnsGame, CancelNotStartedBases,
  ExpansionCommander, AutoProduceWorkers, Produce* guards,
  OnOurUnitCreated matching). NOBODY drains the Queue into issues -
  the legacy drain is gone (`ProductionOrdersCommander` runs only the
  V2 engine); V2 dispatcher/director is the sole issuer, consulting
  Queue counts as in-flight (`Count.*InQueue`) + BO rows as goals +
  DynamicGoals. So #147 = (B1) producers→V2 goals per family, (B2)
  readers→V2 state, (B3) delete Queue/** + order types. Each slice
  suite-green + game proof where the twin names the author.
  B2 SCOPED 2026-11-01 (read-only inventory, no code): Queue readers
  outside `orders/queue/` fall in three buckets. (a) V2's own reads -
  KEEP (`GoalBoardFeeder.inFlight`, `GoalBoardAdvisor` core-await,
  `NewConstructionRequest` funnel checks, `ProductionHealthWatcher`
  diagnostics, `Count.*InQueue` as the in-flight set). (b) Legacy
  decision reads - migrate per family, owner-gated like B1:
  `BuildOrderOwnsGame` (BO-vs-V2 ownership over notFinished rows),
  `CancelNotStartedBases` (stuck-base queue surgery),
  `ExpansionCommander` (mineral/queue-size gate),
  `AutoProduceWorkersCommander` (gateway queue scan + a dead Zerg
  Spawning-Pool/Zergling branch - Protoss-only candidate),
  `Produce*` guards (zealot next-2, critical-stuff notStarted,
  gateway ready-queue), `OnOurUnitCreated` (ready/in-progress/
  notStarted matching). (c) Dead on arrival: the Zerg-pool check in
  `AutoProduceWorkersCommander` (We.protoss-only game). Next: owner
  picks the first B2 family, then per-family slice like B1 (V2 home +
  pin + delete + game proof).
  B1a LANDED 2026-11-01 (dead Terran response dispatch deleted:
  `AsTerranUnitDiscoveredResponse`, `TerranResponseEnemyHiddenUnits`,
  `TerranStrategyResponse`, `TerranAirDefence` stub + the We.terran()
  branches in `EnemyUnitDiscoveredResponse` and
  `RaceStrategyResponseFactory`; Protoss-only D8): fast gate 1058/1058
  (2 arch advisory pre-existing). No game proof needed: dispatch
  branches the bot never takes (We.terran() is false in every game).
  B1b LANDED 2026-11-01 (same for Zerg: `AsZergUnitDiscoveredResponse`
  + `ZergStrategyResponse` deleted, We.zerg() branches pruned; enemy
  Zerg is handled by the Protoss response, which stays): fast gate
  1058/1058. No game proof needed: same untakable-branch argument.
  A4h ATTEMPTED + REVERTED 2026-11-01 (sited-request fallback removal):
  the legacy spiral fallback in `RequestBuildingNear.findExactPosition`
  is load-bearing, not dead - 3 pinned sited shapes go red without it
  (cannon/pylon at natural/third). Probe (`SitedDeclineProbeTest`,
  deleted after the answer): in stub world the catalogue answers the
  pylon ([2,48]) but correctly declines the unpowered cannon (null),
  while the legacy spiral answers both - the pylon-first-then-cannon
  chain depends on the fallback. The fallback stays until the demand
  side owns the pylon-first chain (B-phase); the twin already names
  which path answers. No code change kept.
  A4g LANDED 2026-11-01 (legacy precedence restored + dead
  `modifierFor` deleted): constraints read exact > nearTo > modifier >
  anywhere (was modifier-first since A4b - a silent reorder the legacy
  spiral never had; nearTo names the tile, modifier the class).
  `DemandModifierTest` rewritten as `ConstraintPrecedenceTest` (the
  precedence contract lives where it is enforced now). Pins 25/25 +
  fast gate 1058/1058. No new game proof: ordering fix inside the
  already-proven funnel (A4b's game stands).
  A4f LANDED 2026-11-01 (dead race-special siting deleted:
  `FindPosition` bunker + creep branches, `SpecificConstructionRequests`
  Terran/Zerg methods (now unconditional null),
  `ZergCreepColony.java`, `TerranAddonBuilder.java`; 0 bunker warps in
  98 games, We.terran()/We.zerg() false in every game, bunker path had
  no live or test callers): fast gate 1058/1058. No game proof needed:
  untakable branches + unreferenced files.
  A4e LANDED 2026-11-01 (dead bunker siting path deleted:
  `DefineExact` bunker block + `definePosition` + whole legacy
  modifier package except `ModifierAnchor` (`PositionModifier`,
  `BunkerPositionModifier`, `PositionAtMain/NaturalChoke`); the
  vocabulary constants moved to `ModifierAnchor`): 0 bunker warps in
  98 retained+proof games, no live or test callers left. Fast gate
  1058/1058. No game proof needed: uncalled code cannot fire.
  A4a DONE 2026-11-01 (StatusChanger null-site reroute). A4b IN PROGRESS:
  modifier→constraint translation (legacy @NATURAL/@MAIN wall-in orders
  anchor the catalogue neighbourhood instead of anywhere).
  A4b LANDED 2026-11-01 (modifier→constraint translation landed with
  pins+gate; answered-live half rides a wall-in vehicle - game 155312
  showed 0 funnel rows).
  A4c LANDED 2026-11-01 (pure-catalogue base siting, fallback chain
  deleted: `FindPositionForBaseNearestFree`, `NearestFreeBase`,
  `ASpecialPositionFinder`; base test rewritten to
  `BaseSitingCatalogueTest` - neighbourhood, not exact tile): pins
  25/25 + fast gate 1058/1058. GAME 20261101_160757 (TauCross PvZ,
  VICTORY f11370, supply-80 exit via `atlantis.sh` headless): 0 building
  cancels, 2 natural warps f6430/f11143 - again 0 CATALOGUE_SITE rows
  (V2-director bypasses the funnel entirely). All post-flip proof games
  agree: the legacy door fires at most once per game while V2-direct
  sites everything. The funnel is a safety net, not the pipeline -
  which is why the fallback could go: a decline retries through the
  demand (no construction minted, no churn), and the twin names every
  decline. 51x Assimilator cancels on one tile = known gas-storm shape.
  A4d LANDED 2026-11-01 (dead `findPositionForNew` overloads deleted
  from `APositionFinder` - zero production callers after A1+A4a; only
  comments + `CacheKeyTest` string mention it, `DemandModifierTest`
  pins the surviving `modifierFor`): pins 24/24 + fast gate 1058/1058
  (2 arch advisory pre-existing). No game proof needed: unreferenced
  code cannot fire (verified by grep, not by belief).
  - **#145** Catalogue-only siting: DONE (flip 2026-11-01, unconditional
    `createPlacementPlanner`, legacy planner in `src-old/`, sim on catalogue).
    Verdicts: A/B III 0 cancels (was 4214), confirming game 20261031_124722
    0 cancels; suite 1887/1887. Open remainder: (1) DONE 2026-11-01 -
    resite-not-cancel on proximity-finish with standing sibling
    (`shouldProximityFinish` resites instead of absorbing when the cover
    is a same-type sibling on overlapping - not identical - tiles and a
    live non-travelling builder is attached; exact duplicates still
    absorb, travelling builders keep the old path per #53; pinned
    `SiblingCoverResitesInsteadOfFinishingTest`), (2) DONE (re-goldened for
    the stride-60 sim, green in the mega-test), (3) chain deletion is #146.
    DATUM 2026-11-01 (audit v4, full suite on the resite HEAD): none of
    the 10 failing classes is resite/placement/proximity-related
    (BusyGateway, ArchBoundary, RequestBuildingNear, ProtossDynamicCombat,
    ProtossDragoon x2, AUnit, GasTileIsGeyser, CountInQueue, Queue3) -
    zero resite regressions; resite pins green solo + in fast gate.
  - **#234** V2 mid-game equilibrium stall (mega test `LongGameToSeventyTest`,
    14 s, milestone pipeline): gas branch landed, so Dragoons flow (14) and
    Zealots mix in on lings. The gate3 verdict went green via a 10 s busy
    latch (proven load-bearing by on/off A/B at stride 60: latched green,
    unlatched red on the same tree) because simultaneity (`gatewaysFree == 0`
    + rich bank in one update) almost never holds under coarse dispatch.
    (b) Single-base equilibrium: DECIDED 2026-11-01 save-for-expansion.
    Evidence (`PostFiftyProductionTest` table, solo): workers cap at 25 on
    one base, the bank climbs 160->430 instead of feeding a trickle, the
    Nexus warps (~f13920, bank 430->8), probes resume on the new base
    (21->23). Old shape (1 probe/1800f, bank hoarding to 849, no expansion)
    fails the rewritten test: Nexus warps after the mark AND probes resume
    after it. Bleed covers the pressured half (replacements flow there).
    Open: live proof of the pre-saturation window.
    DATUM 2026-11-01 (game 154822, one-base loss f24095): ends bank 15
    (max@warp 674) - spent, not hoarded; the single-base hoard-to-849
    shape does not reproduce here. Supports save-for-expansion (bank
    moves), pre-saturation window proof still open.
    DATUM 2026-11-01 (game 153615, VICTORY): natural warps f6254 at bank
    232 supply 34 (2nd Nexus f10759 bank 1288) - expansion fires early
    on a thin bank while production flows, the save-for-expansion shape
    live (no 849-hoard-then-expand). Still not the PostFifty window
    verbatim, but the doctrine's live half strengthens with every
    natural before the worker cap.
  - **#146** Delete `APositionFinder` after catalogue default and parity (D1).
    Exit: zero `Can't find place` across retained runs; current failure volume
    remains above that bar.
    DATUM 2026-11-01 (94 retained runs): zero literal `Can't find place`,
    but 267 SCHEDULE_REFUSED no-placement + proximity finishes (standing
    Gateway x3k, Pylon x1.6k, "bugged" Pylon/Nexus x3.4k, refresh-still-bad
    610). The absorptions are by design; the refused/never-sited remainder
    is the bar. Note: the #145.1 proximity resite rides the legacy
    `findPositionForNewBuilding` seam (same as adopt) - #146 migrates both
    to the catalogue together. Deletion stays owner-gated (WO order).
    GUARD 2026-11-01 at HEAD: `createPlacementPlanner()` unconditionally
    returns `CataloguePlacementPlanner`, legacy planner sits in `src-old/`,
    and the #145.1 proximity resite rides the same legacy
    `findPositionForNewBuilding` seam as adopt - #146 migrates both seams
    to the catalogue together. No code touched.
    A1 LANDED 2026-11-01 (`Construction.findPositionForNewBuilding`
    delegates catalogue-direct; orderless-guard + testing seam preserved
    at the seam; all 6 call sites null-safe): pins 13/13 (SiblingCover,
    RefreshCancel, StaleDuplicate, PreparingSite, WarpedOrder) + fast
    gate 1058/1058 (2 arch advisory pre-existing). GAME PROOF 20261101_
    152515 (TauCross PvZ, loss f17123, supply-50 exit via `atlantis.sh`
    headless): ZERO Pylon/Gateway/Core/Forge/Cannon cancels - the
    building seam holds. Reproduced pre-existing shapes (not regressions):
    118x Assimilator "Can't build here" on one tile [86,118] f5723-17072
    (gas-starved bank 6246/0 - same storm as retained 213009's 138x),
    3x transient Nexus cancels on [94,112] f5141-5258 (not the #151
    thousand-row chase). (Update: the 3x Nexus cancels re-read as
    V2-sited [94,112] engine-refused + A1-null on refresh - honest
    refusal handling, 3 rows not 3000. The #145.1 resite + adopt seams
    rode the legacy finder only before A1; both are catalogue-direct
    now.)
    A2 LANDED 2026-11-01 (`FindPositionForBase.forNewBase`
    catalogue-first at the natural neighbourhood, legacy search stays as
    fallback; base scope lifted, gas stays dedicated): pins 24/24 (incl.
    rewritten `basesAreAnsweredNearTheirNatural`) + fast gate 1058/1058.
    GAME 20261101_153615 (TauCross PvZ, VICTORY f12990, supply-80 exit):
    0 building cancels, 2 natural warps f6254/f10759 - but ZERO
    CATALOGUE_SITE rows: the V2-director sites expansions straight from
    scheduler reservations, the legacy base path never fires live. So
    the game proves no-regression on the live path, not
    catalogue-answers-base-live; that half rides the synthetic pin +
    a vehicle that actually exercises the fallback.
    A4b LANDED 2026-11-01 (modifier→neighbourhood translation):
    `ModifierAnchor.resolve` + `constraintFor` (both overloads) anchor
    @NATURAL/@MAIN/choke orders instead of anywhere; legacy `toPosition`
    stays as the fallback. Pins 25/25 (5 new translation pins +
    adapter suite) + fast gate 1058/1058. GAME 20261101_155312 (TauCross
    PvZ, loss f16689, supply-50): 0 funnel rows - V2-direct bypasses the
    funnel and no legacy modifier order flowed, so the answered-live
    half rides a wall-in vehicle; no-regression holds (0 building
    cancels ex gas-storm; 5x Nexus "Can't build here" on [94,112] is
    the known engine-refusal family, 5 rows not 3000). Repaired in
    passing: A1 had silently degraded legacy @NATURAL wall-ins to
    anywhere (bypassed spiral); the anchor is restored on catalogue.
    A4a LANDED 2026-11-01 (travelling-builder null-site reroute):
    `ConstructionStatusChanger` uses the construction seam instead of the
    legacy finder directly (same null contract, same testing seam).
    Pins 25/25 (status-advance, demote, travelling-builder, stalled-warp
    sim, sibling resite) + fast gate 1058/1058 (2 arch advisory
    pre-existing). No game proof needed: behaviour-identical delegate
    swap, covered by the A1/A2 proof games' refresh/resite traffic.
    A3 LANDED 2026-11-01 (dead-code deletion, Protoss-only D8): removed
    the Terran depot siting files (`SupplyDepotPositionFinder`,
    `PositionsAroundMainBase`, `BuildablePositionsAroundMainBase`,
    `TerranSupplyDepot`, `BarracksPosition`) + the `isSupplyDepot`
    branch in `FindPosition` + dead `nextPosition()` in `PylonPosition` /
    `GatewayPosition` (whole file). All callers were commented-out
    painter lines or the depot branch itself; `CacheKeyTest` mentions
    them in a comment only. Fast gate 1058/1058 (2 arch advisory
    pre-existing). No game proof needed: the 3 proof games show zero
    activity on these paths (0 `WINE_DEBUG` legacy-search mentions),
    and there is no live demand that could reach them.
  - **#147** Replace `Queue/**` and order types LAST with stateless demand and
    scheduling. Exit: V2 LIVE by default with parity; legacy queue remains the
    only queue until then.
    GUARD 2026-11-01 at HEAD: `Queue` is still the singleton queue
    (`Queue extends AbstractQueue`, static instance) - no parallel demand
    path was introduced; the deletion wave waits on #144-#147 parity runs
    (review 2026-11-09). No code touched.
- **#155** `ProduceBuilding` gates are an ordered registry; the core-single-
  instance deadlock is fixed. Narrow `power-not-funded` and `live-construction`
  gates remain stub-pinned by design. Live resite proof rows are oid-linked;
  adopt/warp tile offsets are coordinate conventions, not resites.
  → details: `WINE-MACRO-DATUMS.md` §9.
  VERIFIED 2026-11-01 at HEAD: `BuildGates.all()` is the single ordered
  registry (`BuildGate` interface + `BuildGates` list, evaluated in order in
  `ProduceBuilding`); no ad-hoc gate bypasses it. No code change.
- **#156** Unify the four reservation models behind one queue-position ordering
  and one paid-work exclusion (a building unit exists). Duplicate-Pylon races
  currently read different sums. This belongs to #145/#147; no blind merge.
- **#157** Semantic `ConstructionOrderStatus` queries are in place; all
  PREPARING readers use `isWaiting()` and `setStatus` is the sole writer.
  Behavior-neutral migration; reader proofs ride normal macros.
  VERIFIED 2026-11-01 at HEAD: zero `== PREPARING` / `!= PREPARING` reads
  outside `isWaiting()` itself (all live changes flow through `setStatus`;
  remaining `== IN_PROGRESS` / `== NOT_STARTED` reads name different states,
  not the aimed-future). No code change.
- **#160** Automated tuner selection machinery is implemented and pinned,
  including objective, hall of fame, promotion, manifests, gradient and sim
  batch. Efficacy remains unproven; duel noise dominates at K=3. Require K-of-N
  replicates before promotion.
  CHECK 2026-11-01: still parked (review 2026-11-09 is future); no
  replicates run, nothing promoted.
- **#164** OpenBW takeover: `/sc-ai/OpenBW/openbw` is canonical; the harness
  copy is a build artifact. Install engine changes only through
  `scripts/build-openbw.sh --install`, then run host smoke and engine tests.
  Wine remains the 1v1 proof vehicle; `M&M_v_ZealDrag` is the OpenBW vehicle.
  Do not touch `src/main/Main.java`.
  CHECK 2026-11-01: worktree carries only the two pre-existing foreign
  modifications (archunit store + `run-wine-full.sh`) - `Main.java` and
  the harness untouched by this session; canonical-engine rule holds.
- **#166** Independent OpenBW work is scoped in `OPENBW-HANDOFF.md` (T1–T7,
  M1). No done-signal yet; do not take over or verify until handoff, then rerun
  the host smoke and engine tests. Owner decisions are tracked separately.
  CHECK 2026-11-01: still no done-signal; nothing taken over.

- **#170** Choke doctrine + SquadScout (high priority): do not push a narrow
  choke without superiority; defend from a standoff so the enemy commits into
  the funnel. ChokePlug is sim-pinned for melee-only enemies; no-contact hold
  yields to formation. Open: live no-contact/choke proof, then bait/collapse.
  DATUM 2026-10-28 (game 010715, VICTORY): 263/289 FIGHT rows with foe -1 -
  the army holds forward ground ([53,114], enemy main [95,119]) without
  contact for most of the game; the no-contact shape is present in bulk,
  choke-funnel proof (bait/collapse) still open. DATUM 2026-10-28 (game
  012238, PvP Destination, loss): scout latch generalizes to the new map
  (452/488 rows known from first sighting); Pylon siting clean (8 warps,
  gates 8-63, no duplicates, no leak - naturals connected here as predicted
  in #213); the single CONSTRUCTION_CANCEL is the known proximity-finish
  absorption (Pylon [57,117]), zero cover false positives.
  DATUM 2026-11-01 (94 retained runs, CLUSTER focus census): only 8 games
  ever focus a choke (focus/anchor choke strings); the rest focus coords
  ([119,10] x7788, [95,119] x3613) or Nexus anchors. Choke-funnel proof
  (bait/collapse) has almost no vehicle in this tree; 202204's 4000-frame
  contactless flap (130/184 foe -1, #232 slice 2) is no-contact bulk but
  milling, not holding - not the hold shape either.
- **#171** High-ground choke hold: high side is preferred by the sim-pinned
  `HighGround`/`ChokeStandoff` path; it correctly stays silent vs ranged
  enemies. Live proof still needs a melee-choke game.
  DATUM 2026-11-01: same 8/94 choke-focus census as #170 - the melee-choke
  vehicle is scarce in this tree (TauCross naturals stay connected, no
  island maps); still needs a game, not a rule.
- **#172** Region-aware escape: sim-pinned walkable candidates and
  corner/obstacle/enemy scoring are implemented. Open: live superiority proof
  and tuner efficacy. No center-pull prior (it would bias against measured edge
  risk).
- **#173** Escape doctrine is long-vector-first. The 5-tile ray deflects at full
  length when blocked and engaged live on map corners; angle-stepping is parked
  (no natural trapping geometry found). Duel hit reduction came from #152's
  per-cycle standstill lever, not step amplitude. Open: live walled-pocket
  engagement and whether a deflectee glues.
- **#179** ZealDrag coordination/hold-fire is sim-pinned; live losses showed
  mobile units, not freezing or coordination failure. Open: winnability against
  real opponents (0/9 FFA wins; one weak positive with Dragoon mass).
  DATUM 2026-10-28: game 010715 won vs the native AI with 24 Dragoons / 7
  Zealots - mobile, no freeze; FFA winnability still open.
  DATUM 2026-11-01 (94 retained runs, GAME_END won=true): 16 wins, ALL vs
  the native AI (none FFA, none vs real bots) - 6 short (~f1700-5400, supply
  3-19, micro/FFA maps) + 10 macro (f8640-25050, supply 51-123, TauCross).
  Winnability vs real opponents stays unproven; no live value selected.
- **#181** Split-cluster reinforcement is sim-pinned (fresh-contact reinforce,
  ghost-contact rejection, ongoing-fight priority). Timeouts were the Regions
  path. Open: live recurrence of the park-while-fighting shape.
  DATUM 2026-11-01 (94 retained runs, STALL twin): same census as #182 -
  stalls are sparse small-count rows, never a parked army beside an
  ongoing fight; the recurrence vehicle is still missing.

Priority note: #170-#184 slot BELOW the production ratchet (#45, #144-147) -
small sim-pinned slices, game-verified one at a time. Combat logic stays out of
the commanders (CONVENTIONS §23): new code lives in `FormationHold` /
`FightTarget` / `AcquireTarget` seams.

- **#182** FormationHold's contact veto and holder-unstick are sim-pinned; no
 200f stand has recurred. Pocket geometry explains the remaining stand-and-trade
  shape. FrozenWatch's DEPLOYED gate stays (decision recorded).
  DATUM 2026-11-01 (94 retained runs, STALL twin): 75 games carry STALL
  rows but sparse (max 41 in 235312) with small stalled counts (max 12 in
  184042; single-unit stalls reach still=10k) - no army-mass stand signal
  anywhere; the 200f line-stand recurrence still needs its pinned shape,
  not this proxy.
- **#183** Steady holders cannot use manager-history `isUnderAttack()` because
  HP is refreshed every frame. `DuelMemory` now latches battle-frame damage; it
  is a backstop, while VETO/guns-in-range usually decide first. The one active
  Protoss consumer migrated; remaining consumers are Terran-only or dormant.
  VERIFIED 2026-10-28: zero production callers of `isUnderAttack()` outside
  `AUnit` itself - the only `isUnderAttack(n)` reader is `UnitStateManager`
  (the HP trail infrastructure, not a decision) plus Terran-only turret logic.
  RE-VERIFIED 2026-11-01 at HEAD: only `AUnit` + `UnitStateManager`
  reference it anywhere in production code - the migration holds, no new
  callers (not even Terran).
- **#185** Learn WHEN to use the fixed behavior set from features; synthetic
  scenarios and varied enemy policies only. Sequenced after the production cut;
  spec and gates are in `_AI/NN-PLAN.md`. Test scope now has five zombie policies,
  feature/episode CSV and an S1 learned hold-early gate. Limitation: held-out
  runner behavior needs runner examples in training. Open: feature export is
  test-side only; fidelity Stage A precedes any production learner.
  DATUM 2026-11-01 (30-issue package): test-side export verified green
  (`EpisodeManifestTest` + `LearnedHoldEarlyGateTest` 5/5); the learner
  waits on Stage A fidelity (owner direction on what it measures).

- **#186** `ProtossAttackHoldToShoot`: sim-pinned stop-point model accounts for
  braking, windup, latency and chaser commitment. Live 1Drag retained wins and
  the V2 hit band, but did not isolate a gain. The 0–2-hit target needs harder
  Stage B scenarios; the A/B toggle remains available.
  DATUM 2026-11-01 (30-issue package): the Stage B gate holds -
  `DragoonPincerKiteTest` 2/2 + `PocketVsOpenFieldKiteTest` green (wipe
  pinned, 0-2 hits pinned vs chase-nearest). The missing harder shape is
  named in-code, not designed: ranged pressers that outrange the guns
  (`DragoonsVsZerglingsTest` note). No uncalibrated doctrine pinned -
  the threat pick wants live calibration first (same rule as #92).
  DATUM 2026-11-01 (audit v4): zero kiting-test failures in the full
  suite (none of `DragoonPincerKite`, `PocketVsOpenField`,
  `DragoonsVsZerglings`, `DuelKite` in the 10-class FAIL list) - the
  Stage B gate holds through the resite tree; harder shape + live
  calibration still open.
  STAGE-B SCENARIO LANDED 2026-11-01 (`dragoonsDoNotStalemateOutrangingPressers`):  the wallFallsToRangedPressers fixture (cannons die, pressers untouched)
  now runs WITH two Dragoons dressed at the guns. MEASURED FIRST, red:
  both Dragoons stand through the shelling and die with the wall - the
  escape doctrine's outranged branch does not fire on unseen shellers.
  The test pins fixture + resolution (no 2200-tick stalemate) and names
  the gap; the unseen-shooter step-away is the next B-slice (sim-pinned,
  then live-calibrated). 25/25 in-class green, fast gate 1060/1060
  (2 arch advisory pre-existing).
- **#188** ClusterFlee overhaul: sim pins cover homeward rally, organized
  retreat, and fighting through a blocking army; live audits found no blocking
  instance or scatter. B136's earlier “blocking” reading was withdrawn; the
  observed shape was a cut-off rearguard at the attack destination. Open: live
  blocking-geometry proof and cohesion through a lost fight. MOVE_FAILED
  refusal twins are ready; unstick remains parked until the shape recurs.
  DATUM 2026-10-28: game 010715 (VICTORY) carries zero MOVE_FAILED rows and
  no scatter - the shape does not recur here either.
  DATUM 2026-11-01 (94 retained runs): 93 games carry zero MOVE_FAILED
  rows - the blocking/scatter shape does not recur anywhere in the tree;
  unstick stays parked.
- **#192** Gateway demand was repeatedly cancelled on null position during
  battle. Sim-pinned fixes cover self-overlap, ground-distance search, Pylon
  anchoring, completed-footprint overlap, and bounded WAIT (480 attempts; the
  stuck-loop guard is delayed, not removed). WAIT engaged live; orders survived
  failed searches and warped. Longer-siege calibration remains open; reservation
  release while waiting is tracked in #202. DATUM 2026-11-01 (94 retained
  runs): max GAME_END is f28800 (183253) - Wine's horizon caps every game
  at 20 in-game minutes, so longer-than-28800 sieges are unobservable in
  this tree; calibration past the cap needs a longer-horizon vehicle.
  LIVE PROOF 2026-10-28 (game 003802,
  V2 LIVE): 3rd Gateway warped f18424 at supply 59 on a V2 `At 0` order - the
  BO owns only two Gateway rows and standing Gateways suppress prereq orphans,
  so the only remaining author is the V2 dynamic demand goal (bank 807 at
  f11017, 57 Dragoon `no free facility` blocks f17000-18500 = all-busy, supply
  59 past the 30 gate, under the 4 cap). 4th Gateway pending at game end
  (attempt f19950 engineAccepted, constructing, ended mid-warp f21030).
  SIM HALF CLOSED 2026-10-28 (`LongGameToSeventyTest` to 70 supply): the
  workers/4 army floor is a ceiling without a strategic raiser - rich sim
  banked to 10k with idle Gateways while Dragoons trickled 6->16 in 12k
  frames and GW#3 never fired (no demand ever saturates 2 Gates). Fix is
  the Dragoon pump (`DynamicGoals.addArmyGoal` post-opening continuous
  goal when rich with a free Gateway, symmetric with GatewayDemand) plus
  a sim fidelity fix (training facilities mark `busy`, like the engine's
  idle - without it `freeGateways` never drops in sim and the gateway
  goal can never fire there). Pinned `DragoonPumpTest` 5/5.
- **#194** `DefendedPlace`: the mission selects ground; the army decides how to
  stand. The resolver and focus are in `atlantis.combat.focus`; landed
  2026-10-25 (commit ce4de4034): MIDDLE priority and an absolute >10-Dragoon
  ban, both pinned (`DefendedPlaceTest`, `ProtossDefendFocusTest`).
  The army-size veto is proven live (GAME_97948C74: 153/218 CLUSTER rows on
  MiddleFocusChoke, zero MainChoke); the absolute ban needs a longer game.
  Open: formation adaptation and a CherryVis row. CHERRYVIS ROW LANDED
  2026-11-01 (30-issue package): `DefendedPlace` kind+tile on the ArmyBoard
  (`noteDefendedPlace`, change-only "DefendedPlace" key), computed where
  the answer is resolved (`ProtossDefendFocusPoint`); pinned
  `defendedPlaceRowNamesKindAndTileOnce` (world-free, with the null-tile
  dash). Formation adaptation still open (no formation/cluster code reads
  the kind yet - a doctrine slice: which kinds change slots, sim-pinned).
  INVENTORY 2026-11-01 at HEAD: exactly 3 files reference `DefendedPlace`
  (`DefendedPlace`, `ProtossDefendFocusPoint`, `ArmyBoard`) - zero readers
  in `combat/formation/`, `combat/cluster/`, `FormationHold`. The slice
  boundary is drawn (producer + board row exist, no consumer); which kinds
  change slots is still a doctrine decision, not an implementation gap.
  Shared input resolver
  LANDED 2026-10-26 (commit `ea378db7f`): `HomeGround` in
  `atlantis.combat.cluster` resolves the main base + main choke once per
  resolution - `ClusterRally` (standoff, no-contact hold, guard slot,
  expected direction) and `ProtossDefendFocusPoint` read through it
  instead of five copies of the same null-handling. Pinned
  `HomeGroundTest` 5/5, mutation-proven on `isComplete()`; full unit
  scope green. Small-army main standoff and `guardSlotFor` follow-ups
  CLOSED 2026-10-28 as routing pins (`ClusterRallyTest` +2: melee-only
  at a main-choke anchor plugs the mouth, one ranged foe releases it
  to the crescent - the pair proves the routing, not a coincidence);
  the plug march itself stays sim-pinned in `ChokePlugSimTest`.
  `ClusterBreach`/`ClusterTactic`/`BlockingArmy` converted
  2026-10-26 (same pair, same answers - consumer pins green).
  FORMATION VERDICT LANDED 2026-11-01 (owner: crescents on all three
  chokes, rest unchanged): the main-choke mouth plug is OUT of the
  `formationSlotFor` wiring - `guardSlotFor` deleted, `ChokePlug`
  geometry kept (focus + unit pins still read it). Pinned sim-side:
  `chokeKindsTakeTheCrescent` (Main/Middle/Natural anchors all arc
  around the pivot, melee-only included) + `ChokePlugSimTest`
  rewritten to the crescent contract (dress + stand off, no mouth
  march; plug geometry still pinned pre-march). Combat batch 44/0.
  Revert in one commit if the plug's flank argument returns - the
  deletion is flagged here deliberately. Open: game proof (a choke
  station dresses the arc live - the pressure vehicle below).
  DATUM 2026-11-01 (vehicle game 20261101_204401 on ProtossBaseB_v_Zerg,
  frozen jar WITH crescents, 3:22 loss, 3 deaths): 2858 rows, 383 SHOT
  / 84 HIT / 111 KITE - a real fight, but the doctrine's STATIONING
  half is absent live. Shadow walks STAGE_MIDDLE(f0) -> ATTACK(f1) ->
  HOLD_MAIN(f96-696) -> ATTACK(f901) -> STAGE_MIDDLE(f995-4595 end);
  HOLD_MAIN_CHOKE never fires (min dChoke 7 vs AT_STATION 2.0) and the
  army parks at [7,87] (middle staging) for 3600 frames vetoing chases
  (HOLD: VETO 30, BREACH 29). The mission vocabulary CLASSIFIES where
  the army is; nothing MARCHES it station-to-station (Main -> Main
  Choke -> Natural -> enemy). That conductor is slice-5 territory
  (mission orders movement), queued after slice 4 and 3b per owner
  order - NOT built here. What this vehicle proves instead: no
  regressions from the plug deletion (crescents dress, vetoes hold),
  and the 3a gate stayed silent correctly (never at station, never
  vetoed).
  DATUM 2026-10-28: the absolute >10-Dragoon main-choke ban is PvP-only by
  code (`We.protoss() && Count.dragoons() > 10` in
  `ProtossDefendFocusPoint`) - game 010715 fielded 24 Dragoons vs ZERG, so
  it cannot trigger the ban; the "longer game" it needs is a long PvP.
  DATUM 2026-10-28 (game 012238, PvP Destination, loss): 10 Dragoons trained
  / 8 died, live count never exceeds 10 - the ban stays correctly silent;
  still no game masses >10 live Dragoons vs Protoss.
  DATUM 2026-10-29 (game 154822, PvP Destination, DEFEAT 18/111): FIRST
  LIVE TRIGGER - 20 dragoons dispatched, peak 13 live (f10559-13814
  over-10 window). Ban correctly silent: all 109 CLUSTER rows in the
  window focus the breach fight (EnemyBreachedBase/coords), never a choke
  hold - nothing to veto. PvP vehicle: per-run ENEMY_RACE=Protoss env,
  no source flip.
- **#196** Workers have a named 10-tile limit measured from the main for
  entry/attack/stop/help-Cannon (`WorkerFarFightTest`). Open: a pressure game
  where raiders reach the mineral line; native-AI macros produced no pressure.
  The same livelier vehicle would help #192 calibration and #188b.
  PARTIAL LIVE 2026-10-28 (game 012238, PvP): the vehicle exists after all -
  enemy raiders killed 10 probes on the line f15086-15707. The rule engages
  live and tagged: `#Attack_Unit/Swarm!` then ordered `-WorkerDefenceRun`
  + `#Move/RunToDist` (units 171/190). The line still fell when overrun -
  engagement proven, rescue unproven.
  DATUM 2026-11-01 (94 retained runs, UNIT_DIED probes pre-f6000): 12
  pressure games - strongest 20261026_122342 (14 probes dead from f638,
  ProtossBaseB) + 115321 (7 from f3990), 212230 (7 from f3961), 184529
  (6 from f4013), 003802 (6 from f3847). The native-AI pressure vehicle
  exists in bulk; #200/#205 hold-at-main proof rides the same queue.
  DATUM 2026-11-01 (game 20261101_165023, ProtossBaseB UMS, on-demand
  vehicle): raiders kill 3 probes + the whole home defense (2 pre-placed
  Zealots + 1 Dragoon) f242-714, loss f4812. NO WorkerDefenceRun/order
  tags fire (contrast 012238's tagged defence) - engagement untagged
  here; #205 N/A (no BO in UMS, gate correctly scoped to BO dragoon
  builds); #200 home-fight happened (warped zealots died at home)
  but unattributable post-hoc. #232: shadow STAGE_MIDDLE/HOLD_MAIN
  only, 0 MISSION_ENFORCED - enforcement still unproven.
- **#197** Trapped-Dragoon posture is sim-calibrated and latched; the trapped
  rule lives in `atlantis.combat.focus` with #194. Open: live proof of an army
  attacking out of a trapped defense; inventory other posture reasons later.
  DATUM 2026-10-28: postures observed live are only ATTACK/DEFEND (010715:
  344/39; 012238: 312/103; 010058: 92/270) - no TRAPPED row ever surfaces
  in the twin; the latch is silent or never triggers in these games.
  DATUM 2026-11-01 (94 retained runs): TRAPPED=0 everywhere - zero rows
  in any EVENTS file. Live proof still needs the vehicle, not a fix.
- **#198** Specialist behaviors landed sim-pinned: scarab production, Corsair
  hunting, attacked DT retreat, and High Templar storm selection (including
  deep-energy tiers and snipes). Observers use their own detector chain and fall
  through the specialist dispatch. The 195-energy single-target tier is rarely
  expressible in sim; live geometry remains its evidence path.
  DATUM 2026-10-28: zero High Templar (also zero Corsairs/DT/Scouts) warped
  in 3 audited V2 macro games (010715, 010058, 012238: Zealot/Dragoon-only
  armies) - the whole specialist tier never presents; needs a build that
  techs there, same vehicle problem as #85/#201.
  DATUM 2026-11-01 (94 retained runs): zero High Templar warps anywhere,
  zero Psionic_Storm rows, Corsairs in only 2 runs (222936: 2, 115405: 3
  with Observatory at f21433) - and all vanish from every record (no
  telemetry, no order tags, no deaths). Full expression unproven; needs a
  game that techs there AND fights with it. TRACE GAP (game 115405): the 3
  warps (f12652/13694/15839) leave 0 telemetry/death/attack rows - see
  #201 deep-dive; either air units never enter the twins or the warps die
  off-record.
- **#200** Against early Zerg, fewer than five combat units hold at the main
  when the midpoint exceeds six tiles, leaving room for workers to join
  (`NoContactHoldTest`). Extended 2026-10-26 by the first-Dragoon gate
  (#205): a dragoon build holds the main regardless of that count until its
  first Dragoon is finished. Open: live proof. DATUM 2026-10-28: game 010715
  (PvZ VICTORY) shows first shots at f9885 - no early-Zerg pressure at all,
  so neither #200 nor #205 can be proved by this vehicle; stands with #196
  (needs a livelier opponent than the native AI).
  DATUM 2026-11-01: the vehicle queue is now named - 122342 (14 probes
  dead from f638), 115321 (7 from f3990), 212230 (7 from f3961), 184529
  (6 from f4013) all pre-f6000 (see #196). Hold-at-main (#200) and
  first-Dragoon-gate (#205) proof rides the next quiet run over one of
  these, not a new search.
- **#201** Corsairs trail the main army instead of hunting: they need their own
  (default solo) cluster, merging only when threatening air exists; the
  "numerous Wraiths" line becomes a named constant with a pin (AEC §31).
  Sim-first (both queues spent). Membership rule LANDED 2026-10-26
  (commit `716a46142`): `CorsairCluster` in `atlantis.combat.cluster` -
  solo by default, merges on any visible non-Wraith air combat or
  `NUMEROUS_WRAITHS = 3` known Wraiths, pinned `CorsairClusterTest` 10/10
  (pure math + live stub-world on both conditions and the boundary),
  mutation-proven both ways. The `Alpha.gather` split landed 2026-10-26:
  solo Corsairs stay out of Alpha (CorsairHunt still owns them), merging
  only when the rule fires, pinned `CorsairGatherSplitTest` 2/2. Open:
  live proof the solo flight hunts. DATUM 2026-10-28: game 010715 built 0
  Corsairs (24 Dragoons / 7 Zealots, VICTORY) - the proof needs a game that
  builds them. DATUM 2026-11-01 (30-issue package): 2 retained runs built
  Corsairs (20261026_222936: 2, 20261029_115405: 3 in a 148-supply loss) -
  and all vanish from every record (no telemetry, no order tags, no deaths,
  no attacks). A build alone proves nothing; the hunt still needs a game
  where Corsairs actually engage.
  DEEP-DIVE 2026-11-01 (game 115405): the 3 Corsairs warp f12652/13694/
  15839 at [20,41] yet produce 0 UNIT_TELEMETRY + 0 UNIT_DIED + 0 attack
  rows; the only Corsair records are 59 V2_DISPATCH rows reading
  "facility unfinished #237" + 1 SCHEDULE_REFUSED. Either air units never
  enter the twins, or the warps die instantly off-record - the trace gap
  is now the shape, not just the missing hunt. No fix (owner: no
  opportunistic macros for rare shapes).
  ROOT CAUSE TRACED 2026-11-01 (read-only, owner order): the hunt is
  unreachable, not just unproven. `CorsairHunt.act` has exactly one
  caller (`SpecialistBehaviors.act`), which runs only inside
  `BattleCommander.act`, which runs only over Alpha's loop - and Alpha
  explicitly skips solo Corsairs (`!CorsairCluster.mergeWithArmy()`).
  Nothing else drives them per frame: no orders, no `observe()` (hence
  no telemetry/FrozenWatch/DuelMemory), and if nothing kills them, no
  death rows either. Solo Corsairs sit at their warp tile forever.
  Fix direction (no code yet): a per-frame driver for non-Alpha Corsairs
  calling `observe()` + `CorsairHunt` (or route them through the shared
  pipeline without Alpha membership). The 59 "facility unfinished #237"
  dispatches are scheduler churn on the unfinished Stargate, separate
  from the driver gap.
- **#202** Production stall with banked gas: READY construction reservations
  may starve unit production while #192's bounded WAIT holds. Landed
  2026-10-25 (commit ce4de4034): release the earmark during WAIT and
  re-reserve on success, pinned in `GatewayWaitsOnBlockedBaseTest`. Owner
  bank rule landed 2026-10-26: 550 minerals covers everything, gas stops
  mattering at 300 (150 supply or less) / 400 above, gas-free orders never
  wait on gas - a rich bank flows through reservations and D3 waiters alike,
  pinned in `RichBankBypassTest`. Live verdict needs owner data on minerals,
  dominant twin rows, and Gateway count. Live datum 2026-10-27/28 (183253, 184042, 190104,
  all V2 LIVE): the rich-and-idle deaths are NOT this mechanism - observed
  causes are the covered-tile Pylon cancel loop (fixed, top-left overlap
  closeout 2026-10-27/28: zero cancels in 234528 + 000314), no 3rd
  Gateway author (V2 demand goal landed, needs its own live proof), the
  Nexus refresh chase (unfixed, needs #150), and an engine supply-refusal
  the twin mislabels "no free facility". The WAIT+reserve interaction
  itself was never observed to starve anything.
   DATUM 2026-11-01 (game 131909, loss f28800): single base to f24271,
   bank to 8625, then Nexus (bank 8625->~0) + Pylon f27680; ends 8084/52 at
   supply 53/65 with 4 Gateways (GW#3 f8144, GW#4 f14620), 1 Assimilator,
   21 Zealots, 10 Dragoons. Late twins are combat micro (KITE/SHOT/HIT),
   production silent (2 BUILD rows after f24000). Minerals at stall: ~8k;
   dominant rows: combat, not demand; gateways: 4, standing idle with a
   full bank. A rich single-base pump stall with no WAIT involved - the
   save-for-expansion doctrine (#234b) fires 16 minutes late here.
- **#203** Zerg matchup: landed 2026-10-25 (commit ce4de4034) `ProduceZealot`
  brakes excess Zealots at >=200 gas when a Core exists and the minimum Zealot
  floor is met; initial Zealots remain allowed. Pinned by
  `ZealotGasBankBrakeTest`; live proof remains. DATUM 2026-10-28 (010058):
  gas banks 200+ only in the final 70 frames (880-888 at f14717-14747,
  game ends f14785) - the mid-game brake window never presents, so this
  vehicle cannot prove it; needs a gas-rich mid-game.
  DATUM 2026-11-01 (94 retained runs, BUILDINGS gas@warp): the window
  presents in bulk - 17 games warp mid-game Zealots at gas 200+ (e.g.
  121013 f6420 g216, 222936 f9109 g204, 000314 f6175 g216, 134145 f8996
  g268, 192200 f7863 g242). Brake-consistent cases: 010715 (Core + 5
  Zealots pre-gas, then ZERO Zealots f5424-14605 across the whole gas-rich
  Dragoon phase, 2 only very late at g624/786) and 184529 (7 pre-gas,
  then none until gas spent to 62). Counter-shape: 122342 warps 2 Zealots
  at g508/482 - initial-floor Zealots, allowed by design. Full live proof
  (brake row vs warp row attribution) still wants the twin, not just the
  counts - but the vehicle is no longer missing.
- **#204** Retreat floor + choke standoff depth (owner orders 2026-10-25,
  after repeated losses to over-long retreats): (1) `RetreatFloor.clamp`
  stops a FLEE rally on the `MiddleFocusChoke` standing line, never behind
  it - only a retreat that would CROSS the line is stopped, an army already
  inside keeps today's rally, and a breach never reaches the path
  (`ClusterTactic` forces FIGHT while raiders are in the base); (2)
  `ChokeStandoff.standoffTiles` = RADIUS + SPARE_TILES (6) replaces the fixed
  10/8-tile depths and is consumed by the strategy hold and by
  `ClusterRally.chokeStandoffPoint` - one doctrine, one number - so the dead
  `CombatTuning.chokeStandoffTiles` knob was deleted. Pinned
  `RetreatFloorTest` 4/4 (deep rally stops short, an outside rally stands, an
  inside line keeps its rally, end-to-end retreat) +
  `ChokeStandoffDistanceTest` 3/3 (the owner's 9-radius example => 15 tiles,
  a wider choke stands further back, the rally matches); `DuelKiteTest`
  rebased on the same seam. Suite 1568/1568, arch 2-failure advisory
  unchanged. Open: live proof - a game where a losing fight stops at the
  middle instead of running home.
  - Graded further 2026-10-26 into the **retreat ladder** (owner order,
    commit `e06adb27d`) after the floor's own gate failed live: in
    GAME_26E683D4 an army at 64 tiles from home ran 42 tiles back to the
    Nexus, past the line it held - "already inside the line" switched the
    floor off exactly where it was needed. The floor now reads the ground
    the army last fought on (`ArmyContact.noteFight` ->
    `RetreatLadder.noteFightGround`, only 4+ unit fights): beyond the
    middle choke line, then the natural (main) choke line, and that is the
    floor - the Nexus is never a retreat destination. `RetreatLadderTest`
    5/5 + `RetreatFloorTest` 8/8 (was 4), both mutation-proven (killing the
    middle rung fails 4 pins, dropping the hold guard fails 1). The owner's
    companion symptom "came home, then hid in the main" is #206.
    LIVE SUPPORT 2026-10-28 (game 010715, VICTORY f15540): 94 FLEE rows hold
    a constant forward focus [53,114] f9920-15035, never run home (39 home
    rows are all pre-Attack early defense f3627-4805); strength rebuilds
    315->667 mid-rally and the game is won. Rung identity ([53,114] = which
    ladder rung) is not provable from the twin - the shape matches the floor
    intent, the exact rung needs trace geometry.
   DATUM 2026-11-01 (games 154822 + 202204): neither proves the floor -
   154822 runs home and dies there (last Defend f23333, dead f24095, no
   stop at the middle), 202204 flaps with no contact at all (the #232
   indecision shape, not a losing fight). The losing-fight-stops-at-middle
   game is still missing.
- **#205** First-Dragoon gate vs Zerg (owner order 2026-10-26): on a build
  that trains Dragoons the line defending home stays at the main base until
  the first Dragoon is finished - the early-Zerg small-army hold (#200) only
  covered the count, so six Zealots of a dragoon build walked out to the
  halfway standoff with no ranged unit on the field. Landed 2026-10-26
  (commit `fe2c0431b`): `FirstDragoonGate` = Zerg x zero finished Dragoons x
  build plans a Dragoon, written as a flag because combat never reads
  production - `OnGameStarted` hands over `CurrentBuildOrder.plansDragoon()`
  (the same bridge pattern as the scout setting) and
  `BattleCommander.clearForTests` drops it per game. Pinned
  `FirstDragoonGateTest` 8/8 (pure gate math, fresh-game default,
  build-order rows, unknown build) + `NoContactHoldTest` +3 (six Zealots
  hold the main, one Dragoon opens it, an ungated build stages out),
  mutation-proven both ways. Open: live proof in an early-Zerg defense;
  rides the same one-game queue as #200.
- **#206** Army hides in the main after coming home (owner live finding
  2026-10-26): the line comes back and then stays tucked into the main
  despite repeated attempts to stop doing that. Everything nearby points
  home by design - the ladder (#204) stops the run at the choke line,
  #200/#205 hold small or gated armies at the main, `defendAnchor` answers
  the main building for a non-Protoss enemy - so the open question is which
  of them keeps the army in AFTER contact is over. Needs one game where the
  fight ends and the army must re-deploy: read the anchor/hold/posture rows
  in the frames after contact, name the latch that keeps it home, then one
  rule with a pin. Do not add a fourth home-pointer before that reading.
   DATUM 2026-10-29 (games 184529 + 200344): the army never comes home
   in either (fights out, dies out) - the hide shape stays unobserved.
   DATUM 2026-11-01 (game 131909): Defend on contact f23695, contact lost
   f23808, re-Attack f23815 (20 s later) - the line re-attacks instead of
   coming home; still no home-hide shape. Ends f28800 rich-idle (bank
   8084, gas 52, supply 53/65 - see #202).
   DATUM 2026-11-01 (games 154822 + 202204): 154822 fights/flees home and
   dies there (68/111 deaths are probes post-f18000, last mission Defend
   f23333, 762 frames to the f24095 end) - home death under pressure, not
   hiding; 202204 flaps Attack/Defend every ~100f f13981-f18085 with no
   contact (the #232 indecision shape). The hide-after-contact latch still
   unobserved in all three.
  MATCHUP DOCTRINE LANDED 2026-10-28 (owner order: vs Zerg hold, vs Terran
  attack, vs Protoss wait for 3 zealots or a dragoon then attack):
  `PostureReasons` carries `vsProtoss`/`ourZealots`, and a no-contact small
  army with mass marches as PROTOSS_MASS_READY instead of holding for
  blind strength; toothless lines hold. Contact shapes (trap), big armies,
  Terran pressure and the dragoon pair all still answer first, so no
  existing pin moved - the vsProtoss flag defaults off everywhere except
  live. `PostureTrappedArmyTest` 260 now marches under the mass reason
  (the old window-over-DEFEND was the pre-doctrine shape); the latch
  still holds the 60-frame rally.
  ATTACK DECISION LANDED 2026-10-28 (owner order): `AttackSnapshot` (4
  nullable corners, engine-free) -> `AttackDecider.decide` (verbatim old
  chain) -> `AttackDecision{target, AttackReason}`; StarEngine pins every
  branch without orders (`AttackDeciderTest` 5/5); `AttackFocusPoint`
  builds the snapshot live and twins ATTACK_DECISION rows on change,
  behavior identical (focus pins green). The army CAN kill a passive base
  when sent (`MotionlessBaseRaidTest` + raid collapse); the live question
  who sends it now reads off the twin instead of the orders. Live proof
  rides the running macro.
  DATUM 2026-10-28: game 010715 never presents the shape - Attack f4818 to
  victory, the only home rows are pre-Attack early defense; nothing came
  home to hide. DATUM 2026-10-28 (game 012238, PvP loss): army comes home on
  Defend f14964 and dies FIGHTING there (shots from f14964, 10 probes lost
  on the line) - defending under attack, not hiding; the latch shape still
  unobserved. CAPABILITY HALF 2026-10-28 (StarEngine, owner order):
  `MotionlessBaseRaidTest` - 12 Dragoons vs a motionless base (Nexus + 10
  Zealots holdStill) - went red first (stood 13 tiles out in standoff
  forever), fixed by the raid collapse in `Regroup.march` (dressed in slot
  far beyond sight of a FIGHT anchor marches a walkable staging 5 short of
  it, COLLAPSE tag), now green with the base killed. So the army CAN kill
  a passive base when sent. COLLAPSE COMMITS 2026-11-01 (owner: why does a
  near start break the battle?): measured with HOLD twins - at 20-60 tiles
  the raid never acquires (fog) and the 10-tile collapse gate dithers at
  the boundary (line shuffles ~10-13 tiles, zero HOLD verdicts in 600
  ticks - formation is NOT the culprit, the no-target regroup is). The
  collapse now latches per unit until 6 tiles or contact
  (`Regroup.shouldCollapseToAnchor` + `BattleCommander.clearForTests`).
  The raid starts dressed 20 tiles out at full 12v10 scale: the 140-tile
  march is dead ticks, the committing collapse carries the line into
  contact, and the owner-reframed pin (attacked, not killed) exits on
  first damage.
  BUILDINGS-ONLY FORMATION GUARD 2026-11-01 (owner: no formation when only
  enemy buildings remain; allowed before ground-attacking defense): 12
  Dragoons held 600 ticks vs a lone Nexus (HOLD x40, zero damage) -
  `FormationHold.decide` returns OTHER when no enemy combat unit and no
  ground-attacking static defense is known (cannons keep formation;
  hidden defenders re-arm it on approach, per frame). Pinned
  `NoFormationVsBuildingsOnlyTest` (fast). The zealot-held variant keeps
  today's logic (visible combat units) - its stalemate is the collapse
  side above, not the formation side. ATTACK-NOT-KILL 2026-11-01 (owner:
  time is secondary, attacking is the pin - stop at Nexus below 95% hp):
  `dragoonMassRaidsMotionlessBaseAndAttacksIt` (renamed from ...AndWins)
  breaks on first damage with the 12v10 force from 20 tiles; the full
  grind never runs, the weird non-attacking behavior is what fails.
  ATTACK_DECISION LIVE 2026-10-28 (game 110910):
  the twin fires - 1 row f4389 ENEMY_MAIN_KNOWN -> [119,10], same frame the
  mission flips Defend->Attack (change-only, no repeats); raid collapse
  fires live too (164 ClusterCollapse tags). The live question who sends
  the army now reads off the twin instead of the orders.
- **#207** Army forms as if the enemy were in a different base (owner live
  finding 2026-10-26): an enemy base is detected, yet the army positions
  toward a base the enemy is not in. Open: line the scout/enemy-base memory
  (`EnemyUnits`, last-seen positions) up against the anchor chosen in the
  same frames of one trace and decide whether the stale base or the anchor
  choice is at fault - fix whichever one the frames convict, with a pin.
  Needs a game where both bases are known. DATUM 2026-10-28: game 010715
  (VICTORY) knew both mains from f3658 and formed AT the correct enemy base
  [95,119] (96 rows) - the wrong-base shape did not occur.
  DATUM 2026-11-01 (98 retained + 12 proof games audited): no game shows
  the army forming toward a known-wrong base - every known-both-mains
  game forms at the enemy main or holds home by mission. The
  fault-attribution this item asks for still waits for its vehicle.
- **#208** Formation in our own base faces our own main (owner live finding
  2026-10-26): slots are laid out as if we were attacking our own Nexus -
  a sign/orientation error between the formation pivot and the threat
  direction. Sim proof landed 2026-10-26: a home anchor left behind the army
  no longer owns the pivot - `expectedDirectionPoint` faces outward past the
  army (at-anchor bunch and forward-anchor crescents unchanged), pinned in
  `ClusterRallyTest` (red-first: old slots arc at [31,20] around the main).
  Open: the same orientation live. DATUM 2026-10-28: game 010715 defends
  home only pre-Attack (39 rows f3627-4805); the orientation question needs
  trace geometry from a home-defense game, not twins.
  DATUM 2026-11-01 (same window re-audited): 376 rows, anchor=focus=Nexus
  throughout, ZERO formation/slot/direction twin rows in the window;
  UNIT_TELEMETRY carries unit pos + foe DISTANCE only (no foe bearing,
  no slot dest, no facing). The pivot-vs-threat question is unanswerable
  from twins by construction - viewer trace still required.
- **#210** Formation orders while defending at most once per 10 frames
  (owner order 2026-10-26): re-issuing formation orders every frame during
  a defense churns the line. Landed 2026-10-26: shared `FormationOrderLatch`
  - identical re-issues wait out the window, first deployment and changed
  destinations always flow, leaving DEFEND forgets the latch; denied
  callers hold instead of falling through (pinned `FormationOrderLatchTest`
  4/4, red-first on the deny path). Measured first: 120 identical orders
  per unit per 120 defended frames in the sim. Open: live proof the line
  calms down. TWIN KEY LANDED 2026-10-30: FORMATION_ORDER rows
  issued/absorbed with dest (transitions always, 150f heartbeat) - the
  calm reads as absorbs, no trace geometry needed.
  DATUM 2026-11-01 (game 202204): anti-calm, not calm - 417
  FORMATION_ORDER rows f13981-f18085 (reform ~every 10f) riding the
  Attack/Defend flap with no contact. The latch fires as designed; the
  calm-defense game is still missing.
- **#212** Worker acquisition strategy (owner call, surfaced in
  `CASE-STUDIES.md` #53): a worker held at a construction site that cannot
  start should return to mining (`ShouldNotTravelToConstructYet` covers
  only the travel half); and the 250-mineral `CriticalStuff` gate refuses
  the worker that would earn the minerals - the whole worker path is gated
  on being rich, the one state a young base is not in (the output of
  waiting is fewer minerals, so the gate never releases). Candidates:
  exempt a base below N workers from the mineral-margin gates, or make the
  worker goal a V2 `DynamicGoals` contributor at `PRIORITY_WORKERS` and let
  the scheduler arbitrate. The duplicate-order half (TWO constructions for
  one building, one miner lost each) rides #144's duplicate-Pylon
  attribution, not this item. First half LANDED: a waiting builder mines
  through the same forecast gate that holds its walk (assignment stays, no
  Builder/Gather tug of war), pinned `BuilderMinesWhileWaitingTest` 2/2,
  full unit scope green; the branch sits BEFORE travel because the
  CantAfford tail of `IssueBuildOrder` returns handled. Open: the 250-gate
  second half and live proof. Parked 2026-10-27 (owner: no young-base
  stall has ever shown; 222936 ran a healthy bank through the whole
  game - revisit when a game shows the stall, not before).
  CHECK 2026-11-01: still parked; the 94-run census shows no young-base
  stall exhibit either (one-base long games bank rich or spend, never
  stall at 250).
- **#213** Pylon #3-4 sited at the natural (owner live finding): the
  `exclude(natural).last()` fallthrough in `PylonPosition.nearToForPylon`
  leaks the natural in whenever `Bases.natural()` is null with two bases
  (exclude(null) excludes nothing, last() is the newest base). Fixed:
  pylons < 4 pin to `Select.mainOrAnyBuilding()`, first/second-Pylon
  origins unchanged, pinned `FirstFourPylonsStayAtMainTest` (red-first:
  toMain=40/toNatural=0). Open: live proof on a two-base custom map -
  TauCross proves nothing (its natural detection holds, the leak never
  fires there). TRIGGER CHARACTERIZED 2026-10-28 from code
  (`DefineNaturalBase`): natural is null iff no second base location is
  ground-connected to the main (island starts, UMS) - so the proof vehicle
  must be an island/UMS map, not just any 2-player ladder map (a (2)Destination
  PvP game is running to re-verify siting generally, but its naturals are
  connected, so it cannot trigger this leak).
   DATUM 2026-10-29 (games 184529 + 200344): no natural ever exists in either -
   untriggered again. Needs a vetted island vehicle + a game that expands there.
   DATUM 2026-11-01 (94 retained runs, dir census): 78 TauCross + 5
   Destination + 11 singletons (7th_rav, ProtossBaseB, Micro_2, LingsUpg2)
   - ZERO island/UMS maps, so the null-natural trigger cannot fire anywhere
   in the tree. Vehicle still missing; no code touched.
   DATUM 2026-11-01 (game 20261101_171213, (2)Benzene headless, loss
   f16479): Benzene is NOT the island vehicle - the ground scout reaches
   the enemy main [119,14] f3627 and holds it (415/491 scout rows with
   sighting), so the map is connected and the null-natural trigger cannot
   fire here either. No natural taken all game (0 Nexus warps, single base
   to the f16479 home-defense loss). Conclusion: the trigger is
   unreachable on the entire vetted map library; proving it needs an
   island/UMS map that does not exist here - owner call (same class
   as #34).
- **#214** Decision reasons in CherryVis (owner: VERY HIGH priority).
  Every big decision logs its reason; where a behavior is unattributed
  today, the reason row IS the fix. (a) Siting: every construction logs
  nearTo + chosen tile + author (BO/supply/wall-in/per-base) - the Pylon
  case needs WHERE, not WHY. (b) Army: AlphaMission/AlphaTactic,
  AlphaReason with numbers, AlphaFocus/AlphaFocusName (name + tile),
  AlphaStrength - each on change. (c) Units: active Behavior per combat
  unit on change ("tag/Class" in units_logs); workers only while
  fighting/fleeing. Board for time-varying values, Logs for events.
  Volume rule: transitions, never per-frame rows. Motivating game:
  14:25:25 Tomas Cere (TauCross, Defeat 95/86, 52 Dragoons lost, army
  never left the main - unattributed, no trace).
  VERIFIED LIVE 2026-10-28 (game 010715, VICTORY): (b) lands in
  board_updates change-only - AlphaStrength 233 rows, AlphaReason 207
  (with numbers, e.g. "Early strength ok (4 units, 999%)"), AlphaTactic 13,
  AlphaFocusName 8 (with tile); 461 updates over 15540 frames, never
  per-frame. (c) log half lands (order-watcher `#Attack/<behavior>` tags);
  Task half is the Task-mirror work (manager-slot mirror for managerless
  army units, landed 2026-10-28). (a) half: CATALOGUE_SITE carries type/result/reason/via
  but NO nearTo/chosen tile - the Pylon WHERE is still missing.
  WHERE LANDED 2026-10-28: answered rows carry nearTx/nearTy from the
  request constraint (neighbourhood centre or exact pin, -1 otherwise),
  so a far-away warp names its origin without tile forensics. Pinned pure
  (`TargetPlacementNearTest` 4/4) + answered row shape (13/13 in
  `CatalogueSiteAdapterTest`); declined rows unchanged.
  DECLINED CAUGHT UP 2026-11-01 (30-issue package): declined rows carry
  the same nearTx/nearTy (scope and no-order fires keep -1: type-level,
  no site context). Pinned in `sitedFailureDeclinesToLegacy` (row shape
  with origin).
  FOG-LOOP REPAIR LANDED 2026-10-28: whatever path skips a visible
  enemy (missed discover, listener throw, new id with no create event),
  the per-frame fog loop now wraps it - first wrap rides the discover path
  with responses, guarded by the same gas exemption as the update. This
  closes the never-wrapped hole behind show-heal (re-sight) and
  create-wrap (new ids): sight without memory repairs itself every frame.
  Pinned `FogMemoryResightTest` 5/5 (red-first: null after update).
  WALK-WINDOW RE-FIRE CLOSED 2026-10-28: the second Core of game 151131
  (dispatches f6139+f6281, warps f6297+f6400 on two tiles from one At-22
  row) is the scheduler's in-flight check missing IN_PROGRESS-without-unit
  constructions - between the builder's arrival and the warp the build
  reads as missing and the row re-fires mid-walk. `LiveExistingItems`
  counts them now (same optimistic estimate as a requested site); the row
  fires once. Pinned `LiveExistingItemsTest` 2/2 (red-first: missing).
  REMAINDER 2026-11-01 (30-issue package): all three halves landed
  ((a) siting+WHERE+declined-nearTo, (b) army board+tooltips, (c) Task
  mirror); what remains is a fresh live game proving an unattributed
  death is now attributed (the Tomas Cere shape) - owner-gated vehicle.
- **#216** OpenBW melee hosts no game (handshake loop, zero frames; UMS smoke
  green): `run-openbw-e2e.sh` on TauCross attaches but the host never
  publishes frames. Same map + script played full openings on 10-18/21.
  Delta since: the harness was rebuilt from the dirty engine-track tree
  (uncommitted GameUnits/Server/BWData edits; canonical openbw clean).
  Engine-side, not bot-side - DO NOT touch (OPENBW-HANDOFF owns it).
  Melee parity/LIVE runs (WO-144 step 3, WO-145 A/B) wait; Wine as
  alternative vehicle is an owner call.
  Vehicle 2026-10-27 (owner): Wine + StarEngine only, OpenBW abandoned
  for now - parity and proofs ride Wine macros until further notice.
- **#217** Sim post-expansion production stall (measured 2026-10-27 in
  `SimOpeningToFiftyTest`, Zealot-into-Goon to 50): after the natural Nexus
  warps (~f9393, supply 49) the sim builds one Probe in ~1800 frames while
  the bank climbs to 849. Live 104542 keeps producing through the same
  window (max bank below 50 is 178). Open: whether the stall is bot-side
  (scheduler with two bases) or sim-side (no worker transfer to the
  natural), and whether it predates the exact-gate cutover. The opening
  gate stops at the BO end (horizon 30) until this is settled.
  Worker-cap half fixed 2026-10-27: the pump saturated on standing
  bases only, stalling the whole natural warp - a warping base counts
  now, pinned `WorkerGoalCapTest` 3/3. STALL HALF CLOSED 2026-10-28:
  `PostFiftyProductionTest` plays the same opening to 50 then 2500 frames
  more - probes 22->30 (~1/300f), supply 50->62, bank cycles on spends
  (Pylon 6->7, Dragoons 6->7); the 1-probe/1800f + bank-849 shape is gone,
  fixed by the saturation change. Open: the no-enemy half (army
  target met, nothing dies - needs the P3 scripted enemy) and the live
  proof of pre-saturation.
   DATUM 2026-11-01 (time-budget stage): solo-red again, suite-green -
   2 Probe warps in 2500 frames after f12000 (mark later than the ~f9393
   pin), deterministic in a fresh JVM. Suspect stride-60 fallout (the
   dispatch that fixed it predates the stride cutover); the hunt rides
   the 30-issue package, the test is time-clean (12 s solo).
   DATUM 2026-11-01 (audit v4, isolated e2e): PostFiftyProductionTest
   6.8 s + PostFiftyBleedTest 6.6 s, both exit 0 - time-clean and green
   in isolation; the solo-red/suite-green flip does not reproduce as an
   OVER here.
- **#220** Runby screening gap (measured in `LingFloodHoldsTest`, P3 shape
  of 115321): 2 staged Zealots fall back to the Nexus (both at (10,9) by
  t200) instead of screening mid-map, so a 4-ling runby is fought at the
  mineral line - 4/6 probes fall vs chase-nearest, 6/6 vs focus-weakest,
  1 Zealot dies in both. Lings die and the Nexus stands (that half is
  pinned). Suspected eval conservatism at 2v4 rallying to the anchor -
  the #206 shape. Combat-cluster tuning with live proof, not economy
  work.
- **#222** Sim order-fragility (pre-existing, untouched): `CutoffSwampedRetreatTest`
  flips under suite order - razor margin (measured 7.01 vs threshold 8) with
  iteration order keyed off the global FakeUnit id hash, so a shifted id
  counter reshuffles the cutoff evaluation. Reproduction recipe: full fast
  suite (solo and package runs stay green); trigger is intermittent, not
  deterministic. DATUM 2026-10-28: solo 1/1 green, `tests.unit.sim` package
  108/108 green, full suite 1693/1693 green twice - no flip today. Do NOT
  fix with a global FakeUnit id reset (reshuffles all order-sensitive
   expectations) - and none was needed: the 7.01-vs-8 wobble was cured by
   committing to one escape direction (waypoint) plus a toward-rally bias
   (DATUM 2026-10-29, suites stay green).
  expectations) and do NOT touch from the builder-log harness (world()-based
  setup perturbs global state and flips it - `BuilderOrderLogTest` uses
  direct BaseSelect mocks for exactly this reason.
   DATUM 2026-10-31: recurred byte-identical (7.015677199137657 vs 8,
   "survived") in a full run carrying two new test files
   (`CoveringFireSimTest`, `SharedFocusSimTest`) and green again solo
   1/1; full suite WITHOUT the two files (sources moved + stale
   `out/` classes deleted) is 1896/1896 green - the trigger is the
   shifted global id counter, exactly the documented mechanism, not a
   product regression (the slice carries zero production diff). Any new
   test file can flip it; the durable fix stays hermetic seams, not id
   resets. Owner 2026-10-31: live with the flake (document recurrences),
   durable fix with K-of-N data or a test-harness rebuild - not now.
   DATUM 2026-11-01 (audit v4, with the new `SiblingCover` test file in
   tree): CutoffSwampedTest green (absent from the 10-class FAIL list) -
   the documented trigger (new test file shifts the global id counter)
   did NOT flip it this time. Flake stays lived-with, recurrences
   documented.
   DATUM 2026-11-01 (time-budget stage): the same one-JVM runs slow sims
   5-11x vs solo (Bleed 11->63 s, Stalled 13->134 s, LongGame 13->65 s)
   and flip reds both ways (LongGame @65 red in suite, green solo;
   PostFiftyProduction red solo, green in suite). Mechanism unidentified;
   sim-to-sim pairs show no leak, so the poison comes from earlier scopes
   or GC pressure at suite scale. Landed instead of solved: e2e runs
   isolated (one JVM per class) in `run-full-tests.sh` and the audit,
   `-Xmx4g` on every test JVM, stale `.class` purge before every compile
   (phantom tests from deleted sources - measured), and the JUnit default
   timeout must stay `same_thread` (Mockito static mocks are thread-local:
   `separate_thread` NPEs every harness test - proven by probe).
- **#223** Hidden-unit crisis response (owner ask 2026-10-28: do we answer
  lurkers and Dark Templar, and how does PurpleWave do it). PurpleWave
  pumps Observers 1-vs-3 on lurker-likelihood (seen lurkers/eggs or burrow
  tech) plus cannons at openings, behind the Robo chain; prediction rides
  `enemyHasShown` evidence, including tech buildings. Ours answers the
  same shape on first sight: lurker/egg/DT (or Citadel/Archives/HydraDen)
  latches `HAS_HIDDEN_COMBAT_UNIT` and queues Forge (if missing) + 2
  cannons with top priority alongside Core/Robo/Observatory/Observer.
  VERIFIED 2026-10-28 in-harness, was unpinned and doubted: a seen lurker
  queues the full chain (`HiddenUnitResponseTest` 2/2: Robo+Obs+Observer,
  Forge+Cannons). V2 GOAL LAYER LANDED 2026-10-28: the legacy queue above
  never drains in V2 LIVE (`ProductionOrdersCommander` returns after the
  engine), so a seen lurker ordered nothing live despite the queue firing.
  The snapshot carries `enemyHasHiddenUnits` (fed live from
  `EnemyInfo.goesOrHasHiddenUnits()`), and `addDetectionGoals` emits
  Robo/Observatory/Observer + Forge (unless standing) + 2 main-anchored
  cannons at PRIORITY_BASEDEFENSE; the scheduler pulls missing prereqs
  itself. Pinned `HiddenUnitDetectionGoalsTest` 5/5 (red-first: no goal
  within 30 frames before the branch; `atBase()` also dropped every
  post-construction field - now carried across). Open: a live lurker/DT
  game proving the order dispatches and the Observer ships in time;
  PurpleWave's 3-vs-1 observer pump on likelihood (vs our 1) is a doctrine
  delta, not a defect. DATUM 2026-11-01 (30-issue package): game
  20261027_110507 (PRE-fix) lost 2 Dragoons to lurker 210 at f17027/17062
  with an early Forge standing and zero detection chain after - the shape
  the V2 goal layer answers. No post-fix lurker game in retained runs;
  still needs the vehicle.
  DATUM 2026-11-01 (30-issue package, POST-fix game 20261029_122910):
  lurker #234 breached f18946, fought in contact to the f28800 loss -
  and the chain answered PARTIALLY: Forge warped f19800 (+850f), ONE
  RoboticsF dispatch (never warped - gas ended at 8), ZERO cannon/
  Observatory/Observer dispatches or warps, ZERO HIDDEN/detection twin
  rows. The lurker appears only in CLUSTER rows (never a discovery row -
  no DISCOVER/first-sight twin key exists). Suspect: never entered
  `EnemyUnits.discovered()` (no create-event) so `hasHiddenUnits` stayed
  false and the snapshot flag with it - despite mineral affordability for
  cannons (bank 4577). Needs: verify discovered() contents for contact-
  only units, or add the discovery twin; plus a gas-available lurker
  game for the full chain.
  DATUM 2026-11-01 (game 20261101_165835, TauCross PvZ, loss f16591,
  long-horizon): NO lurker anywhere in 17k rows (gas-starved 6740/0 end,
  Forge f16259) - the hunt continues; lurker rate is ~1 in a few games,
  not every game.
  DATUM 2026-11-01 (game 115405 re-audited, POST-fix, gas-rich loss
  f28800): the TECH-evidence chain WORKS - Citadel/Templar tech evidence
  opens Robo dispatches f16518 (pre-breach), Forge warps f16921, lurker
  #248 breaches f16985 in contact, Robo x3 (f19406/21812/23140) +
  Observatory f21433 warp. But TWO gaps, both certain: (1) ZERO Observer
  dispatches ever (addDetectionGoals emits Robo/Obs/Forge/cannons but
  NO Observer unit goal - plain omission in code); (2) ZERO cannon
  dispatches ever despite Forge standing + 7300 frames of post-Obs
  threat (goals emitted per code, vanish before dispatch - scheduler
  or dispatcher drops them, twin-blind: V2_GOAL_SOURCE only logs
  Pylons). The sighting path (122910: contact-only lurker, partial
  Forge-only answer) stays the other half. Next: Observer goal +
  cannon scheduling diagnosis, sim-pinned.
  DATUM 2026-11-01 (94 retained runs): ZERO DISCOVER-mention rows in any
  EVENTS file - no first-sight twin key exists anywhere in the tree, so
  contact-only units (like lurker #234, visible solely in CLUSTER rows)
  have no discovery path to `hasHiddenUnits` except the fog-loop repair.
  The twin gap is confirmed tree-wide, not per-game.
  DATUM 2026-11-01 (owner vehicle `ums/rav/protoss/P_v_Lurkers2.scx`
  + live Orders log): units walk into invisible Lurkers and die there
  with a `ClusterForm/Regroup <-> ClusterGhost/GhostAdvance` flap every
  ~12 frames and NO attack ever issued. Root cause in code: the fight
  branch rightly ignores undetected enemies (`Engagement` filters
  `!isDetected`) but `GhostAdvance.ghostPointSkippingFresh` served
  their remembered positions as destinations (no detection check) -
  the creep marched onto the spines while Regroup pulled back, forever.
  FIXED 2026-11-01: an `effUndetected` ghost (burrowed/cloaked with no
  cover, or a hp:0 fogged snapshot - both read `effUndetected` in
  `AUnit.java:1412`) is skipped unless a mobile detector (Observer/
  Vessel/Overlord) stands within 8.5 tiles of the unit - the
  enemy-detector convention mirrored. Pinned
  (`undetectedGhostIsNotMarchedWithoutDetectorCover` +
  `undetectedGhostIsMarchedWithDetectorCover`, GhostAdvance 24/0).
  Open: vehicle game on P_v_Lurkers2 (stand-off, then Observer, then
  kill - the #198/#223 full chain proof).
  DATUM 2026-11-01 (vehicle game 20261101_185829 on P_v_Lurkers2.scx,
  POST-fix, loss-of-6 in a 124-foe UMS pit, f1058 scenario end):
  the shape flipped - 206 SHOT / 33 HIT / 108 KITE rows and kill
  orders through the FIGHT branch (`Attack:Lurker#103/#152/
  ClusterReinforce`, +Reengage/Kite/Hunt) instead of the pre-fix pure
  `Regroup<->GhostAdvance` flap with zero attacks. The 2 remaining
  `ClusterGhost` rows vs Lurker#152 are fresh-contact approach (seen
  seconds ago), not fogged-march feeding. Full chain (Observer in
  time, zero-feeding kill) still open - no Robo exists in a 44 s UMS.
  FOLLOW-UP 2026-11-01 (owner: flap persists on his runs, f393-500):
  his `185102`/`185223` games built pre-fix jars (18:51-52, fix
  committed 18:58:01) - the log is the old build, not a fix failure.
  But it exposed the second half of the same hole: the FRESH branch
  (`freshContactFor`) had no detection check - a Lurker seen, then
  burrowed, kept its seat as destination for up to 240 frames, and
  arrival STANDS the army inside the spines. FIXED 2026-11-01:
  a fresh seat with an unhittable memory within 6 tiles (the spine
  envelope - burrowed Lurkers never drift) is suppressed without
  detector cover at the fight (`seatIsDark`), same 8.5-tiles rule.
  Pinned (`freshSeatGoneDarkIsNotMarched...` + cover twin, GhostAdvance
  22/0, combat batch 16/0 incl. `LurkerAmbushSimTest`). Needs a
  post-fix vehicle game on P_v_Lurkers2.
  PROCESS NOTE 2026-11-01: owner's `185223` has no GAME_END - it was
  still running when the agent's `185829` start killed leftovers. One
  game at a time was violated by overlap; agent discipline from now:
  `pgrep -x StarCraft.exe` + lock check before ANY launch, never
  alongside the suite or an owner game. JAR-RACE 2026-11-01: the
  background jar rebuild races game start (both `185829` and `191233`
  may have run pre-fix code - unverifiable post-hoc). Agent games now:
  synchronous `build-bot-jar.sh` + `check-jar-freshness.sh` FIRST,
  launch with `BUILD=0`, and a `GHOST_DARK_SKIP` twin (change-gated)
  names every dark-destination refusal so the vehicle adjudicates the
  filter instead of the clock.
  DATUM 2026-11-01 (owner: burrowing transition + unconditional Zealot
  flee): the avoid filter only knew burrowed Lurkers - a burrowING one
  (still detected, not yet burrowed) passed every gate and Zealots
  stood into the transition. FIXED 2026-11-01 in
  `AvoidLurkers.zealotMustFleeBurrowing`: a burrowing Lurker within
  radius means Zealots leave, before all bravery vetoes; detector
  cover at the unit is the only exception (it stays hittable =
  `effVisible`, the engage gate the owner named). Pinned
  (`AvoidLurkersTest` 4/4, red shown with the branch behind the null
  gate; combat batch 34/0). Faka `setBurrowing` added alongside
  `setBurrowed`. Open: vehicle proof Zealots break off as burrowing
  starts instead of dying in place.
  WIRING 2026-11-01 (vehicle `200942` + twin silence): the manager
  only ever ran for workers and scouts (`ProtossAvoidCriticalUnits`
  is referenced solely by `WorkerManager`/`ScoutUnitManagers`) - the
  army pipeline (`BattleCommander.act`) never evaluated it, so zero
  `AVOID_LURKER` rows across 1289 frames is structural, not a gate
  miss. FIXED 2026-11-01: `BattleCommander.act` consults
  `AvoidLurkers` after specialists, before engagement (FLEE keeps its
  routing); a move issued owns the frame. Pinned end-to-end
  (`armyZealotBreaksOffThroughBattlePipeline`: `RUN_ENEMY` move, no
  `ClusterGhost`; `AvoidLurkersTest` 6/6 + combat batch 36/0).
  Open: vehicle proof on P_v_Lurkers2 with the wiring live.
  PROOF 2026-11-01 (vehicle game 20261101_202007 on P_v_Lurkers2.scx,
  frozen jar WITH the wiring, win f827): ZERO deaths (vs 8 in every
  prior vehicle), 35 `AVOID_LURKER` rows across 5 Zealots vs Lurkers
  #101/#102 - the branch fires live, always with `burrowed: False`,
  i.e. exactly the transition the owner named. Army kites and shoots
  around the transition (259 KITE / 236 SHOT) instead of standing in
  it. `GHOST_DARK_SKIP` silent, `GHOST_TARGET` absent - no dark
  marches, no blind engagements. The Lurkers2 feeding chain is
  CLOSED sim-side and live-side: memory guard + seat guard +
  destination guard + unconditional Zealot flee + army wiring.
  DATUM 2026-11-01 (vehicle game 20261101_190800 on P_v_Lurkers2.scx,
  POST-fix-1, 23 s loss, 8 dead to Lurker#103, zero lurker kills):
  the instrument (not a guess) names the march: EVERY CLUSTER row
  reads `ghost: sweep` at region centers (61,55)/(37,57)/(74,76) with
  the foe closing 68->2 tiles onto them. The lurker-memory guards
  never fired because the ghost was never a memory - the UMS sweep
  marched at centers sitting inside the lurker group, arrival stood
  the army in the spines (flee only f533, `STAY_BASELESS`). FIXED
  2026-11-01: the destination gets the same darkness check as the
  memory - a dark center (unhittable memory within 8 tiles, no
  detector at the fight) is skipped: unexplored falls through to the
  sweep, the sweep rotates to the next safe center. Pinned
  (unexplored-dark-null + sweep-skips-dark + cover-revalidates,
  GhostAdvance 25/0, combat batch 16/0). Open: post-fix-2 vehicle
  (expect `ghost:` rows avoiding (61,55), stand-off instead of
  stand-in-spines); the PATH through dark ground stays uncovered -
  only destinations are guarded.
