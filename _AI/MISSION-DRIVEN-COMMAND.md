# Mission-Driven Command (MDC)

Owner-ordered architecture change (2026-10-30, priority 0): the army's
cluster stops being steered by four independent decisions and starts
being steered by one. This file is the whole change: vocabulary,
decision architecture, congruence rule, migration slices, test
strategy, the post-mortem of how command got scattered, and the
process rules that keep it from scattering again.

Status: DESIGN ONLY. No code lands from this file until each slice
gets its own commit with pins (suite green) and game proof where
named. Slices land smallest-first; behavior changes never ride
measurement slices.

## 1. The problem, stated once

Today the cluster answers "what are we doing" in four places that do
not know about each other:

| # | Decision | Owner today | Reads |
|---|----------|-------------|-------|
| 1 | Posture (DEFEND/ATTACK) + anchor | `ClusterMission`, `PostureReasons`, `ClusterAnchor` | headcount, strength, enemy race, latch |
| 2 | Tactic (FIGHT/FLEE) | `FightOrFlight` via `ClusterTactic` + latch | eval, breach, home ground, blocking army, weak/strong gates |
| 3 | Approach (COLLAPSE / BAIT / HOLD / DUEL / OPEN) | `BattleStrategies.pick` | nearest choke, contest, crush ratio |
| 4 | Unit behavior (KITE / REENGAGE / RETREAT / COLLAPSE / GATHER ...) | `BattleCommander.act` branches, `FightTarget`, `Regroup` | target present/absent, per-unit state |

Measured divergences (all from retained Wine twins, not theory):

- ATTACK posture with the anchor on the enemy base + CHOKE_HOLD at our
  own choke + FLEE tactic + retreat-floor clamp = the army sits home
  while every row claims a different intent (games 184529, 232538).
- Middle-focus ATTACK flips FIGHT/FLEE per frame while units walk
  COLLAPSE / GATHER / REENGAGE in rotation: the target flickers on the
  sight edge, each frame takes a different branch, nobody owns the
  through-line (game 233730, f12629-12670 shape).
- A Pylon warp freezes 8+ minutes while the supply goal stays
  suppressed by the phantom coming count: the construction tracker, the
  overdue path and the demote path each decline jurisdiction
  (game 184529 cid 10). Same class of bug: no one owns the outcome.

The unit of confusion is always the same: a technical fallback (floor
clamp, collapse march, ghost creep, hold point) overrides a decision
silently instead of reporting upward. The fallback is locally
reasonable; the silence is the defect.

## 2. Vocabulary (initial, owner-confirmed)

A Mission is a sentence a human commander would say, and it names the
whole cluster's job - not a unit's orders. `Behavior` stays exactly
what it is (per-unit, per-frame reason tags); Mission is the parent
they all serve. Kiting, fighting and withdrawing are children of a
mission, never peers of it.

| Mission | Human sentence | Anchor | Allowed tactics | Forbidden branches |
|---------|---------------|--------|-----------------|-------------------|
| `HOLD_MAIN` | Stay at the main, defend it | main base | FIGHT (FLEE only on breach-overrun) | any march past the main choke |
| `HOLD_MAIN_CHOKE` | Hold our main choke, do not exit | main choke standoff | FIGHT, FLEE rallies to the choke line | collapse-to-anchor march, ghost creep past the choke |
| `HOLD_NATURAL_CHOKE` | Hold our natural choke | natural choke standoff | FIGHT, FLEE rallies to the choke line | same as above, mirrored |
| `STAGE_MIDDLE` | March to the middle and wait, formed up | middle choke | FIGHT | FLEE past the middle line homeward (retreat stops AT it) |
| `HOLD_MIDDLE` | Hold the middle, do not push | middle choke standoff | FIGHT, FLEE rallies to the middle line | choke push without crush |
| `ATTACK_ENEMY_BASE` | Go kill the enemy base | enemy base / focus | FIGHT, FLEE only via retreat-ladder rungs | no-contact hold at home, ghost hunt away from the axis |

Rules of the vocabulary:

- Closed set, changed only by owner decision. A new situation gets a
  new Mission, never a new flag on an old one.
- Every early-game posture must be expressible: "stay at base" is
  `HOLD_MAIN`, never "DEFEND posture plus no target plus gather".
- Missions name ground, never units. Which units go is execution.

## 3. Decision architecture

One pure function owns the cluster's job:

```
Mission.decide(Snapshot) -> Mission
```

