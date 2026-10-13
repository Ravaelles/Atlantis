# Building blockers — what stops a construction, per site and per mechanism

Status 2026-10-12: living document for the Gateway/Pylon/Assimilator refusal
hunt (NEXT #59, #62). Three sections with different burdens of proof:

- **Certain** - measured in a game log or in source, with the evidence named.
  Act on these.
- **Refuted** - an idea that died to a measurement, with what killed it.
  Do not re-open without new evidence.
- **Open** - each with the exact probe or run that would settle it. Do not
  patch these blind.

A refusal looks identical from the score line in every case (a construction
exists, its builder walks to it, weeks later `took too long`), so the site and
the numbers decide which mechanism it is - never the shape alone.

## Certain

1. **Poverty refuses through `Game.canMake`.** Measured on Gateway [96,111]:   every reachability answer true, every tile-rule answer true, power true,
   empty footprint, terrain ok - and `canBuild=false` with the sub-flag
   bisection isolating `minerals(28) < 150` (`canBuild000=true`,
   `100=false`, `010/001=true`). Evidence: `BUILD_BREAKDOWN` lines in
   `E2E_LOGS/20261012_2*_short-Protoss-vs-Zerg/bot.log`, `Game.canMake`
   source (`Game.java:1293`). Consequence: when the bank is under price at
   attempt time, the engine is right and no placement fix can help.
2. **Poverty is A cause, not THE cause.** Owner observation plus run logs:
   refusals happen with 400-600 minerals banked against a 150 price just as
   often. A broke attempt explains itself; a rich attempt needs sections 3-4.
3. **The [14,51] site refuses on POWER (`hasPower=false`).** Settled 2026-10-13
   (`20261013_100525_short`, Gateway, 315 refused attempts): `noBuilder=false
   withBuilder=false`, `canBuild000=false` (even the most permissive sub-flag),
   `explored=12/12`, `canAfford=true` (minerals=180), `unitsInRect=1 [BUILDER]`
   (only the assigned builder), `terrainOnly=ok` - and `hasPower=false`. The
   engine's own power answer is the refusing term; occupancy is exonerated
   (builder excluded by rule, nothing else on the footprint). Upstream defect:
   the legacy finder requested a Gateway outside any Pylon field (NEXT #50 -
   it never checks power), so the refusal is correct and the site is wrong.
4. **A builder that never settles is never accepted.** Measured:
   `distToBuild=1.3 isMoving=true lastCommand=Move` refused every frame until
   the 35 s cancel. Fix in tree: stop on bare `lastCommand==Move` inside the
   1.4-tile threshold (verified: Gateway=2/Zealot=5 in game, was 0/0).
   Variance between runs remains - the far site is reached late, if at all.
5. **Uncompleted units take no order.** Measured: `workerCompleted=false` ->
   `canCommand=false`, both gather and rightClick refused, `order=Nothing`
   from frame 30 to game end, while our flag claimed gathering. Fixed in
   mining managers, `FreeWorkers` and builder replacement (NEXT #63, closed
   with game verification).
6. **Duplicate constructions + requeue veto + relaxed-margin overflow.**
   All measured, all fixed with mutation-proven tests: #57 (leak), #59 requeue
   gate (cancel-before-count), #64 (`!relaxedBoundaryMargin` guard). Archive:
   `CASE-STUDIES.md`, this file does not repeat them.
7. **A refinery builds only on its geyser's exact tile.** Measured
   `20261013_101406`: `GAS_ACCEPTED win=0 tile=[117,4]`, both shifts refused
   client-side (`noBuilder=false`); the server demands a geyser under the sent
   position's center (`can_place_building` + `build_refinery` via
   `get_building_at_center_position`, `/sc-ai/OpenBW/openbw/bwgame.h`). Gas
   needs no power (code + live `hasPower=true`). Rotation experiment removed;
   the request tile is exact. Roster proof: `Assimilator=1`.
8. **An accepted Protoss order can die silently server-side.** Read in
   `/sc-ai/OpenBW/openbw/bwgame.h` (`order_PlaceProtossBuilding`): within
   70 px (88 px refinery) of the target the server re-checks supply+resources
   and `can_place_building` NOW - on NO, silent `order_done` to
   `return_to_idle` (our `order=PlayerGuard` + stale `lastCommand=Build`).
   Matches the Core @4697 signature; prime suspect the bank between issue and
   warp (minerals 194 -> 68, worker pump training through it). JBWAPI warns of
   exactly this ("a small chance for a command to fail after passed to
   Broodwar", `Unit.build` javadoc).

## Refuted (do not re-open without new evidence)

- **Regions block placement.** `Region mismatch` went 549 -> 0 after routing
  through `MapTiles.hasPathBetween`; live lines read all-true since.
  (Per-site note: exonerated where measured. The [14,51] refusal never got a
  hasPath reading - its split run will supply one.)
- **The search area is unbuildable terrain / invisible tiles.** Probe data:
  2422 buildable tiles in the search square, 1149 invisible tiles answering
  `buildInc=true`. Correlation (offending tiles mostly unseen), not cause.
- **The -64/-32 geyser offset is the Assimilator bug.** The arithmetic is the
  centre-to-top-left conversion, so request == engine tile by construction.
  Hardened anyway (Stardust pattern: answer the geyser's own tile via the new
  `AUnit.tilePosition()`), because equality-by-arithmetic is one rounding
  away from a 2-off miss.
- **The hasPath patch regresses refineries.** `hasPathOverTerrain`
  (`Game.java:2491`) errs towards reachable; the builder-path passes next to a
  geyser. Read, not guessed.
- **Fog blocks the build command.** `explored=4/4` and `12/12` on every refused
  footprint measured; `checkExplored=false` by default on the search path.
- **Worker stall theories: stale flags, TreeSet identity, never iterated.**
  All three checked and ruled out on the way to the completed-flag root (#63).
- **Gather-hijack / alternating manager names.** Withdrawn twice (#48, #61):
  the sleep gate stamped dispatcher names over the true owner (#67, fixed).

## Open (probe named, no patch)

- **O2. Why the bank is low when it is low.** Spending race (worker pump burns
  income before the Gateway attempt) vs weak mining. Needs: bank sampled at
  attempt frames across runs (one `WORKER_GATE`-style line already exists;
  read it, do not add another probe yet).
- **O3. Exit-path hang frame (#69).** Proven to hang between trace write and
  exit; 20 s (now 15 s) backstop bounds it. Still needs one `jstack` catch.
