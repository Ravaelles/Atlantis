# Goal approach (mid/long-term organization)

Status 2026-10-15: direction approved by owner (board-first, strangler
in-tree, zero copy-paste). This file states the shape; `_AI/__DIAGRAM.puml`
draws it. Normative details live in CONVENTIONS §5 (architecture) and §21
(domain vs machinery).

## The loop

Every frame answers one question - "what matters now?" - and only that:

```
game state -> BoardState -> GoalBoard ranks goals -> wallet serves
top-down (commit price, skip what the bank cannot cover) -> queue rows
-> builder execution -> engine. Skipped goals wait for the next frame,
never die. Started work always finishes (commitment).
```

Decision (what) and execution (how, did it work) meet only in the queue
and in telemetry. A missing building is therefore attributable by
construction: board said X (decision log) vs builder did Y (builder
log) - never "maybe everything".

## Layers

| Layer | Lives in | Role | Proved by |
|---|---|---|---|
| Engine | BWAPI/OpenBW/Wine | physics, truth | games |
| Leaves (keep, never rewrite) | `units` (AUnit, types, OrderSink), `map` (AMap, tiles), `cherryvis` | data + order transport | existing tests |
| Board machinery (generic) | `decisions.board` (Goal, BoardState, GoalBoard) | pure ranking; zero project imports | stub states, ms |
| Domain vocabularies (grow) | `decisions.goals.<race-phase>` (today: `protoss`) | game meaning: when a goal applies, its ladder rank, its price class | stub states |
| Consumers (thin) | `GoalBoardAdvisor` (log), `GoalBoardDriver` (preempt), `GoalBoardFeeder` (queue missing rows) | board -> legacy translation, one direction each | stub + twins + games |
| Legacy production (shrinks) | `production/dynamic/**`, `SupplyCommander`, pumps | executes until absorbed, then deleted same cycle | games, twins |
| Execution (stays) | `ConstructionsCommander`, builders, placement, `Queue` | builders, sites, engine refusals | BUILDER_LOG games |
| Combat (sibling, cluster Phase 0) | `battle.*` (BattleCommander, one cluster, evaluator gate, CLUSTER twin; legacy `combat/**` deleted to `src-old/`, see `__PLAN-COMBAT.md`) | one group moves between anchors, fights or retreats on one gate; senses game state directly, takes no orders from the board | OpenBW games with an enemy (blocked: NEXT #94) |
| V2 reference | `production/v2/**` | cutover target (DRY_RUN today); board is its Strategist half, timeline scheduler deferred | DRY_RUN logs |

## Growth rules

- New game meaning goes to a domain vocabulary (sibling package, never
  under `board/`). New races/phases are new siblings.
- The ladder is the only strategy knob; prices come from engine data,
  never memory.
- No new veto/gate in legacy producers, ever (ratchet). Arbitration
  changes land in the board or they do not land.
- Deletions need per-gate game proof (one gate, one game), never batch
  rewrites. Strangler order follows NEXT #45.
- Wallet, not winner: the board outputs a ranked list; top-only driving
  would starve low-ranked savers (measured: Nexus at rank 80).

## Non-goals

- No per-unit micro on the board (frames vs seconds, positions vs
  minerals - different currency, different layer).
- No timeline simulation until a measured case needs time-shifting; every
  measured starvation so far was a priority inversion, never timing.
- No tactical DDD (aggregates/repositories) - strategic only, per CONVENTIONS §21.
