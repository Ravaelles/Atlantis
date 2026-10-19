# Combat micro: kiting, attack frames, unsticking (Dragoon case study)

Owner-verified on Wine 2026-10-19 (`ums/rav/protoss/1Drag_v_1Zeal.scm`):
a Dragoon with DoNothing active stood still under a Zealot and died.
Root causes, in order found: (1) FLEE with null defend anchor issued
no order at all (baseless map); (2) no kite behavior existed;
(3) issuing move during the attack animation cancels the shot;
(4) straight-back steps die on walls/glue (dist ~0 divides by zero).

## PurpleWave reference (primary source for micro truth)

Read these before touching kiting, in this order:

- `src/Micro/Agency/Commander.scala:121-145` (`attack`) - the single
  decision: `useAttackCommand = readyForAttackOrder || inRangeToAttack`
  (reposition between shots is allowed, even desired); a new attack
  order issues ONLY on a new target or when overdue
  (`cooldown==0` and no attack started for 2x cooldown), else `sleep`
  (do nothing - let the standing order ride). Never re-issues.
- `src/Micro/Agency/AttackDelay.scala` - frame data per unit type
  (DragoonGndAttkInit sequence quoted verbatim): a move issued in the
  window after the attack starts cancels the damage AND can leave the
  unit stuck. Withholding orders there is load-bearing, not polish.
- `src/Micro/Agency/Commander.scala:252-280` (`move`) - move-then-sleep
  pattern, `tryingToMove` tracking, push-through-traffic on the way.
- `src/Micro/Actions/Protoss/BeDragoonVsSlowlot.scala` - the Dragoon
  shape: margin math, `hardAvoidThreatRange` while outranged,
  `retreat()` via threat-aware waypoints when the margin goes negative.
- `src/Micro/Coordination/Pathing/MicroPathing.scala`
  (`getThreatAwarePath`, `getWaypointInDirection`) - pathing that routes
  around threats instead of backing straight into walls.
- `src/Micro/Targeting/FilterRequired/TargetFilter...`, `Commander:135`
  - repositioning (move) vs attacking (attackMove/attack) split.

What we ported (Atlantis `atlantis/battle/BattleCommander.java`,
`atlantis/units/actions/move/MoveAway.java`): kite while cooling
(`shouldKite`: enemy < 3.5 tiles + cooldown >= 10, Dragoon-only),
fall through to fight when FLEE has no anchor, never interrupt
`isAttackFrame`/`isStartingAttack`, compass fallback (8 directions,
never toward the threat) when the straight step fails, attack as the
last resort instead of standing still. What we did NOT port (open):
threat-aware pathfinding (we step blindly), sleep/no-reissue
discipline (we issue every frame), stuck detection + unsticking
(`tryingToMove` + timeouts), per-unit-type attack-frame tables
(we use the generic cooldown threshold).

## Twins that name it

- `KITE` (`stepped-aside` throttled 1/30f per unit, `stuck-no-step`
  always): stepped vs stuck ratio decides cornered-vs-open at a glance.
- Still missing (NEXT #152): HIT twin (HP drop + range + cooldown +
  moving at the moment of damage - answers "why did this hit land"),
  SHOT twin (attack started - answers "did we stop shooting", i.e.
  the animation-guard latch question), goal-source twin for V2 Pylon
  pairs (which demand re-fired: BO row vs supply goal).

## Numbers from the proof games (all Wine, same map)

- Pre-fix: 0/1 loss, Dragoon motionless (FLEE + null anchor).
- Post kite+fallthrough, pre animation-guard: 0/1 loss, twin showed
  9 stepped-aside vs 106 stuck (arena walls; dist-0 glue sometimes
  escapable) - the fall-back attack trades while cornered.
- Post compass + animation guard: 1/0 WIN, 14 stepped vs 3 stuck,
  20+ hits landed. Kiting works; corners still cost steps.

## Open traps

- FLEE on a baseless map has no anchor: any future tactic must fall
  through to fight when its move target is null (same DoNothing
  class of bug, one `if` away).
