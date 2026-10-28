# TODO — Atlantis (open issues)

Single source of truth for open work. Direction: `_AI/00-GOAL-APPROACH.md`;
execution order: `_AI/__MIGRATION.md`; operational learnings:
`_AI/NOTES.md`. Per-game evidence lives in `_AI/WINE-MACRO-DATUMS.md`;
this backlog keeps current conclusions and open questions (CONVENTIONS §26).

## Phases (the quarter)

- [x] **0 — Foundations.** Bounded headless loop, fast suite + frozen ArchUnit
  store, Java 1.8, twins + query tooling. Met.
- [ ] **1 — Production correctness (top priority).** The opening plays itself,
  every issuance attributable. Substantially met; remaining #95, #96.
- [ ] **2 — Decision board completion.** Per-gate legacy deletions, one game of
  proof each; exit: one issuer, one log. (Next steps 1–2, #91.)
- [ ] **3 — Combat cluster.** Structure done; behavior pending live proof.
  Blocked on #94 (no enemy on OpenBW).
- [ ] **4 — Placement cutover.** Catalogue default, legacy finder deleted
  (`PLACEMENT-CUTOVER-PLAN.md`, `redesign/03_PLACEMENT.md`).
- [ ] **5 — Production V2 LIVE + architecture migration.** V2 by default only
  after parity is established (#45; stages E–J below). One Wine victory showed
  stronger tech/army than same-day legacy, but parity remains unproven; open
  gaps include expansion siting (#150) and dual-authored Pylon demand (#144).
- [ ] **6 — Measurement and learning.** Scenario battery + parameter search;
  exit: an unattended tuning loop that improves a frozen table (#185).
- [ ] **7 — Expansion.** Second goal vocabulary, sweeps vs real bots. Nothing
  starts early.

Work rules: never batch rewrites; never gate commits on full games; never
trade a measurement for an explanation (§3a).

## Current owner priorities
1. Decision reasons in CherryVis (#214 - the army dies without a trace).
2. Fast, self-terminating OpenBW sims (§17).
3. Production V2 (#45; `redesign/01_PRODUCTION.md`).
4. Placement cut-over (`redesign/03_PLACEMENT.md`).
5. OpenBW/E2E testing (`IDEA-E2E-TESTS.md` §3.5).

## Process (normative)

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

1. **Retreat floor + choke standoff doctrine** (owner orders 2026-10-25):
   LANDED 2026-10-25 as #204 - (a) a retreat stops at the
   `MiddleFocusChoke` standing line, never behind it; (b) every choke
   standoff stands RADIUS + 6 tiles off the center (a 9-tile radius =>
   15 tiles). Pinned `RetreatFloorTest` 4/4 and `ChokeStandoffDistanceTest`
   3/3. Graded 2026-10-26 into the **retreat ladder** (commit `e06adb27d`,
   #204) - the floor now reads the ground the army last fought on instead
   of a single line - pins `RetreatLadderTest` 5/5 + `RetreatFloorTest` 8/8.
   Live proof still rides the next quiet game.
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
   #194 >10-Dragoon ban instance, #195 re-engage latch, #188 blocking
   geometry, #190 escalation rows, #197 trapped-Dragoon break-out, #200 Zerg
   main hold, undetected-target silence (needs a cloaked foe).
8. **Owner data before live verdicts:** #202 (minerals at the stall, dominant
   twin rows, Gateway count/state), #96 (painter labels + missions game),
   #34 (UMS opponent map).
9. **Long-term ordering:** #45 deletion order, rewrite order #144-#147, #8 V2
   LIVE runs, stages E–J below.
10. **Housekeeping:** drop the four stale worktrees (`w-zealot-gas`,
    `w-anti-flap`, `w-regroup-march`, `w-middle-focus`) once their content is
    confirmed inside commit ce4de4034; keep the box quiet between games.
11. **Sweep of #144–#204** (owner order 2026-10-26: work the next 40
    positions as Senior Engineer, autonomous, long-term answers first).
    Triage of the whole range: everything that needs a game or owner data
    stays in the one-game queue (step 7) or the owner-data queue (step 8);
    what is executable now runs in this order, each its own commit:
    (a) #211 suite attribution so the gate is honest [LANDED 2026-10-26:
    runner-integrity commit `3e7136a8e` + attribution correction], (b) #194's
    shared anchor/rally input resolver [LANDED 2026-10-26: `HomeGround`,
    commit `ea378db7f`], (c) #201 Corsair cluster sim-first [LANDED
    2026-10-26: membership rule `716a46142`, the `Alpha.gather` split
    stays open], (d) #184 one TargetLadder rule at a time with a sim
    no-regression proof, (e) sim-only slices of #188/#190/#192.
    #156 stays parked until 2026-11-09 (owner: no blind merge), #144–#147
    wait on parity runs (review 2026-11-09), #160/#185 wait on K-of-N data
    (review 2026-11-09). #206–#210 are new owner findings
    from the 2026-10-26 games and slot into the same queues.

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

## Architecture migration — stages E–J

- **E · #3** Move remaining `FoggedUnit` readers to `UnitSnapshot` only after
  World owns lifecycle; current readers are battle-track dependencies and wait
  for E/H.
- **F · caches** Complete: `UnitIndex` owns frozen per-frame lists; `Select`
  query caches and unexplained TTLs were removed. Historical inventory:
  `DOCS/SELECT-CACHES.md`.
- **G · #8 race strategies** `ProductionRacePolicy` + three race policies are
  landed and live-proven. V2 parity remains open; compare Pylon timing side by
  side before deciding whether early warp is pull-forward or waste.
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
  No retained game has shown FROZEN_WARP or recurrence of the old silent-stall
  class. → details: `WINE-MACRO-DATUMS.md` §3

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
  detailed ledger adjudication is archived in `WINE-MACRO-DATUMS.md` §3.

- **#152** Dragoon kiting: instrumentation, targeting, escape and tuner are in
  place; the latency ladder improved hits while retaining wins. Open: 0–2-hit
  goal via #186, wall-aware steps after production, macro proof with attributable
  foes, and K-of-N tuner efficacy.

- **#149** Causeless early exit: one occurrence was the hoard tripwire by design;
  the other clean, enemyless exits have no bot-side cause. Engine-side termination
  remains open; keep `GAME_END` as witness. No recent recurrence: 9 retained
  games 20261027_183253-20261028_004959 end as losses at various frames or
  28800 time-ends, none clean-and-enemyless.

- **#71** Readiness gate: still lacks the deciding 700 s+, one-base, low-bank
  run. Wine's default supply horizon prevents reaching it; a per-run horizon is
  required. No blind tuning.
  → details: `WINE-MACRO-DATUMS.md` §4

- **#81** Corner-stall Pylon search: evidence favors tile doctrine, not leash
  relaxation; corner Pylons land and supply unblocks. Last-resort ladder is
  pinned but has not fired in retained games.

- **#84** Wine Pylon issuance: observed stalls self-recover; pipeline audits
  remain healthy. `WarpStartedTwinTest` covers the builder path and siteless
  CANCELs carry cid/oid/reason. Open: verify STARTED-twin naming on a legacy-
  minted warp; the remaining re-request-door gap is per-warp, not per-game.
  DATUM 2026-10-28: 0 legacy-minted warps in 4 audited V2 LIVE games
  (003802, 000314, 183253, 004959 - every warp carries a V2 `At 0` order),
  so the shape is unobservable in this mode; needs a non-V2 game or the
  legacy path to mint one.
  → details: `WINE-MACRO-DATUMS.md` §5

- **#74** Wine builder variance: observed stalls recover; alternating handoff
  and 1.4-tile parking remain unobserved. Builder twins require `BuilderLog`,
  so flag-off games cannot settle this.
  DATUM 2026-10-28 (game 010715, BuilderLog ON, VICTORY): multi-builder
  sites are sequential replacement over minutes (Pylon 109,17: 6 builders
  f13950-15030, all IN_PROGRESS; no A-B-A alternation), 1.4-tile parking
  present but rare (17 STARTING rows on 2 sites). Rapid alternation still
  unobserved.
  → details: `WINE-MACRO-DATUMS.md` §6

- **#85** Armor research is independently gated from weapons L3 (stub-pinned);
  current thresholds are owner tuning, not a defect. Singularity fires live, but
  post-Forge L3 and a longer research window remain unobserved.
  DATUM 2026-10-28: zero Forge warps in 9 retained games (183253 through
  010715) - the whole Forge line never presents in these openings; needs a
  build that takes one.

- **#82** Terran off-site completion may leak (unobserved): the bot plays
  Protoss-only (D8), so the shape cannot appear. DATUM 2026-10-28: same
  class as #84 - 11 V2 LIVE games audited, Terran never played, zero
  off-site shapes; unobservable until a Terran game happens. Needs a game
  showing it. No blind fix.

- **#92** Second Gateway lateness is intermittent; no blind patch. The stable
  pattern is a +1-supply lag on #2/#3, not a multi-minute delay; all three
  Gateways warped in audited games. DATUM 2026-10-28 (10 retained games):
  GW#1 f1963-2157 supply 10-11, GW#2 f2502-3458 supply 12-17 everywhere -
  no multi-minute delay in any game; GW#3+ lands only in long games and is
  now V2-dynamic (proved 003802, see #192).
  → details: `WINE-MACRO-DATUMS.md` §7

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

- **#45** Production V2 deletion order: prove LIVE parity before default flip;
  delete dynamic commanders only after tech/race-army coverage, `Queue/**` and
  order types last; retain `Construction/**` (V2 delegates builders). Tech, gas,
  Singularity and Dragoon demand fire live; the owner-ordered ASAP Dragoon rule
  is in `DynamicGoals.addArmyGoal`. V2 remains OFF until parity. Recorded
  “Protoss rich but idle” shape has no verdict.
- **#145–#147** Rewrite order, clean-room: each lands specced, stub-pinned,
  default-flipped with a game proof, then the legacy files move to `src-old/`.
  (#144 closed by e203c6121 with the 104542 VICTORY proof. WO-144 supply-gate
  roster complete on the new jar: 183253 (long) + 190104 (short) + 192937
  (full length, f28800) - duplicate-free openings everywhere, no warp at
  cap, Pylon@8 at exactly 8 in all three. Authorship races both ways
  (legacy built Gateway#1 in 183253, V2 in 190104/192937) - mixed operation
  in LIVE mode stands, it is WO-147's question. Deletions per the WO order
  are the owner's call.)
  - **#145** Legacy site-finding chain → catalogue-only siting; delete the chain
    after cutover. The [119,12]/`Null position` storm mints here (no
    standing-cover check), so the cutover is the fix. `SITE_TOO_FAR` only rejects
    absurd distances (`BuildSiteDistanceTest`). WO-145 STOP 2026-10-28 (A/B
    game 104128, PLACEMENT=catalogue, TauCross): 3988 CONSTRUCTION_CANCELs,
    stall-death f6900 supply 29 (flag-off games: 0-1 cancels, full games).
    The catalogue returns COVERED tiles - Gateway warps at [94,112] f3841
    then 2088 proximity-finish rows on the same tile, Pylon/Nexus rows die
    builderless (1522+368 `looks bugged`: covered base tiles trip the
    inRadius(12)>=4-workers builder refusal). Same missing standing-cover
    check as #151, now measured on the catalogue arm itself. Per WO stop
    rules: flip stopped, flag stays default-off; the ARM needs the cover
    check first (spiral guard or planner-side exclusion), then a new A/B.
  - **#146** Delete `APositionFinder` after catalogue default and parity (D1).
    Exit: zero `Can't find place` across retained runs; current failure volume
    remains above that bar.
  - **#147** Replace `Queue/**` and order types LAST with stateless demand and
    scheduling. Exit: V2 LIVE by default with parity; legacy queue remains the
    only queue until then.
- **#155** `ProduceBuilding` gates are an ordered registry; the core-single-
  instance deadlock is fixed. Narrow `power-not-funded` and `live-construction`
  gates remain stub-pinned by design. Live resite proof rows are oid-linked;
  adopt/warp tile offsets are coordinate conventions, not resites.
  → details: `WINE-MACRO-DATUMS.md` §9.
- **#156** Unify the four reservation models behind one queue-position ordering
  and one paid-work exclusion (a building unit exists). Duplicate-Pylon races
  currently read different sums. This belongs to #145/#147; no blind merge.
- **#157** Semantic `ConstructionOrderStatus` queries are in place; all
  PREPARING readers use `isWaiting()` and `setStatus` is the sole writer.
  Behavior-neutral migration; reader proofs ride normal macros.
- **#160** Automated tuner selection machinery is implemented and pinned,
  including objective, hall of fame, promotion, manifests, gradient and sim
  batch. Efficacy remains unproven; duel noise dominates at K=3. Require K-of-N
  replicates before promotion.
- **#164** OpenBW takeover: `/sc-ai/OpenBW/openbw` is canonical; the harness
  copy is a build artifact. Install engine changes only through
  `scripts/build-openbw.sh --install`, then run host smoke and engine tests.
  Wine remains the 1v1 proof vehicle; `M&M_v_ZealDrag` is the OpenBW vehicle.
  Do not touch `src/main/Main.java`.
- **#166** Independent OpenBW work is scoped in `OPENBW-HANDOFF.md` (T1–T7,
  M1). No done-signal yet; do not take over or verify until handoff, then rerun
  the host smoke and engine tests. Owner decisions are tracked separately.

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
- **#171** High-ground choke hold: high side is preferred by the sim-pinned
  `HighGround`/`ChokeStandoff` path; it correctly stays silent vs ranged
  enemies. Live proof still needs a melee-choke game.
- **#172** Region-aware escape: sim-pinned walkable candidates and
  corner/obstacle/enemy scoring are implemented. Open: live superiority proof
  and tuner efficacy. No center-pull prior (it would bias against measured edge
  risk).
- **#173** Escape doctrine is long-vector-first. The 5-tile ray deflects at full
  length when blocked and engaged live on map corners; angle-stepping is parked
  (no natural trapping geometry found). Duel hit reduction came from #152's
  per-cycle standstill lever, not step amplitude. Open: live walled-pocket
  engagement and whether a deflectee glues.
- **#175** Arc retreat toward the squad (low priority): sim-pinned and
  tuner-controlled (`EscapeArcBlend`), default blend 0. Open: contact-aware
  bending; no live value is selected.
- **#178** Zealot kiting: zealots never yield, so against faster attackers they
  stand through the cooldown and bleed. Rule: after attacking with cooldown
  left, withdraw a step when wounded, locally outnumbered, or facing a faster
  attacker - but never break the line on open ground. Landed sim-first
  (`ZealotWithdrawSimTest`, mutation-proofed, `ZealotWithdrawTest` 5/5) and
  live (272 stepped-aside, zero standing deaths; no freeze in any later game).
  HOLDS 2026-10-28: VICTORY 010715 loses 4 Zealots / 5 Dragoons in combat,
  no standing-death shape; KITE frozen-reset fires 46x as the designed
  recovery (not a freeze).
- **#179** ZealDrag coordination/hold-fire is sim-pinned; live losses showed
  mobile units, not freezing or coordination failure. Open: winnability against
  real opponents (0/9 FFA wins; one weak positive with Dragoon mass).
  DATUM 2026-10-28: game 010715 won vs the native AI with 24 Dragoons / 7
  Zealots - mobile, no freeze; FFA winnability still open.
- **#180** Per-unit telemetry samples HP, Zealot/Dragoon distances and nearby
  enemies every 24 frames. Diagnostic-only; thin it after #184's ladder lands.
- **#181** Split-cluster reinforcement is sim-pinned (fresh-contact reinforce,
  ghost-contact rejection, ongoing-fight priority). Timeouts were the Regions
  path. Open: live recurrence of the park-while-fighting shape.

Priority note: #170-#184 slot BELOW the production ratchet (#45, #144-147) -
small sim-pinned slices, game-verified one at a time. Combat logic stays out of
the commanders (CONVENTIONS §23): new code lives in `FormationHold` /
`FightTarget` / `AcquireTarget` seams.

- **#182** FormationHold's contact veto and holder-unstick are sim-pinned; no
 200f stand has recurred. Pocket geometry explains the remaining stand-and-trade
  shape. FrozenWatch's DEPLOYED gate stays (decision recorded).
- **#183** Steady holders cannot use manager-history `isUnderAttack()` because
  HP is refreshed every frame. `DuelMemory` now latches battle-frame damage; it
  is a backstop, while VETO/guns-in-range usually decide first. The one active
  Protoss consumer migrated; remaining consumers are Terran-only or dormant.
  VERIFIED 2026-10-28: zero production callers of `isUnderAttack()` outside
  `AUnit` itself - the only `isUnderAttack(n)` reader is `UnitStateManager`
  (the HP trail infrastructure, not a decision) plus Terran-only turret logic.
- **#184** TargetLadder owns the explicit nine-rule order; five rules are
  extracted (chewer, keep-current, weakest-in-range, sticky, shared),
  four still delegate to legacy implementations. Sim-pinned slices
  cover chewer selection, re-arm/step interaction and undetected targets. A
  forced finish on a 10-HP chewer donates the Dragoon, so a faithful finish rule
  waits for post-cut ladder work. Coordination preferences remain removal
  candidates, each requiring a sim no-regression proof.

- **#185** Learn WHEN to use the fixed behavior set from features; synthetic
  scenarios and varied enemy policies only. Sequenced after the production cut;
  spec and gates are in `_AI/NN-PLAN.md`. Test scope now has five zombie policies,
  feature/episode CSV and an S1 learned hold-early gate. Limitation: held-out
  runner behavior needs runner examples in training. Open: feature export is
  test-side only; fidelity Stage A precedes any production learner.

- **#186** `ProtossAttackHoldToShoot`: sim-pinned stop-point model accounts for
  braking, windup, latency and chaser commitment. Live 1Drag retained wins and
  the V2 hit band, but did not isolate a gain. The 0–2-hit target needs harder
  Stage B scenarios; the A/B toggle remains available.
- **#187** Scout should remain near a discovered enemy main until chased out;
  time cutoff and separation fallback are sim-pinned. `SCOUT_WATCH` now records
  the trail. First live trail never discovered the enemy main, so discovery
  coverage is the new open question. ROOT CAUSE FOUND AND FIXED 2026-10-28
  (B-43): `EnemyMainBase.get()` cached its first miss forever (`Cache` TTL
  -1) - combat focus reads it from the early frames, the scout later stood
  within 1.0 tile of the enemy start, and every reader kept the early null.
  Measured 10/10 retained games at 0/5111 known-enemy scout rows (183253
  through 004959, all V2 LIVE TauCross). Fix: misses are never cached, only
  the hit latches, plus a per-game `clear()` in `OnGameStarted`; pinned
  `lateDiscoveryStillLatchesTheMain` (red-first: null despite late
  discovery on the old code). Open: live proof that the main latches and
  the army finally leaves. LIVE PROOF 2026-10-28 (game 010058, first jar
  with the fix): main latches f3658 at [95,119] (scout at [85,118]),
  359/436 scout rows known thereafter. Mission flips Attack f4705 and the
  army marches to the enemy main (92 CLUSTER rows anchor->focus [95,119],
  contact foe 836 at f7502), returns on Defend f7547, holds home correctly
  (foe -1, no latch - #206's hide-shape needs a post-contact redeploy case,
  this game came home mission-ordered). Loss f14785 in the final home
  defense (strength bled 340->118); combat quality, separate slice.
- **#189** Log priority adoption is complete: console levels and event `lvl`
  follow CONVENTIONS §27; diagnostic event volume stays MED by design. TEMP
  lines are session-only; latest source sweep found none. SWEEP 2026-10-28:
  zero `[TEMP]` lines in production code (only the mechanism docs); one stray
  live-path `System.out.println("PvP Zealot dont")` found in
  `ProtossTooBigBattleToRetreat` and removed (verified: full suite 1694/1694).
- **#188** ClusterFlee overhaul: sim pins cover homeward rally, organized
  retreat, and fighting through a blocking army; live audits found no blocking
  instance or scatter. B136's earlier “blocking” reading was withdrawn; the
  observed shape was a cut-off rearguard at the attack destination. Open: live
  blocking-geometry proof and cohesion through a lost fight. MOVE_FAILED
  refusal twins are ready; unstick remains parked until the shape recurs.
  DATUM 2026-10-28: game 010715 (VICTORY) carries zero MOVE_FAILED rows and
  no scatter - the shape does not recur here either.
- **#190** Swamped cutoff dragoons glued in place: lethal live instances were
  validated; the KITE twin distinguishes no-step from engine refusal. Sim-pinned
  escalation now rallies hopeless or stuck-and-chewed Dragoons through the shared
  retreat path, including FIGHT; Zealot last-stand is unchanged. Live adjudication
  of the new escalation rows remains open.
  DATUM 2026-10-28 (game 010715, VICTORY): KITE fires live 482 rows
  (stepped-aside 244, stuck-no-step 192, frozen-reset 46) with 20
  ESCAPE_RALLY rows in the same fights - the twin vocabulary for the
  adjudication exists; per-unit stuck-to-rally linkage still needs trace
  work. LINKAGE CLOSED 2026-10-28 (game 013929, KITE twin carries unit ids):
  7 units hit stuck-no-step, all 7 later stepped-aside or frozen-reset -
  the escalation path resolves stuck Dragoons live. Hopeless-rally half
  (ESCAPE_RALLY 2 rows) still thin.
- **#192** Gateway demand was repeatedly cancelled on null position during
  battle. Sim-pinned fixes cover self-overlap, ground-distance search, Pylon
  anchoring, completed-footprint overlap, and bounded WAIT (480 attempts; the
  stuck-loop guard is delayed, not removed). WAIT engaged live; orders survived
  failed searches and warped. Longer-siege calibration remains open; reservation
  release while waiting is tracked in #202. LIVE PROOF 2026-10-28 (game 003802,
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
  Open: formation adaptation and a CherryVis row. Shared input resolver
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
  DATUM 2026-10-28: the absolute >10-Dragoon main-choke ban is PvP-only by
  code (`We.protoss() && Count.dragoons() > 10` in
  `ProtossDefendFocusPoint`) - game 010715 fielded 24 Dragoons vs ZERG, so
  it cannot trigger the ban; the "longer game" it needs is a long PvP.
  DATUM 2026-10-28 (game 012238, PvP Destination, loss): 10 Dragoons trained
  / 8 died, live count never exceeds 10 - the ban stays correctly silent;
  still no game masses >10 live Dragoons vs Protoss.
- **#195** Flee reasons are named on CLUSTER rows; #190 escalation uses the
  shared retreat decision. Landed 2026-10-25 (commit ce4de4034): 120f
  re-engage latch, except eval > 1.1 (`RetreatReengageTest` 3/3). Live proof
  remains open. DATUM 2026-10-28 (game 010715, VICTORY): fleeReason fires
  live (RALLY 73, ESCAPE_RALLY 20, STEP_CUTOFF 1 across 94 FLEE rows);
  flee-clear to next-FIGHT gaps 31-248f, median 124, 12/25 within the 120f
  latch window - consistent with the latch biting when eval allows and the
  rally holding otherwise. Full proof needs eval in the twin.
  EVAL LANDED 2026-10-28: CLUSTER rows carry `eval` (leader's eval x100,
  -1 with no live leader), so the next flee-to-FIGHT gap adjudicates
  from EVENTS - eval > 110 inside 120f re-attacks, the rest holds the
  rally. Pinned both ways in `ClusterTwinDestTest`.
- **#196** Workers have a named 10-tile limit measured from the main for
  entry/attack/stop/help-Cannon (`WorkerFarFightTest`). Open: a pressure game
  where raiders reach the mineral line; native-AI macros produced no pressure.
  The same livelier vehicle would help #192 calibration and #188b.
  PARTIAL LIVE 2026-10-28 (game 012238, PvP): the vehicle exists after all -
  enemy raiders killed 10 probes on the line f15086-15707. The rule engages
  live and tagged: `#Attack_Unit/Swarm!` then ordered `-WorkerDefenceRun`
  + `#Move/RunToDist` (units 171/190). The line still fell when overrun -
  engagement proven, rescue unproven.
- **#197** Trapped-Dragoon posture is sim-calibrated and latched; the trapped
  rule lives in `atlantis.combat.focus` with #194. Open: live proof of an army
  attacking out of a trapped defense; inventory other posture reasons later.
  DATUM 2026-10-28: postures observed live are only ATTACK/DEFEND (010715:
  344/39; 012238: 312/103; 010058: 92/270) - no TRAPPED row ever surfaces
  in the twin; the latch is silent or never triggers in these games.
- **#198** Specialist behaviors landed sim-pinned: scarab production, Corsair
  hunting, attacked DT retreat, and High Templar storm selection (including
  deep-energy tiers and snipes). Observers use their own detector chain and fall
  through the specialist dispatch. The 195-energy single-target tier is rarely
  expressible in sim; live geometry remains its evidence path.
  DATUM 2026-10-28: zero High Templar (also zero Corsairs/DT/Scouts) warped
  in 3 audited V2 macro games (010715, 010058, 012238: Zealot/Dragoon-only
  armies) - the whole specialist tier never presents; needs a build that
  techs there, same vehicle problem as #85/#201.
- **#200** Against early Zerg, fewer than five combat units hold at the main
  when the midpoint exceeds six tiles, leaving room for workers to join
  (`NoContactHoldTest`). Extended 2026-10-26 by the first-Dragoon gate
  (#205): a dragoon build holds the main regardless of that count until its
  first Dragoon is finished. Open: live proof. DATUM 2026-10-28: game 010715
  (PvZ VICTORY) shows first shots at f9885 - no early-Zerg pressure at all,
  so neither #200 nor #205 can be proved by this vehicle; stands with #196
  (needs a livelier opponent than the native AI).
- **#201** Corsairs trail the main army instead of hunting: they need their own
  (default solo) cluster, merging only when threatening air exists; the
  "numerous Wraiths" line becomes a named constant with a pin (AEC §31).
  Queued behind #195/#190; sim-first. Membership rule LANDED 2026-10-26
  (commit `716a46142`): `CorsairCluster` in `atlantis.combat.cluster` -
  solo by default, merges on any visible non-Wraith air combat or
  `NUMEROUS_WRAITHS = 3` known Wraiths, pinned `CorsairClusterTest` 10/10
  (pure math + live stub-world on both conditions and the boundary),
  mutation-proven both ways. The `Alpha.gather` split landed 2026-10-26:
  solo Corsairs stay out of Alpha (CorsairHunt still owns them), merging
  only when the rule fires, pinned `CorsairGatherSplitTest` 2/2. Open:
  live proof the solo flight hunts. DATUM 2026-10-28: game 010715 built 0
  Corsairs (24 Dragoons / 7 Zealots, VICTORY) - the proof needs a game that
  builds them.
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
  causes are the covered-tile Pylon cancel loop (fixed, see #219), no 3rd
  Gateway author (V2 demand goal landed, needs its own live proof), the
  Nexus refresh chase (unfixed, needs #150), and an engine supply-refusal
  the twin mislabels "no free facility". The WAIT+reserve interaction
  itself was never observed to starve anything.
- **#203** Zerg matchup: landed 2026-10-25 (commit ce4de4034) `ProduceZealot`
  brakes excess Zealots at >=200 gas when a Core exists and the minimum Zealot
  floor is met; initial Zealots remain allowed. Pinned by
  `ZealotGasBankBrakeTest`; live proof remains. DATUM 2026-10-28 (010058):
  gas banks 200+ only in the final 70 frames (880-888 at f14717-14747,
  game ends f14785) - the mid-game brake window never presents, so this
  vehicle cannot prove it; needs a gas-rich mid-game.
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
  a passive base when sent. ATTACK_DECISION LIVE 2026-10-28 (game 110910):
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
- **#209** Units deep in our own main stack on each other (owner live
  finding 2026-10-26): blocked units inside the base occupy the same
  ground instead of spreading, i.e. the unstick/holder logic assumes open
  ground or an enemy in front. Sim proof landed 2026-10-26
  (`PocketSpreadSimTest`): formation slots must be standable, not just
  walkable - the engine's walkability is terrain-only, so a slot inside a
  building read valid and the march wedged on its wall;
  `FormationSlots.valid` now refuses building tiles and the line spreads
  around them instead. Checked against #182's holder-unstick: displacing
  units never trip the wedge watch, and its pins stay green.
  LIVE SUPPORT 2026-10-28 (game 012238, PvP home defense f14964-16370):
  966 combat-unit telemetry samples, 5/586 pairs within 1 tile - the line
  spreads at home, no stacking; wedge fix holds live.
- **#210** Formation orders while defending at most once per 10 frames
  (owner order 2026-10-26): re-issuing formation orders every frame during
  a defense churns the line. Landed 2026-10-26: shared `FormationOrderLatch`
  - identical re-issues wait out the window, first deployment and changed
  destinations always flow, leaving DEFEND forgets the latch; denied
  callers hold instead of falling through (pinned `FormationOrderLatchTest`
  4/4, red-first on the deny path). Measured first: 120 identical orders
  per unit per 120 defended frames in the sim. Open: live proof the line
  calms down. DATUM 2026-10-28: no formation-order twin key exists, so the
  calm cannot be read from EVENTS - needs trace geometry or a new row.
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
  Task half is #221. (a) half: CATALOGUE_SITE carries type/result/reason/via
  but NO nearTo/chosen tile - the Pylon WHERE is still missing.
  WHERE LANDED 2026-10-28: answered rows carry nearTx/nearTy from the
  request constraint (neighbourhood centre or exact pin, -1 otherwise),
  so a far-away warp names its origin without tile forensics. Pinned pure
  (`TargetPlacementNearTest` 4/4) + answered row shape (13/13 in
  `CatalogueSiteAdapterTest`); declined rows unchanged.
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
- **#219** Late-game Pylon goal/dispatch storm with zero warps (measured
  20261027_183253, V2 LIVE): f24730-28800, 1568 Pylon goal rows and 120
  `builder committed` dispatches, no Pylon warp after f24617. CANCEL
  COMPONENT CLOSED 2026-10-27/28 by the top-left overlap fix: 234528 and
  000314 (both post-fix jars) show zero CONSTRUCTION_CANCEL in full games
  (was 120 + 256), including an adjacent placement that warps (Pylon
  [16,39] 2.0 tiles from standing [16,37], f27937 - the old math cancelled
  exactly this shape). REMAINDER: per-frame re-fire while a Pylon is in
  flight (000314: 481 late dispatches, 1 warp) - absorbed same-tile, no
  cancel, no stall, just churn. Needs the builderLog game to confirm
  zero-cancel persistence under siege pressure. BACKSTOP LANDED
  2026-10-28: dispatch-time revalidation (`ProductionDispatcher.dispatch`
  with this frame's goals) - a fresh non-prerequisite item nobody asks for
  is skipped as `stale: no live goal`, prerequisites and committed
  re-offers always flow, pinned in `ProductionDispatcherTest` (4 new).
  The 000314 opening pair (BO goals f3996+f4333, dispatches same frames,
  warps f4349+f4474 at supply 23) is undecided from the twin: re-fire of
  one row in the dispatch-to-warp walk window, or the next row firing on
  a supply the twin does not log - either way the backstop now drops a
  second dispatch nobody asks for. Live proof rides the next macro.
  LIVE VERDICT 2026-10-28 (game 003802, first jar with the backstop, V2 LIVE,
  loss f21030): zero `stale: no live goal` skips in the whole game - every
  dispatched item had a goal, so the backstop stands as defense-in-depth with
  twin telemetry, not as a demonstrated save. The per-frame storm shape is
  explained, not fixed: the Gateway f19442-20537 run (~600 `builder committed`
  rows, 0 warps) is the committed re-offer path doing its job - one pending
  GW#4, same tile, and its single BUILD_ATTEMPT f19950 reads engineAccepted
  with constructing=true; the game ended f21030 exactly one Gateway build
  time later, mid-warp. Churn, not stall. Remainder narrows to goal-layer
  re-fire frequency (a goal per frame while in flight), harmless while
  same-tile dedup absorbs it. GATEWAY FLAVOR 2026-10-28 (game 010715,
  VICTORY f15540): 1 CONSTRUCTION_CANCEL, `Gateway proximity finish:
  standing Gateway covers [108,6]` f12960 - the same re-fire shape absorbed
  by proximity finish instead of same-tile dedup; no stall, game won.
  Zero-cover false positives remain at zero. Game 013929 (full-length loss
  f28800): zero cancels, GW#3 warps f9989 supply 53 on the V2 order, zero
  stale skips - the backstop still never fires because every dispatch has
  a goal.
- **#220** Runby screening gap (measured in `LingFloodHoldsTest`, P3 shape
  of 115321): 2 staged Zealots fall back to the Nexus (both at (10,9) by
  t200) instead of screening mid-map, so a 4-ling runby is fought at the
  mineral line - 4/6 probes fall vs chase-nearest, 6/6 vs focus-weakest,
  1 Zealot dies in both. Lings die and the Nexus stands (that half is
  pinned). Suspected eval conservatism at 2v4 rallying to the anchor -
  the #206 shape. Combat-cluster tuning with live proof, not economy
  work.
- **#221** Task slot empty for army units (owner ask, measured live
  20261028_000850, V2 LIVE TauCross): 10 of 26 units with `#Attack` rows
  (6 Zealots ids 137/143/144/151/159/160, 4 Dragoons 180/181/187/188)
  have no `units_updates` entry at all - the static battle functions
  never call `usedManager`, so the managerLog funnel that feeds Task
  never sees them. Manager-driven units are fine (15/25 Task entries).
  The `#Attack/<behavior>` order-watcher tags already cover the log
  half (114/114 attack rows tagged in the same game). Fix direction:
  tag army units with their cluster/mission behavior through
  `usedManager` (or wire the disabled tooltip channel change-only) -
  needs a trace-size check, not a guess. (Log half rides #214(c).)
  LANDED 2026-10-28: the tooltip channel is wired change-only
  (`ACherryVisUnitLogger.tooltip` with per-unit dedup, rendered into
  `units_updates` as union frames with carry-forward, empty keys omitted
  so the legend stays clean). Pre-live size estimate (hundreds of rows)
  was wrong - callers flap tags per frame, so the first live game wrote
  114k tooltip rows / 4.2MB raw (245KB zst); see FLAP FINDING below.
  Pinned in `CherryVisUnitLogsTest` + legend agreement.
  OWNER MECHANISM LANDED 2026-10-28: the unit itself holds its active
  behavior (`AUnit.behavior`, set by whoever drives it - `usedManager`
  names the manager, the order funnel names the tooltip), empty updates
  never erase it, and the order watcher keys on it with tooltip fallback.
  Units_updates tooltips ride the CHERRYVIS_TOOLTIPS flag (default off);
  the grouped units_tooltips section stays as the minimal machine format.
  LIVE PROOF 2026-10-28 (game 013929, first jar with the channel): Task
  populates - 130 units in units_updates, tooltip-only rows for
  managerless units (army included). FLAP FINDING in the same game:
  formation tags oscillate ClusterForm<->ClusterMove per frame (unit 141:
  ~9800 tooltip rows vs 128 command rows - tag-writer disagreement, not
  movement churn) and the scout re-issues Explore/HoldCourse constantly
  (unit 3: 27k tooltip + 5.7k command rows); trace 0.5MB -> 4.2MB raw
  (245KB zst). The channel is faithful; the callers flap. Fix direction:
  single tag owner per unit per frame, or accept the volume.
- **#222** Sim order-fragility (pre-existing, untouched): `CutoffSwampedRetreatTest`
  flips under suite order - razor margin (measured 7.01 vs threshold 8) with
  iteration order keyed off the global FakeUnit id hash, so a shifted id
  counter reshuffles the cutoff evaluation. Reproduction recipe: full fast
  suite (solo and package runs stay green); trigger is intermittent, not
  deterministic. DATUM 2026-10-28: solo 1/1 green, `tests.unit.sim` package
  108/108 green, full suite 1693/1693 green twice - no flip today. Do NOT
  fix with a global FakeUnit id reset (reshuffles all order-sensitive
  expectations) and do NOT touch from the builder-log harness (world()-based
  setup perturbs global state and flips it - `BuilderOrderLogTest` uses
  direct BaseSelect mocks for exactly this reason).

## Session ledger (owner-ordered handoff file, 2026-10-27; context saver)

Landed this sweep (all suite-green at commit, arch baseline unchanged):
posture thresholds+fallback+Terran/mirror/Input (`4a89f42c`, `63ab38b9`),
reason texts + Reason board key + §34, CLUSTER strength+focus twin +
ArmyBoard game-values (`3d51a57b`, `f7c3ac7f0`), EnemyStrength
addressless fix (#215, `25c78b9b5`), Pylon nearTo branch codes +
PylonSite board (`e5483f96b`), ladder rules 6-9 (#184 complete:
`af5fbb01`, `28f36a2`, `efbf2b1`, `91e0531`), retreat rally scatter,
march line abreast, Alpha* board keys + FocusName tile (`c60035171`),
kill-leftovers stale-lock guard, cherryvis-start.sh, Closes #199,
#212-2 parked, #216 OpenBW hands-off, #212 parked, vehicle=Wine.
- Open: WO-144 step 3 needs a full V2 LIVE verdict (running below);
  #213 needs a two-base custom-map game; #215 needs a post-fix game
  with foe-unseen stretches; #214a catalogue arm pending #145;
  stance-from-focus rewiring (retreat rally/standoff read main-choke
  geometry, not DefendedPlace Kind - confirmed by reading, needs
  sim pins + game proof, NOT started); #212-2 parked to a stall game;
  #211 acceptance tier red (owner-only scope); #156/#160/#185 parked
  to dates/K-of-N; #144-147 wait on WO-144 step 3 + WO-145.
- Running now: Wine V2 LIVE long macro, TauCross, supply-120 env
  (`/tmp/opencode/ENV-WINE-V2LIVE`), super-fast headless, detached
  (setsid, survives tool timeouts). Read its verdict next: no supply
  blocks 17/17+25/25, no duplicate Pylon warps, SUPPLY_BLOCKED
  samples, tech timing, PylonSite rows, strength curve sanity.
- Next after verdict: adjudicate WO-144 step 3 (flip or name the
  blocker with twin evidence) → WO-145 A/B on Wine → WO-146/147
  deletion checklist → stance-from-focus sim pins.

## Sweep 2026-10-27 midday (both owner-reported, both landed suite-green)
- Retreat owns an escape route (`7e6db92da`, 6 pins): FLEE no longer
  marches home through the enemy - road within 6 tiles of an enemy
  combat unit reroutes to the farthest clear choke (farthest from the
  enemy army + enemy base; into-enemy and through-enemy candidates
  refused; all-roads-crossed steps locally). New verdict reason
  ESCAPE_RALLY tells reroutes apart in telemetry.
- Forge-before-Gateway fixed (`aeaf49bfd`, 3 pins): V2 fortification
  emitted a Cannon goal from frame 0 (expectedCannons >= 1 always),
  dropping legacy's Have.forge() + supply80/min200/2bases/mutas gates
  - the Cannon's Forge [pre] got dispatched while the Cannon itself
  never scheduled (0 Cannon dispatches in runs 104542/110507/115321;
  Forge f2112-2746 before Gateway, never in BO). No Forge standing =
  no Cannon goals now; baseline cannon returns once a Forge stands.
- REMAINDER (not started): scheduler still leaves a planned prereq
  behind when its parent goal fails to schedule (orphan mechanism
  above - needs timeline-dealloc design); V2 has no legitimate
  Forge/Cannon author (port ForgeGoal + CannonGoal board timing).
- WO-145 A/B: catalogue arm 115321 done - DEFEAT f4500 to ling flood,
  4 buildings, 1126 benign proximity-finish cancels (same shape as the
  healthy game's 668), env mechanism verified (placementCatalogue=true
  from ENV-WINE-V2CAT, V2 provably live in its log). Legacy arm
  (ENV-WINE-V2LIVE, same supply-120 conditions) RUNNING now, detached,
  log /tmp/opencode/wine-v2legacy.log - adjudicate A/B when it lands.

## Sweep 2026-10-27 late (StarEngine P0-P2 + gates, 6 commits, all green)
- P0 map ground (`7784c158a`): snapshot v2 (bases + regionReach, loader
  verifies matrix vs BFS), golden TauCross re-dumped. Found en route:
  the dumper passed tile coords to BWAPI isWalkable (walk coords) and
  scrambled the whole walkability grid - fixed by mirroring the bot's
  own *4 rule; buildable '.' 6227 -> 11263 (recorded in
  _AI/CHALLENGES/MapSnapshots.md). Without this no Gateway fit within
  12 tiles of the sim base.
- Ground truth + validation (`db9145086`): SimGround (single tile
  authority), SimOrder verdicts + OpenBW-derived checks, 9/9 pins.
- Arch (`abaae9f72`): SimWorld movement extracted byte-identical to
  SimMovement (one transcription slip caught by the sim itself: swapped
  atan2 args made builders flee - method-diff verified after the fix).
- FakeUnit sim-safety (`5cba72977`): sim-owned remaining-time fields,
  top-left tilePosition (center-based math put warps 4 tiles off; the
  refinery missed its geyser by a tile). e2e shows only the 3 failures
  that fail identically on unmodified HEAD (stash-proven pre-existing).
- Opening gate, sim (`dedba5ebe`): SimDriver plays Zealot-into-Goon
  to 100 raw supply through the real ProductionEngine - 11/11 BO rows
  covered at/after their gates, no non-BO building before Gateway,
  bank <= 550 below raw 50, zero invalid orders, exact golden warp
  frames. FAITHFUL builder walks the tile map (mover reused).
- Opening gate, Wine (`ed7e6f7cd`): strict file-order dropped, the gate
  asserts coverage at/after each row's gate (V2 emits gated goals, not
  a sequence - both retained live games interleave Assimilator too).
  104542/110507 fail ONLY on the real Forge intruder; 115321 fails
  honestly (died f4500).
- Open questions (measured, not verdicts): live mining runs ~0.03/miner/
  frame vs EconomyModel 0.045 (two Wine windows) - if the model is hot,
  plans run ahead of the bank; needs a proper measurement pass, not a
  guess. FAITHFUL full-opening + scripted enemy (P3) still open; WO-144–
  147 deletions now have sim gates to run against (not yet executed).

## Sweep 2026-10-28 autonomous (fog memory + twin proofs)
- Fog map heals on re-sight: live strength halved on contact loss (182 to
  364, twice in game 142713, same shape as 110910/012238) while the
  remembered part stood still - units the engine saw never entered the fog
  map. A re-sight arrives as show, never discover, and show never wrapped,
  so a lost unit stayed lost. `OnEnemyUnitShow` now wraps unknown enemies
  (enemy-only, known ids untouched). Pinned `FogMemoryResightTest` 3/3
  (red-first on the wrap, enemy-only, keep-known); `EnemyUnitsTest` still
  green. Live proof rides the next macro (fogN must rise where it stood).
- Twin proofs from game 142713 (DEFEAT f16693, contiguous EVENTS, clean run
  with no suite overlap): STRENGTH_WATCH fires (20 rows; first row already
  shows the fog gap foeKnown False, enemy 1), SCHEDULE_REFUSED fires
  (Zealot unaffordable f2368 - real early bank, not a stall),
  ATTACK_DECISION fires (5 rows, ENEMY_MAIN_KNOWN to [119,10]),
  CATALOGUE_SITE answered rows carry nearTx/nearTy, CLUSTER rows carry
  eval. Production: Core f6399, GW#3 f7565 supply 41, GW#4 f10440 supply
  49, no expo, 0 cancels, 0 stale skips. Two notes: (a) most flee-to-fight
  gaps read exactly 31f - the twin throttle, not the 120f latch, so #195
  needs a game with longer fights, not more rows; (b) leader eval reads
  987370 on 12/13 FLEE rows (one reads 1) - the raw eval domain, not the
  twin math; whether that branch is honest is the next question, not this
  commit.
- Process note: game 142025's EVENTS lost frames ~4000-22000 because the
  fast suite ran while it was live (out/ overlap). Its twin rows are void;
  its BUILDINGS facts stand (VICTORY f23850, Core f6504, GW#3 f10677,
  expo f15006, 0 cancels). No suite runs during a live game from here on.
