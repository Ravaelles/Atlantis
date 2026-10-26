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
  and that is honest.

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
  detailed ledger adjudication is archived in `WINE-MACRO-DATUMS.md` §3.

- **#152** Dragoon kiting: instrumentation, targeting, escape and tuner are in
  place; the latency ladder improved hits while retaining wins. Open: 0–2-hit
  goal via #186, wall-aware steps after production, macro proof with attributable
  foes, and K-of-N tuner efficacy.

- **#149** Causeless early exit: one occurrence was the hoard tripwire by design;
  the other clean, enemyless exits have no bot-side cause. Engine-side termination
  remains open; keep `GAME_END` as witness. No recent recurrence.

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
  → details: `WINE-MACRO-DATUMS.md` §5

- **#74** Wine builder variance: observed stalls recover; alternating handoff
  and 1.4-tile parking remain unobserved. Builder twins require `BuilderLog`,
  so flag-off games cannot settle this.
  → details: `WINE-MACRO-DATUMS.md` §6

- **#85** Armor research is independently gated from weapons L3 (stub-pinned);
  current thresholds are owner tuning, not a defect. Singularity fires live, but
  post-Forge L3 and a longer research window remain unobserved.

- **#82** Terran off-site completion may leak (unobserved): the bot plays
  Protoss-only (D8), so the shape cannot appear. Needs a game showing it. No
  blind fix.

- **#92** Second Gateway lateness is intermittent; no blind patch. The stable
  pattern is a +1-supply lag on #2/#3, not a multi-minute delay; all three
  Gateways warped in audited games.
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
- **#144–#147** Rewrite order, clean-room: each lands specced, stub-pinned,
  default-flipped with a game proof, then the legacy files move to `src-old/`.
  - **#144** `SupplyCommander` + fallbacks → board supply goal owns all demand.
    Exit: one author, pumps silent. Dual authorship is confirmed in DRY_RUN;
    routine macros cannot show the twin while V2 is OFF. Attribution of an
    actual duplicate Pylon pair still needs a storm game.
  - **#145** Legacy site-finding chain → catalogue-only siting; delete the chain
    after cutover. The [119,12]/`Null position` storm mints here (no
    standing-cover check), so the cutover is the fix. `SITE_TOO_FAR` only rejects
    absurd distances (`BuildSiteDistanceTest`).
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
- **#179** ZealDrag coordination/hold-fire is sim-pinned; live losses showed
  mobile units, not freezing or coordination failure. Open: winnability against
  real opponents (0/9 FFA wins; one weak positive with Dragoon mass).
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
  coverage is the new open question.
- **#189** Log priority adoption is complete: console levels and event `lvl`
  follow CONVENTIONS §27; diagnostic event volume stays MED by design. TEMP
  lines are session-only; latest source sweep found none.
- **#188** ClusterFlee overhaul: sim pins cover homeward rally, organized
  retreat, and fighting through a blocking army; live audits found no blocking
  instance or scatter. B136's earlier “blocking” reading was withdrawn; the
  observed shape was a cut-off rearguard at the attack destination. Open: live
  blocking-geometry proof and cohesion through a lost fight. MOVE_FAILED
  refusal twins are ready; unstick remains parked until the shape recurs.
- **#190** Swamped cutoff dragoons glued in place: lethal live instances were
  validated; the KITE twin distinguishes no-step from engine refusal. Sim-pinned
  escalation now rallies hopeless or stuck-and-chewed Dragoons through the shared
  retreat path, including FIGHT; Zealot last-stand is unchanged. Live adjudication
  of the new escalation rows remains open.
- **#192** Gateway demand was repeatedly cancelled on null position during
  battle. Sim-pinned fixes cover self-overlap, ground-distance search, Pylon
  anchoring, completed-footprint overlap, and bounded WAIT (480 attempts; the
  stuck-loop guard is delayed, not removed). WAIT engaged live; orders survived
  failed searches and warped. Longer-siege calibration remains open; reservation
  release while waiting is tracked in #202.
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
  scope green. Small-army main standoff and `guardSlotFor` remain
  follow-ups; `ClusterBreach`/`ClusterTactic`/`BlockingArmy` converted
  2026-10-26 (same pair, same answers - consumer pins green).
- **#195** Flee reasons are named on CLUSTER rows; #190 escalation uses the
  shared retreat decision. Landed 2026-10-25 (commit ce4de4034): 120f
  re-engage latch, except eval > 1.1 (`RetreatReengageTest` 3/3). Live proof
  remains open.
- **#196** Workers have a named 10-tile limit measured from the main for
  entry/attack/stop/help-Cannon (`WorkerFarFightTest`). Open: a pressure game
  where raiders reach the mineral line; native-AI macros produced no pressure.
  The same livelier vehicle would help #192 calibration and #188b.
- **#197** Trapped-Dragoon posture is sim-calibrated and latched; the trapped
  rule lives in `atlantis.combat.focus` with #194. Open: live proof of an army
  attacking out of a trapped defense; inventory other posture reasons later.
