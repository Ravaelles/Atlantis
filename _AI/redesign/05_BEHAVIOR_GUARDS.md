# Track 05: Sequential behavior levels with disjoint guards

> Priority: SPEC ONLY - no code lands here until the production v1 cut
> (#45, #144-147) is done. This document is the shape waiting, written
> down so the idea does not rot while started work gets finished first
> (owner direction: finish what's started, no new big projects).
> Sources: this repo's battle package (`atlantis.battle`,
> `atlantis.battle.targeting`, `atlantis.battle.escape`) and the
> frozen-failure post-mortems in `_AI/NEXT.md` #182/#183.

## 0. The failure mode, with receipts

Overlapping guards - two branches answering the same question with
different rules, priority decided by code position instead of by
design - are the documented way battles break here, twice verified:

- Old tree: competing dispatchers overwrote `usedManager` and the unit
  history, which made alternating dispatcher names look like the
  builder being hijacked (NEXT #48/#61 withdrew that reading; the
  mechanism is recorded in `Manager.java` above
  `invokeFromParent`). Order was implicit, so reordering broke things
  no test named.
- Live today: `focusInRangeTarget` refuses hp<=15, ALL zerglings and
  ALL workers via `isValidTarget`, while the `nearestEngageableEnemy`
  fallback in the same chain applies none of those bans. Same
  question ("which enemy do I engage?"), different answers depending
  on which branch runs first - priority by code position, invisible
  unless you read both. Plus the hold gates: veto 2.0 / contact 2.5 /
  needsKite (range+3) / isUnderAttack - four overlapping "is the enemy
  close/dangerous?" predicates with different radii AND different data
  sources (`enemiesNear` vs `Select.enemy` vs HP history). Game
  144553 Dragoon#162: all four blind for 200 frames while telemetry
  counted 8 enemies within 2 tiles (n2:8), then kiting with zero
  displacement until death. Verified, not hypothesized (NEXT #182).

The lesson both times: the file layout was never the axis. Implicit
order + overlapping guards break; explicit order + disjoint guards
don't. (Nested ifs hide the complexity; a flat ordered list shows
it - same lesson, applied to the call shape.)

## 1. The principle: the unit as a whole, decided level by level

Treat one combat unit as a whole and introduce the pattern (strategy)
only at specific sequential levels. Each level narrows the decision;
no level re-decides the one above it. That narrowing IS the continuity
guarantee - no escapes into competing submanagers:

- **L0 viability** (one place): alive, armed, orderable. Nothing else
  reads these conditions; every branch below assumes them.
- **L1 posture** (exactly one): kite / hold / fight / duel-yield /
  retreat... Decided by ONE decider per tick. Branches partition the
  state space: either mutually exclusive predicates, or an ordered
  list with the priority written down next to it (never emergent
  from which `if` comes first in a method).
- **L2 execution** (parameters only): glide point, kite step, focus
  target. L2 never re-decides L1 mid-frame. Once L1 commits for the
  frame window, L2 executes (existing precedent: `DuelMemory.stepped`
  + the reposition ride - a riding step is not re-decided).

## 2. What "disjoint" means, checkably

For any game state, at most one L1 branch's guard is true. Where
overlap is intentional, the priority is written down, not emergent:
example - a wounded chewer both breaks the hold (#182) AND selects
the target (#184 rule 0); the written rule is "break first, then the
fight branch owns target selection", never "whichever `if` runs
first". Verification is a sim matrix, not a code review: sweep the
guard dimensions (distance, hp, wound, cooldown) across each
threshold and assert exactly one branch owns each cell.
Order-sensitivity pins: swapping two adjacent branches in the pinned
order must either fail the suite or be proven neutral by a test that
says so. The old failure mode (silent reorder breakage) becomes
impossible by construction: there is exactly one order, in one
place, and the suite knows it.

## 3. The Behavior shape (future)

Fresh small classes with `applies()` (reads ONLY its level's inputs)
+ `act()` (issues the order) - not the old Manager framework: no
`usedManager` bookkeeping, no interrupt chains, no per-unit manager
field; that baggage stays buried. The order lives ONCE, as an
explicit ordered list (in-house precedent: `Commander.subcommanders()`
documents "Order matters: it is the execution order"). Each
`applies()` doubles as the NN plug point behind OFF/DRY_RUN/LIVE
flags per behavior (NEXT #185): hand rule or NN score, fallback to
hand on uncertainty, deterministic mode permanent. BattleCommander
iterates the chain - pure delegation, CONVENTIONS §23 intact.

## 4. Relation to open items

- #182: the hold gates unified under sections 1-2 (one proximity
  source, damage gate that can fire, unstick that watches holders).
- #183: the damage signal the gates read (battle-hook snapshot,
  not manager-updated history).
- #184: TargetLadder IS the L2 rule list for targeting; its
  coordination review decides which guards survive into it.
- #185: the learner replaces hand-written gate conditions; the
  levels, the single order and the disjointness are what make a
  learned policy safe to deploy at all.
- #45, #144-147: the production cut lands first. Slices here stay
  spec + sim pins until then.
