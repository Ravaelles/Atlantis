# Combat task file (single source of truth for combat work)

NEXT.md carries exactly one delegation line here and nothing else.
Status per workstream; proof queue at the bottom names the vehicle,
never the rows. Per-game evidence lives in
`_AI/WINE-MACRO-DATUMS.md`, not here. Open decisions name their
owner; blocked stays blocked until the owner decides.

## Target architecture (owner direction 2026-11-09, research only)

Layers talk only through high-level decisions (snapshot in, orders
out; fallbacks report upward, never sideways; twin before gate;
hysteresis on every boundary). No implementation until the owner
calls it - this section is the plan the slices below execute.

- **L0 Strategy (game level, exists, untouched).**
  `information.strategy.*`: which build/tech, when to expand. Down
  only fact flags (isGoingTech, rush/cheese, game phase). The word
  stays here - L1 is not called Strategy (collision, researched
  2026-11-09).
- **L1 Mission (cluster job).** `ArmyMission`: HOLD_MAIN /
  HOLD_MAIN_CHOKE / HOLD_NATURAL_CHOKE / HOLD_MIDDLE / STAGE_MIDDLE /
  ATTACK_ENEMY_BASE. Pure `decide(Snapshot) -> Mission`; no live
  reads inside. STAGE/HOLD_MIDDLE/ATTACK are shadow today - enforcing
  them is the work, not renaming them.
- **L2 Orders (tactical cluster level, derived, no new reads).**
  `forMission(Mission, Geometry) -> {anchor, stance, approach}`:
  anchor (where we stand), stance (HOLD / ENGAGE / WITHDRAW -
  posture+tactic folded in; the posture->stance rename lands with
  the derivation, not before), approach (CHOKE_HOLD / PLUG /
  OPEN_FIELD / COLLAPSE / BAIT from mission+geometry).
- **L3 Behavior (per-unit execution, exists, kept).** The `Behavior`
  enum; dragoon/zealot/corsair splits stay here (Corsair precedent:
  one brain, solo+squad entries). Every branch names its parent
  mission.
- Cross-cutting: twins (read-only) + latches (hysteresis).
- **Task (defined 2026-11-09, twinned live):** `ClusterTask` +
  `TaskAnchor`: the mission instantiated - anchor kind (never a
  tile; the rally code resolves points), posture, whether FLEE is
  allowed and which line it rallies to (null: the retreat ladder
  owns routing). Pure `forMission` table, pinned 6/6
  (`ClusterTaskTest`); `TaskTwin` rows the enforced mission's task
  every frame (transition + 300f heartbeat, pinned in
  `TaskTwinTest`) - twin before gate, so enforcement reads nothing
  yet. HYDRATED 2026-11-09: `MissionHold.hold` reads the task anchor
  instead of the mission switch (same standoffs on every reachable
  path; `HoldMainChokeEnforcementTest` + task pins cover both
  halves, no behavior change). `RetreatFloor` twins the task rally
  next to its rung (`taskRally`, pinned pure) - the divergence data
  for the next slice, which makes the rally read `fleeRally`.
  PROOF ATTEMPT 2026-11-09 (game10, 900 s TauCross PvZ, 14400 f):
  no HOLD_NATURAL_CHOKE mission all game (never expanded), so the
  changed path never fired live - zero live risk from the change,
  but the natural-reform shape stays unproven. Verified instead:
  TaskTwin transitions live + stable (8, no flap), RETREAT_FLOOR
  rows carry taskRally, floors clamp MIDDLE rungs under HOLD_MAIN
  / ATTACK with matching "-" rally, 15397 twin rows, no crash
  (timeout kill as designed). STILL OPEN: one expanding game where
  a lost natural fight re-forms on the natural line.

## Deletion queue (verified unused, one at a time)

Rule: zero live readers (grep src+tests, comments do not count),
one coherent deletion per commit, fast gate green each. Live
packages that only *look* dead stay: `battle/running/*` feeds
`AUnit.runningManager` + `MoveAway`; `battle/avoid/*` feeds
workers/builders/scouts/observers/repairers (Terran leaves stay -
the dispatch chain reads them; dead-race cleanup is its own slice).