- `MoveAway` callers assume a step happens; with the compass a
  returned `false` now truly means "no walkable direction" - treat
  it as fight-in-place, never as wait.
- Macro-game kiting is UNPROVEN (OpenBW spawns no enemies; Wine
  macro games are owner-run). The 1v1 proof does not cover kiting
  inside a 20-unit brawl (focus fire, traffic, retreat latch).

## Stardust reference (Dragoon micro bible, read 2026-10-20)

Why: owner asked how Stardust and PurpleWave manage cooldowns instead
of hardcoding "dist > 2.7 and cd <= 7 → attack". Answer: neither uses
a flat distance gate. Both compute a CONTINUOUS desired position from
(cooldown, distance, ranges, speeds) every frame, and the attack
decision is latency-relative, not second-based. This file is the
durable record; the in-code port is `BattleCommander.desiredKiteDistance`.

Primary source: `/sc-ai/Stardust/src/Units/MyUnit/MyDragoon.cpp`
(307 lines, whole file relevant). Secondary: `MyUnit.cpp`
(`MyUnitImpl::attackUnit`, `isReady`), PurpleWave
`src/Micro/Agency/Commander.scala` (`attack`: new/overdue-or-sleep),
`src/Micro/Agency/AttackDelay.scala` (DragoonGndAttkInit iscript).

### Cooldown management (Stardust: never act on a shot in flight)

- `DRAGOON_ATTACK_FRAMES = 6`: after an attack starts, the unit is
  NOT ready for new orders for 6 frames + remaining latency
  (`isReady()` false). Our equivalent: the animation guard
  (`isAttackFrame`/`isStartingAttack`), same window class.
- `nextAttackPredictedAt`: if the standing attack order is expected to
  fire within remaining latency, leave the unit alone - even target
  switches are refused unless the old target is gone/invalid/too far
  at the predicted frame. Our equivalent: new/overdue-target
  discipline + ride-after-reposition. Theirs is predictive (frames),
  ours is reactive (order state).
- One order per frame max (`issuedOrderThisFrame` + DUPLICATE log);
  move orders deduped (same target within 3px, not within
  latency+3 frames, never re-issue while already moving).
  Ours: re-issue at most every 10 frames (KITE_RESTEPS_EVERY_FRAMES).
- Unstick: moving + no progress past
  `potentiallyStuckSince + latency + 10` → STOP + hold. Ours: 48
  motionless frames → STOP reset. Same shape, ours slower.

### Attack decision (when to shoot vs reposition)

- Stardust Dragoon: attack iff `cooldown <= latency + 2` (≈ 4-5f,
  not 7). Otherwise reposition. Melee exception: none needed - the
  positioning rule below already keeps the unit close vs short range.
- Stardust base (`MyUnitImpl::attackUnit`): dist > 320px (10 tiles)
  or target invisible → plain move toward target (approach, NO attack
  order - the order itself would make the engine chase on arrival;
  ours does the same via the glide: move to firing point, attack
  issues on arrival). Predicted in-range (EDGE-to-edge at the
  latency horizon) or target stationary → attack, with a force-resend
  when coming into range. Else intercept course.
- PurpleWave: `useAttackCommand = readyForAttackOrder ||
  inRangeToAttack || framesToGetInRange <= latency + max(cooldown,
  agencyAverage)`; the order issues ONLY on new target or when
  overdue (`cooldown==0` and no attack started for 2x cooldown),
  else `sleep`. Ours matches this exactly (new/overdue discipline +
  ride), including the arrival-time-vs-cooldown sync.

### The desired-distance formula (the answer to hardcoded 2.7/7)

Stardust `MyDragoon::attackUnit`, per frame, all in pixels
(D = Dragoon range 128, T = target range, C = cooldown,
L = latency frames, S = top speed 5.0 px/f):

- Target fleeing (`current < predicted` dist): desired = min(D - 48,
  T + 16) - do NOT kite out of range, keep pressure.
