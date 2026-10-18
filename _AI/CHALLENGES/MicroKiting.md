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