- **#198** Specialist behaviors landed sim-pinned: scarab production, Corsair
  hunting, attacked DT retreat, and High Templar storm selection (including
  deep-energy tiers and snipes). Observers use their own detector chain and fall
  through the specialist dispatch. The 195-energy single-target tier is rarely
  expressible in sim; live geometry remains its evidence path.
- **#199** Unwalkable regroup points spam the log (owner: HIGHEST priority;
  keep this line on close per owner order): `holdPointFor`/`baitPointFor`/
  `collapsePointFor` answered pure geometry and could land in a wall. Fixed
  (`WalkableRegroupPointsTest` red-first; producers nudge to walkable ground or
  answer null); ownership gate added after a renegade-unit trip
  (`ForeignUnitSkippedTest`). Four consecutive live games were clean. Same
  session also fixed cold-box startup failures in `run-wine-full.sh` and
  `build-bot-jar.sh`.
- **#200** Against early Zerg, fewer than five combat units hold at the main
  when the midpoint exceeds six tiles, leaving room for workers to join
  (`NoContactHoldTest`). Extended 2026-10-26 by the first-Dragoon gate
  (#205): a dragoon build holds the main regardless of that count until its
  first Dragoon is finished. Open: live proof.
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
  live proof the solo flight hunts.
- **#202** Production stall with banked gas: READY construction reservations
  may starve unit production while #192's bounded WAIT holds. Landed
  2026-10-25 (commit ce4de4034): release the earmark during WAIT and
  re-reserve on success, pinned in `GatewayWaitsOnBlockedBaseTest`. Owner
  bank rule landed 2026-10-26: 550 minerals covers everything, gas stops
  mattering at 300 (150 supply or less) / 400 above, gas-free orders never
  wait on gas - a rich bank flows through reservations and D3 waiters alike,
  pinned in `RichBankBypassTest`. Live verdict needs owner data on minerals,
  dominant twin rows, and Gateway count.
- **#203** Zerg matchup: landed 2026-10-25 (commit ce4de4034) `ProduceZealot`
  brakes excess Zealots at >=200 gas when a Core exists and the minimum Zealot
  floor is met; initial Zealots remain allowed. Pinned by
  `ZealotGasBankBrakeTest`; live proof remains.
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
- **#207** Army forms as if the enemy were in a different base (owner live
  finding 2026-10-26): an enemy base is detected, yet the army positions
  toward a base the enemy is not in. Open: line the scout/enemy-base memory
  (`EnemyUnits`, last-seen positions) up against the anchor chosen in the
  same frames of one trace and decide whether the stale base or the anchor
  choice is at fault - fix whichever one the frames convict, with a pin.
  Needs a game where both bases are known.
- **#208** Formation in our own base faces our own main (owner live finding
  2026-10-26): slots are laid out as if we were attacking our own Nexus -
  a sign/orientation error between the formation pivot and the threat
  direction. Sim proof landed 2026-10-26: a home anchor left behind the army
  no longer owns the pivot - `expectedDirectionPoint` faces outward past the
  army (at-anchor bunch and forward-anchor crescents unchanged), pinned in
  `ClusterRallyTest` (red-first: old slots arc at [31,20] around the main).
  Open: the same orientation live.
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
- **#210** Formation orders while defending at most once per 10 frames
  (owner order 2026-10-26): re-issuing formation orders every frame during
  a defense churns the line. Landed 2026-10-26: shared `FormationOrderLatch`
  - identical re-issues wait out the window, first deployment and changed
  destinations always flow, leaving DEFEND forgets the latch; denied
  callers hold instead of falling through (pinned `FormationOrderLatchTest`
  4/4, red-first on the deny path). Measured first: 120 identical orders
  per unit per 120 defended frames in the sim. Open: live proof the line
  calms down.
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
  second half and live proof.
- **#213** Pylon #3-4 sited at the natural (owner live finding): the
  `exclude(natural).last()` fallthrough in `PylonPosition.nearToForPylon`
  leaks the natural in whenever `Bases.natural()` is null with two bases
  (exclude(null) excludes nothing, last() is the newest base). Fixed:
  pylons < 4 pin to `Select.mainOrAnyBuilding()`, first/second-Pylon
  origins unchanged, pinned `FirstFourPylonsStayAtMainTest` (red-first:
  toMain=40/toNatural=0). Open: live proof on a two-base custom map -
  TauCross proves nothing (its natural detection holds, the leak never
  fires there).
- **#214** Decision reasons in CherryVis (owner: VERY HIGH priority).
  Every big decision logs its reason; where a behavior is unattributed
  today, the reason row IS the fix. (a) Siting: every construction logs
  nearTo + chosen tile + author (BO/supply/wall-in/per-base) - the Pylon
  case needs WHERE, not WHY. (b) Army: main-cluster task (posture +
  focus-ground kind: MAIN_CHOKE/NATURAL_CHOKE/MIDDLE/...) on change,
  plus retreat start/end. (c) Units: active Behavior per combat unit on
  change; workers only while fighting/fleeing. Board for time-varying
  values (task, strength), Logs for events. Volume rule: transitions,
  never per-frame rows. Motivating game: 14:25:25 Tomas Cere (TauCross,
  Defeat 95/86, 52 Dragoons lost, army never left the main -
  unattributed, no trace).