- [x] DONE 2026-11-09: `combat/retreating/protoss/big_battle/`
  pair (only each other + one commented ref), `battle/running/
  fallback/RunAttackFallback.java` (`applies()=false` stub, zero
  refs), `battle/retreating/RetreatManager.java` (counter never
  written; `ArmyStrength.muchStrongerPercent` folded to the
  constant 140 it always returned). Evidence: fast gate per commit.
- [x] DONE 2026-11-09: legacy `Missions.forceGlobal*` writers
  `OnOurUnitDestroyed` (ranged mass), `ProtossStrategyResponse`
  (rush defence) + `RaceStrategyResponse` (expansion punish)
  ported into `PostureReasons` as named reasons
  (RANGED_MASS_SO_COME_HOME, RUSH_DEFENCE,
  ENEMY_EXPANDING_SO_ENGAGE), pins red-first, writers deleted
  with their dead helpers. Evidence: fast gate per commit.
- [x] DONE 2026-11-09: the dead rush-defence response chain:
  `ProtossStrategyResponse.rushDefence` override (return ignored),
  `shouldSkipAntiRushCombatBuilding`,
  `rushDefenseCombatBuildingsNeeded` x2, base `rushDefence` stub
  + its call (all zero callers; the DEFEND half already lives as
  RUSH_DEFENCE). Evidence: fast gate green.
- [x] DONE 2026-11-09: decided KEEP (verified, owner-confirmed):
  `AGame`-UMS force-ATTACK (mode setter is the right home for a
  mode default; overwritten next frame except empty-Alpha readers -
  porting would change UMS worker-avoid with no vehicle);
  `DefendedPlace` (single-reader pure function with a pin is
  healthy; the second-reader requirement is withdrawn);
  Terran avoid subtree (leaves self-gate on race; deleting
  contradicts open-for-Terran, CONVENTIONS §5).
- [x] DONE 2026-11-09: `FormationOrderLatch` reads the decided
  posture (noted once per frame by the commander loop) instead of
  re-deriving headcount posture per call; un-noted paths keep the
  old derivation. Pin red-first both directions (0/2 ignored,
  2/2 honored); `wasAttacking` now reads the latch's previous
  decision instead of the global mirror (provably identical -
  both track the previous decided posture; UMS frame-1 edge
  documented in the commit). Fast gate green, sim suite untouched
  by shape.
- [x] DONE 2026-11-09: `ChaseVeto` reads the decided posture
  (same note-per-frame shape as the latch; fallback untouched).
  Pin red-first both directions; existing `ChaseVetoTest` green
  unchanged (it drives the mirror, which the fallback still
  honors).
- [x] DONE 2026-11-09: one shared `DecidedPosture` frame note
  instead of three parallel ones (`FormationOrderLatch`,
  `ChaseVeto`, `Regroup.isAttackMarch` all read it; each keeps
  its own fallback - headcount posture, global mirror). No
  behavior change beyond the already-landed noted paths; pins
  re-verified after the merge (27/27 with the latch, veto,
  reinforce and posture suites).
- [x] DONE 2026-11-09: dead breach-attack counter
  (`EnemyUnitBreachedBase` incremented on the global mirror, zero
  readers of the count). Breach detection itself untouched.
- [ ] `battle/cluster/Mission.java` dormant wrapper + `Missions`
  mirror deletion (slice 5 core): after readers migrate (done:
  posture inputs, latch/veto/regroup note via shared
  DecidedPosture, painter global line + dead focus painting,
  dead breach-attack counter, dead rush-defence chain;
  KEPT: UMS force-ATTACK (mode setter is the right home -
  overwritten next frame except empty-Alpha worker-avoid readers,
  no UMS vehicle to prove a port);
  DONE 2026-11-09: painters' mirror reads (`Alpha.mission()`
  line deleted as a constant duplicate of the enforced line,
  focus text folded to its constant "NONE", squad abbrev to its
  constant "Def" - display identical, no game behavior);
  left: `Missions.reset/history`
  bookkeeping + `OnGameStarted` reset call,
  `GoalBoardAdvisor.sparta()` (reads hardcoded-false - port with
  the board, production workstream),
  `BuildOrderRowParser.fromString` + `Requirements` mission branch
  (BO MISSION= rows parse and gate on supply but nothing executes
  them - deleting the parse would `AGame.exit()` on real BO files
  that carry the directive; belongs to the production queue
  workstream, not here),
  `AUnit.isMissionAttackOrGlobalAttack` (unit OR global - migrate
  both halves together with the per-unit system)).
  Then delete both files at once, suite green as the evidence.
