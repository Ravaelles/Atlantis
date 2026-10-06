# Architecture Redesign: Stardust vs. Atlantis

Two independent migration tracks based on Stardust reverse engineering.
**Production (Track 01)** has higher priority and should be executed first, as it has fewer cross-dependencies with real-time micro.

- `01_PRODUCTION.md` — Elimination of `Queue` and `Construction` subsystem; porting Resource Timeline, Priority Scheduling, and Placement Reservation.
- `02_COMBAT.md` — Elimination of per-unit Manager Chains and static squads; porting Play → Squad → UnitCluster, Tactical Intent vs. Micro Execution, and FAP/OpenBW gating.

Historical context and baseline comparison: `STARDUST_VS_ATLANTIS.md`.
