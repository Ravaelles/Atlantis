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
  yet. Next: wire one mission's task at a time (same discipline
  as the HOLD_* slices); approach narrowing + forbidden branches
  ride those slices, not this vocabulary.

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
  `OnOurUnitDestroyed` (ranged mass) + `ProtossStrategyResponse`
  (rush defence) ported into `PostureReasons` as named reasons,
  pins red-first, writers deleted with their dead helpers.
  Evidence: fast gate per commit.
- [ ] `RaceStrategyResponse` expansion trigger (ATTACK on expanding
  enemy - needs a new enemy-expansion input; next port slice).
- [ ] `AGame`-UMS force-ATTACK (UMS bypasses posture; needs a UMS
  vehicle - owner call).
- [ ] `FormationOrderLatch` stays shared: 3 call sites with
  per-caller denial handling, one twin, keyed on the order itself.
  Follow-up (not this slice): thread the decided posture in
  instead of re-deriving it via `ClusterMission.postureFor` (P2).
- [ ] `battle/cluster/Mission.java` dormant wrapper (lives as the
  `Missions` value type) - dies with the Missions mirror, slice 5.
- [ ] `DefendedPlace` second reader or fold (needs the owner
  doctrine: which kinds change formation slots) - blocked on that
  decision.
- [ ] Terran avoid subtree (dead-race cleanup needs the dispatch seam
  decision first).

## Mission command (#232, priority 0)

Full spec: `_AI/MISSION-DRIVEN-COMMAND.md`. Landed: vocabulary +
pure `decide` + shadow twin (1a/1b), shadow sampling (2), choke
enforcement gates (3a/3b/3c), fallbacks report upward (4),
mission-transition gate (5 s hold-fire after withdraw, 2 s
re-withdraw spacing; pinned red-first). Enforcement reads the
gated mission. Open: live enforcement proof per slice (halted);
the #204 retreat-ladder proof below; Thundering/UMS vehicles are
owner-called.

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
- **#206 army hides in the main.** Unobserved as a latch (home
  deaths were pressure, home returns were ATTACK flips). Needs a
  game where contact ends and the line must re-deploy; read the
  anchor/hold/posture rows for the latch, then one rule with a
  pin. Do NOT add a fourth home-pointer before that reading.

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