- [x] DONE 2026-11-09: dead per-unit mission code:
  `FixActions` idle helper + `ProtossContinueAttack` (never
  instantiated) with its `PreventAttacksInMissionDefend` gate,
  plus the two always-false `isActiveManager` branches in
  `DragoonDontAvoid` (deleted, not ported - nothing could ever
  take them). Evidence: fast gate green.
- [x] DONE 2026-11-09: live per-unit avoid-family fold
  (always-DEFEND proof: `setMission` zero callers, every live
  read sees DEFEND): `ZealotAlwaysAvoidEnemy` (3 always-true/
  always-false gates), `ZealotDontAvoidEnemy` (sparta branch +
  always-DEFEND gate deleted; `dontAvoidWhenCloseEnemyWorkers`
  switched to the frame's `DecidedPosture` with mirror fallback,
  pinned red-first both directions by
  `ZealotDontAvoidFollowsDecidedPostureTest` on the mirror-driven
  ChaseVeto/Latch template), `DragoonDontAvoid.whenMissionDefend`
  gate, `ProtossSafetyMargin` middle conjunct,
  `ShouldRunTowardsBase` dead `isMissionAttack` branch +
  always-true `isMissionDefend` disjunct, `EvalChokeTweaks`
  always-true `missionDefendOrSparta`, `FollowAlphaLeader`
  always-null `focusPoint` (setter has zero callers - both dead
  branches deleted, unused import + dead local removed).
  Evidence: fast gate green (1255/0).
- [x] DONE 2026-11-09: per-unit avoid/run remainder folds
  (same always-DEFEND/always-false proof, no behavior change):
  `DragoonAlwaysAvoidEnemy` dead VeryWounded_vZ branch
  (`isMissionAttack` conjunct), `AvoidReavers`
  `missionAttackAndTooWeak` helper (always false - eval<=3
  stands alone), `ARunningManager` always-true
  `!isMissionSparta` conjunct. Evidence: fast gate green.
- [ ] Per-unit missions remainder (`AUnit.mission()` /
  `Squad.mission()`): `AUnit.isMissionAttackOrGlobalAttack`
  (unit OR global - migrate both halves together with the global
  mirror), orphaned readers (ProduceDragoon/
  ProduceTemplarArchives die with the orphan chain, production
  workstream).

## Mission command (#232, priority 0)

Full spec: `_AI/MISSION-DRIVEN-COMMAND.md`. Landed: vocabulary +
pure `decide` + shadow twin (1a/1b), shadow sampling (2), choke
enforcement gates (3a/3b/3c), fallbacks report upward (4),
mission-transition gate (5 s hold-fire after withdraw, 2 s
re-withdraw spacing; pinned red-first). Enforcement reads the
gated mission. Task layer landed shadow-side: `ClusterTask` +
`TaskAnchor` pure table (6/6), `TaskTwin` live rows, `MissionHold`
reads the task anchor, `RetreatFloor` twins the task rally.
Open: live enforcement proof per slice (halted);
the #204 retreat-ladder proof below; Thundering/UMS vehicles are
owner-called.

### Rally reads task.fleeRally (APPROVED, LANDED sim-side
2026-11-09; live proof WAITS for a natural game - owner 2026-11-09:
no dedicated hunting, the shape will be caught when it occurs)
Vehicle: mocked-choke stub world (RetreatFloorTest precedent -
deterministic distances, not flat stubs: real FakeChoke geometry
at (60,20)/(44,40), two Nexus fakes, mission forced via
sampleShadow). Failing shape first: 2-base Protoss line holding
the natural choke (HOLD_NATURAL_CHOKE, FLEE) rallied home toward
the main ([21,18]) instead of stopping AT the natural line.
Fix LANDED 2026-11-09: the retreat home reads the task anchor -
NATURAL_CHOKE tasks rally at the natural standoff; main-choke
holds already land there via the floor, other missions keep
today's rally. Pinned (`RetreatToNaturalChokeTest` red pre-fix
destination, green post-fix) + related suites green (42/42).
Live proof: one expanding game where a lost natural fight
re-forms on the natural line (TASK + RETREAT_FLOOR taskRally rows
decide; game10 ran without the shape - never expanded).
Out of scope: flee permission itself (FLEE vs STAND stays with
ClusterTactic + breach), approach narrowing, forbidden branches.