- Ranging a bunker: specialised stop/go (skip - not our matchup).
- We outrange (`D >= T`): `desired = min(D, D + (Cd - (predicted - D))/2)`
  where `Cd = (C - L - 2) * S`. I.e. max range, reduced by half the
  gap between "how far the target still is outside range" and "how
  far we walk before the weapon is ready". If `T <= D - 64`
  (melee-ish, Zealot qualifies: 15 <= 64): `desired -= 32`.
- Else (outranged): desired = D.

Worked 1v1 (Dragoon vs Zealot, tiles; ours are center-based, Stardust
edge-based - offset ~0.5 tile, the tuner absorbs it):
dist 3.0, cd 5, healthy: Cd = max(0,(5-4-2))*0.156 = 0 →
desired = min(4, 4 + (0 - (3-4))/2) = 4 → melee -1 → **3.0 = hold,
no step**. This IS the owner's "don't retreat at 2.7+/cd<=7",
derived, not hardcoded.
dist 3.5, cd 15, hurt (required margin 3.69): fixed 2.0 step would
land at ~5.5, OUT of range. Formula: Cd = (15-6)*0.156 = 1.4 →
desired = min(4, 4 + (1.4 + 0.5)/2) = 4 → -1 = **3.0: hold and
shoot** (dist 3.5 is already past it). Old code retreated here;
new code holds. That is the whole "ucieka za daleko" fix.
dist 1.5, cd 25: Cd = 2.97 → desired = min(4, 4 + (2.97+2.5)/2)
= 4 → -1 = 3.0: step back exactly 1.5, not a blind 2.0.

### What we adopt, what we skip (2026-10-20 port)

Adopted: continuous desired distance replacing the fixed 2.0-tile
step (capped at KITE_STEP_TILES, gated by the 0.5-tile step-gain
floor); melee-closer term (-1 tile when outranging by 2+);
latency-relative framing (KITE_LATENCY_FRAMES = 4, consistent with
the existing resteps = latency+6 cadence). Kept untouched: margin
gate (when to kite at all), trade-shot, animation guard,
frozen-STOP, ride, glide approach, attack-only-on-new/overdue-target.
Wounded hold (owner 2026-10-20, PurpleWave hitsLeft shape applied to
the hold point): at or under KITE_WOUNDED_HITS_LEFT survived Zealot
passes (~half health) the Dragoon skips the melee-closer and holds
the max-range edge minus KITE_WOUNDED_EDGE_INSET_TILES (0.4) - the
bleeding unit stops donating contact and retreats longer (bigger gap
to the same cap), no cross-unit synchronization anywhere (neither
Stardust nor PurpleWave syncs fire; simultaneity is emergent same-
cooldown, coherence comes from not blocking friends out of range).
"Healthy steps toward the enemy to draw fire" deliberately NOT
adopted: no reference bot does it, enemy targeting is not steerable
from our side - the wounded-back ordering is the whole mechanism.
Withdrawn 2026-10-20: a Zealot front-line rule (no kite on cd>=7
with Dragoons behind) - falsified before commit, no game spent.
Range-0 units compute desired distance 0, so the kite step length is
identically 0 past point blank: Zealots never kite-step, with or
without Dragoons behind (the cd>=7 exception changes nothing;
pinned in DragoonKiteTest so reviving it stays conscious). The
ZealDrag surround does not come from dancing Zealots - they trade
contact unconditionally.
Wound ladder (owner 2026-10-20): wounded holds the max-range edge
(6 or fewer survived passes); CRITICAL holds fire below 45 total
health unless the enemy stands at 3.2+ tiles or the shot lands
within 5 frames (Stardust's attack-instead-of-step window); a
surrounded Dragoon (3+ melee within 3 tiles) stands and shoots when
the shot lands within 6 frames or the nearest enemy keeps 3.1+
tiles - escape is impossible, retreat is donated damage.
Skipped (open, needs threat-velocity tracking): the fleeing-target
branch ("do not kite out of range"); predicted positions (we use
current dist, re-evaluated every 10f); threat-aware pathing (still
straight-line + compass); per-unit iscript tables (generic
6-frame/10-frame windows instead).
