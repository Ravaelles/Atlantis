# Architecture Redesign: Stardust vs. Atlantis

Remaining migration tracks from Stardust reverse engineering
(production + placement tracks are done and their specs deleted).

- `02_COMBAT.md` — Elimination of per-unit Manager Chains and static squads; porting Play → Squad → UnitCluster, Tactical Intent vs. Micro Execution, and FAP/OpenBW gating.
- `04_MAPLIB.md` — Own map-analysis library (immutable model, defense/drop spots, wall anchors + natural wall-in; JBWEB fork plugs in at M4).
- `05_BEHAVIOR_GUARDS.md` — Sequential behavior levels with disjoint guards (SPEC ONLY until the production v1 cut lands): the unit as a whole, one decider per level, continuity by narrowing; the order lives once, pins prove it. Backs #182/#183/#184, gates #185.

Historical context and baseline comparison: `STARDUST_VS_ATLANTIS.md`.