### Approach narrowing per mission (APPROVED, slice 1 LANDED
2026-11-09 - vocabulary + snapshot boundary, shadow: nothing reads
it yet)
`BattleStrategies.pick` reads live state independently today
(unit, choke proximity, crush). Target: `Approach.forTask(task,
geometry) -> BattleStrategy`, pure over an explicit Geometry
snapshot (choke-near-unit, crush ratio, contact state).
LANDED 2026-11-09: `combat/cluster/Approach` (next to
`ClusterTask`) + `ApproachTest` (6 pins): per-mission defaults
from the MDC §2 table (HOLD_* -> CHOKE_HOLD, STAGE_MIDDLE +
ATTACK -> OPEN_FIELD, null holds like main), `Geometry.capture`
isolating every live read the current pick does (defensive,
null-safe; pure `crushing()` reuses the documented
`mayAdvance` 2.0 ratio, `isDuel()` the same baseless+headcount
rule the live check uses). Shadow only - the narrowing slices
widen `forTask(task, geometry)` one geometry shape at a time
(contact shapes first: trapped-duel and bait keep their pins),
commander port last. Evidence: fast gate green.
Starting hypothesis from the MDC §2 table (unchanged):
HOLD_MAIN / HOLD_MAIN_CHOKE / HOLD_NATURAL_CHOKE -> CHOKE_HOLD,
STAGE_MIDDLE -> OPEN_FIELD (march formed),
HOLD_MIDDLE -> CHOKE_HOLD,
ATTACK_ENEMY_BASE -> OPEN_FIELD default, COLLAPSE/CHOKE_HOLD/
BAIT/DUEL narrowed by geometry slice-by-slice (contact shapes
first: trapped-duel and bait keep their pins).
Out of scope for slice 1: plug-vs-standoff (formation detail,
stays where it is); forbidden branches ride the mission that
owns them.

## Retreat doctrine (#204 landed sim-side, proof pending)

- **Retreat ladder.** Floor, rungs and choke standoff depth pinned
  (`RetreatLadderTest`, `RetreatFloorTest`, mutation-proven).
  Open: one losing-fight live game where the line holds the middle
  instead of running home.
- **No-flee holds (owner 2026-11-08).** Sim-pinned decision gates:
  past two bases the cluster holds when nearer the main than the
  natural (`multiBaseHoldAgainstRetreat`); 15+ combat units within
  20 ground tiles of the natural at 90%+ strength stand and fight
  (`naturalStandWithStrength`, outranks the weak-early-Zerg veto).
  Open: live proof - a game where the line holds the natural instead
  of running home.
- **ClusterFlee.** Sim pins cover homeward rally, organized retreat,
  fighting through a blocking army; 93 retained games show no
  blocking instance - unstick stays parked. Open: live
  blocking-geometry proof + cohesion through a lost fight.

## Choke doctrine + maneuver (all sim-pinned; live proof open)

- **#170 choke doctrine + SquadScout (high priority).** Do not push
  a narrow choke without superiority; defend from a standoff.
  Open: live no-contact/choke proof, then bait/collapse.
- **#171 high-ground hold.** High side preferred; correctly silent
  otherwise. Open: live superiority proof.
- **#172 region-aware escape.** Walkable candidates + corner/
  obstacle/enemy scoring. Open: live superiority proof.
- **#173 escape doctrine (long-vector-first).** Deflects at full
  range on map corners; angle-stepping parked. Open: live
  walled-pocket engagement.
- **#210 formation orders.** Latch landed (re-issues at most every
  10 frames; denied holders hold). Open: a calm-defense game.

## Targeting and kiting (sim-pinned; live proof open)

- **#152 Dragoon kiting.** Latency ladder in place; 0-2-hit goal
  via #186, wall-aware steps after production, macro proof with
  attributable foes, K-of-N tuner efficacy all open.
- **#186 `ProtossAttackHoldToShoot`.** Stop-point model (braking,
  windup, latency, chaser commitment); wipes pinned, 0-2 hits
  pinned vs chase-nearest. B-slices landed (outranged-shelling
  withdraw + fresh-seat darkness guard, both measured red-first).
  Open: harder Stage B shape + live calibration (the threat pick
  wants it first).
- **#179 ZealDrag coordination/hold-fire.** Sim-pinned; live
  losses showed mobile units, not freezing. Open: winnability vs
  real bots (0/9 FFA wins).