`Snapshot` is explicit inputs (bases, army size/composition, strength,
posture inputs, known enemy, choke geometry, clock) - the
`PostureReasons.decide(Input)` shape, which already pins this way and
is the template. No live-state reads inside `decide`, ever: snapshot
in, mission out. That is what makes the process testable: every
early-game behavior pins as input-output rows, no game needed.

Everything else derives FROM the mission, in one place, in this
order, per frame:

```
mission  -> posture + anchor          (where we belong)
         -> tactic bounds             (what we may do there)
         -> approach per unit         (how we stand in terrain)
         -> behavior per unit         (what this unit does now)
```

Each level may only narrow the one above, never contradict it. A
technical fallback (no placement, blocked road, frozen warp) does not
pick a different behavior sideways - it reports upward, and the next
decision round either re-issues within the mission or changes the
mission. That upward report is the whole difference from today.

## 4. Congruence and re-decision (owner: timing is ours)

The standing rule, enforced by twin first, by gate second:

- Every frame the twin carries MISSION plus the branch each unit
  actually took. MISSION vs actual is a congruence check, not
  decoration: a mismatch is a row, never silence.
- A mission that cannot execute MUST be downgraded explicitly. "The
  enemy sits on our choke so we cannot leave" is not a reason to sit
  under ATTACK_ENEMY_BASE forever - it is the trigger to decide
  HOLD_MAIN_CHOKE, with the reason named.
- Timing (owner-delegated): re-decision is NOT same-frame. Same-frame
  re-decides are exactly the flap we are killing (FIGHT/FLEE per
  frame, collapse/gather/reengage rotation). Decisions hold with
  hysteresis: enter threshold, looser exit threshold, minimum hold
  window on the order of the existing 120f latch. A mismatch rows
  immediately; the mission changes only through the window. Urgent
  shapes (breach in the base) keep their instant override, named as
  such - the exception proves the rule because it is listed.

## 5. Migration slices (each its own commit, each suite-green)

1. **Shadow decider.** `Mission` enum + `Mission.decide(snapshot)`
   computed alongside the live chain, twinned, never enforced. Pins:
   input-output rows for every early-game behavior the owner names
   (stay at base, hold main choke, stage middle, ...). Zero behavior
   change; proves the vocabulary covers reality.
2. **Divergence measurement.** One Wine game read for MISSION vs
   actual-branch mismatches. No code: the twin does the work, the
   datum names the worst offender. Enforcement order follows the
   measurement, not guesses.
3. **One mission enforced at a time.** First candidate: HOLD_MAIN_CHOKE
   (forbid the out-march, downgrade blocked attacks into it). Sim pin
   + suite + Wine proof per mission. The old posture/tactic/strategy
   readers it replaces move to `src-old/` one file per commit with the
   game proof attached, per the WO-144/147 deletion discipline.
4. **Fallbacks report upward.** Retreat floor, collapse march, ghost
   creep, no-contact hold: each gets a "cannot execute, requesting
   re-decide" return instead of a silent sideways move. Pinned per
   fallback, enforced after its mission lands.
5. **Delete the shadowed deciders.** `ClusterMission` posture,
   `FightOrFlight` latch, `BattleStrategies.pick` as independent
   readers: deleted or demoted to derived helpers once no enforced
   mission reads them directly. Acceptance tier re-run per deletion.

Explicitly out of scope: per-unit `Behavior` (kept - it is the right
level for unit reasons), the engine/sim work, any threshold the owner
has not set (zealot chase distance, re-decide window length - proposed
values ride the slice, owner confirms).

## 6. Test strategy

- `Mission.decide` pins: table tests, input snapshot in, mission out.
  Every early-game behavior from §2, both sides of every boundary
  (the 10/12 leash pattern: enter, hold, exit asserted separately).
- Congruence tests: fixed mission + stub world, assert the unit
  branches stay inside the mission's allowed set; a blocked mission
  asserts the downgrade row, not the silent sit.
- No full-game gates, ever (repo rule). Wine adjudicates enforcement
  slices; sims adjudicate geometry (choke holds, rally lines).
- Mutation standard as usual: flip a gate, watch its pin fail.

## 7. Post-mortem: how command got scattered

This section exists so we stop doing it. Each entry is a real step we
took, each locally reasonable, each missing the same guardrail: **no
new branch named its parent decision, and none reported upward.**

1. **Behavior vocabulary without ownership** (`Behavior` enum). Naming
   the tags was right; letting every call site pick them freely made
   "what the army does" the union of dozens of independent picks.
   Missing guardrail: a parent decision owning the allowed set.
2. **Per-unit retreat under FLEE** (`RetreatFromLostFight.decide`).
   Verdicts per unit (RALLY / STEP_CUTOFF / CORNERED / NOWHERE) are
   good engineering - but they run under a tactic the unit never sees,
   and STAY verdicts fall through to fighting without telling the
   tactic layer. The flap source.
3. **Gate accretion in `ClusterTactic`** (breach, home ground, blocking
   army, weak-vs-Zerg, strong-army). Each gate is a correct owner
   order. But five early-returns plus a latch plus an eval fallback is
   not a decision process - it is a pile of vetoes with no stated
   priority and no twin of which one fired until ClusterTwin caught up
   years later. Missing guardrail: ordered, twinned, table-tested
   priority - the shape §3 mandates.
4. **`BattleStrategies.pick` growth** (COLLAPSE, CHOKE_HOLD, ...).
   Approach was added as a peer of tactic instead of a child of
   mission, so "hold the choke" can now veto "attack the base" with
   neither knowing. The home-choke breakout gate (150% + <2 bases) is
   the fourth veto on the pile - correct, and evidence for this file.
5. **Regroup fallbacks as silent owners** (no-contact hold, collapse-
   to-anchor, ghost creep, hunt). Each owns the unit "when nothing
   else does" - which, with target flicker on the sight edge, is every
   other frame. Missing guardrail: fallbacks report up (§4).
6. **Rally scatter keyed off the global unit id.** Not a decision bug,
   but the same disease one level down: implicit global input instead
   of an explicit one. Fixed with quadrant fan-out; the lesson stands:
   implicit inputs are untestable decisions.
7. **Frozen-warp silence.** Three systems (tracker, overdue, demote)
   each declined jurisdiction over a started-but-stalled construction
   for 8+ minutes. Every legibility twin existed except the one that
   would have forced a decision. Missing guardrail: stalled states
   re-decide by rule, not by whichever watcher notices first.

The pattern across all seven: **we kept adding readers of live state
instead of writers of a shared decision.** Every fix worked locally
and scattered globally. The process rules below exist to make that
trade visible before it lands.

## 8. Process rules (proposed additions to CONVENTIONS)

Numbered to slot after the existing decision rules; adopt by owner
confirmation, not by this file alone.

- **P1. One parent per branch.** A new behavior branch names the
  Mission (later: the decision) it serves, in its doc comment and in
  its seam test. A branch with no parent does not land.
- **P2. Snapshot in, orders out.** Decision code reads an explicit
  snapshot struct, never live Select/Count/Alpha. Execution code
  issues orders, never decides. A diff that does both is split.
- **P3. Fallbacks report upward.** Any "when nothing else owns the
  unit" path returns a named verdict the decider sees; a fallback
  that acts sideways (different tactic's move under this tactic's
  name) is a bug, with a pin proving the report.
- **P4. Twin before gate.** New telemetry rows land and get measured
  in at least one Wine game before any enforcement reads them.
  Enforcement without measurement history states whose game proved
  the shape.
- **P5. Hysteresis on every boundary.** Any threshold that can flap
  per frame ships with enter/exit bands and a hold window, pinned
  both ways (the 10/12 leash is the house example).
- **P6. Divergence is a row.** If the twin can show MISSION vs actual,
  any other "the army ignored its orders" claim needs the same twin
  before a fix. No forensics from narration.
- **P7. One mission per commit.** Mission vocabulary changes, one at
  a time, each with its decide-pins and its congruence evidence.
  Deletions of shadowed deciders ride their own commits with game
  proof attached (WO-144/147 discipline).
- **P8. Numbers are leaves, words are nodes.** Tuners and learners may
  change leaf values (Proximity tiles, latch windows, margins); they
  never rename nodes or rewire decisions. `decide()` stays pure over
  explicit snapshots - that shape is both the test harness and the
  learned-policy interface. Pins assert behavior categories, never
  exact tuned values, so they double as regression gates for learned
  parameters.

## 9. Open questions (owner)

- Zealot chase veto: RESOLVED 2026-10-30 in words, not numbers - chase
  only within CloseToBuilding of our buildings, inside the base zone
  (15), toe-to-toe, ongoing, or falling back onto a base-ward foe;
  veto forgets the contact (`ChaseVeto` + `forgetContact` + CHASE_VETO
  twin). Small-army generalization still open.
- Re-decide window length: proposed on the order of the 120f latch,
  per mission. Confirm or set.
- Vocabulary changes: add/remove/rename missions only here, never in
  code first. Current proposal is §2 verbatim.
- Whether `Missions.followPosture` becomes the mission executor or is
  replaced: recommendation is replace-after-shadow (slice 5), confirm.