- **Early harass override (owner 2026-11-07).** A healthy first
  Dragoon meets 1-2 lone Zerglings instead of holding formation or
  standing off (`EarlyHarass.shouldHarass` vetoes FormHold calm slots
  with a HARASS verdict and MissionHold standoffs; kite still
  outranks, packs/zealots/breach keep their pins). Pinned red-first
  (`EarlyHarassTest` 3/3); combat 759/759, fast 1137/0. Open: live
  early-Zerg pressure game.
- **#181 split-cluster reinforce.** Sim-pinned (fresh-contact
  reinforce, ghost-contact rejection, ongoing-fight priority;
  2026-11-07 extension: ATTACK-march reinforce off-map, UMS gate
  stays for defending armies). Open: live recurrence of the
  park-while-fighting shape.
- **Cohesion leash (owner 2026-11-07).** A melee unit that acquires a
  target while detached from the ball (>6 tiles from the median)
  rejoins instead of engaging (`ClusterCohesion.recallStraggler` in
  the commander's fight branch + `COHESION_RECALL` twin; median is
  component-wise, scouts march out untouched, chewing/kiting exempt).
  Ranged units keep their kite/escape doctrine (pocket-escape and
  base-raid pins forbid a leash over them - separate slice with its
  own vehicle). Pinned red-first (`StragglerRecallTest` 3/3);
  combat 756/756, fast 1134/0. Open: live lone-zealot game.
- **#182 FormationHold veto + unstick.** Sim-pinned; no 200f stand
  recurs. Open: a stand-and-trade that is NOT the pocket geometry
  (unobserved so far).

## Focus and posture

- **#194 `DefendedPlace`.** Resolver + focus in
  `atlantis.combat.focus` (MIDDLE priority, >10-Dragoon ban,
  army-mission twin). Open: formation adaptation - no formation
  code reads the kind yet; which kinds change slots is a doctrine
  decision, not an implementation gap.
- **#196 worker 10-tile leash.** Pinned
  (`WorkerFarFightTest`); StopFighting now also reads the
  target- no new fights past 10, ongoing fights recall past 12.
  Open: a pressure game where raiders reach the mineral line.
- **#197 trapped-Dragoon posture.** Sim-calibrated and latched.
  Open: live proof of an army attacking out of a trapped defense.
- **#198 specialist behaviors.** Landed sim-pinned (scarab,
  Corsair, attacked-DT retreat, High Templar selection). Open:
  a game that techs there AND fights there.

## Live-shape backlog (one quiet game each; never alongside the suite)

- **#200/#205 hold-at-main (+ first Dragoon).** Fewer than five
  combat units hold at the main (a dragoon build holds regardless
  until its first Dragoon finishes; both pinned). Needs an
  early-Zerg pressure game; named vehicles exist (122342, 115321,
  212230, 184529, 003802).
- **#207 army forms toward the wrong base.** Unobserved (games
  form at the known enemy base or hold home). Needs a
  both-bases-known game; then attribute: stale base memory or
  anchor choice.
- **#208 formation faces our own base.** Sim proof landed
  (outward-facing pivots). Open: trace geometry from a
  home-defense game (twin rows cannot show bearing).
- **#220 runby screening.** Screeners collapse to the Nexus and
  the line pays; eval conservatism at 2v4 suspected. Needs the
  same pressure vehicle as #200.
- **#223 hidden-unit crisis response.** V2 goal chain landed +
  pinned; lurker #234 breached with only partial answer (Forge,
  no Observer/cannons); trace gap: contact-only lurkers never
  enter `EnemyUnits.discovered()`. Needs: a gas-rich lurker game,
  the observer goal + cannon dispatch diagnosis, and a discovery
  twin.
- **#201 Corsairs trail the army.** Solo-by-default + merge rule
  landed and verified live (zero deaths, kiting instead of
  standing). Open: the full chain - a game where Corsairs hunt
  AND kill with the trace to prove it.

## Parked (not loop work; revisit on trigger, not on schedule)

- Angle-stepping (#173), no-contact-hold ownership, collapse-latch
  follow-up measurement: parked, shapes unobserved.
- Learner sequencing (#185, sequenced after the production cut):
  synthetic scenarios + varied enemy policies only; test-side
  export green; waits on Stage A fidelity (owner direction on what
  it measures).
