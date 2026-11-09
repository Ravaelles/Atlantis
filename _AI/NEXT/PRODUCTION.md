# Production task file (single source of truth for production work)

NEXT.md carries exactly one delegation line here and nothing else.
Status per workstream; open questions name their owner and date.
No per-game evidence: game IDs appear only where a vehicle is
specified, never as proof-by-volume. Ratchet tests live only for
the migration they guard (one per seam/door, never a battery per
case) and are deleted or moved to `tests-aux/` when it closes.
Per CONVENTIONS §26, raw rows live in `_AI/WINE-MACRO-DATUMS.md`
(gitignored), never here.

## V3 single door (owner order 2026-11-04)

`_AI/NEXT/PRODUCTION_V3.md` - the migration plan: one entry, one
verdict, one exit. This file tracks everything else; the verdict
itself (including the footprint-center power rule, fixed live
2026-11-03 and pinned by `GatewayPsiEdgeTest`) belongs to V3 S1.

## Board and demand strategy (#91, active)

Pure board, zero engine imports (`BoardState` + ladder); domain
vocabulary next door in `decisions.goals.protoss`. Consumers live:
advisor, emergency-only worker driver, feeder
(supply/gateway/core/assimilator + sustained army rows), wallet,
commitment (`top` gets the queue + construction in-flight set).
No new arbitration gate in legacy producers, ever (ratchet).
Next, one at a time with a game proof each: per-gate deletions
(owner picks the family); Robotics/observer demand and the gateway
threshold thicket stay with current owners until game proof.

## Legacy dynamic commander coverage (owner question 2026-11-09)

Live loop runs V2 only (`ProductionCommander`: `ProductionOrdersCommander`
+ `ConstructionsCommander`); `DynamicProductionCommander` (with
`DynamicBuildingsCommander` + `ProtossDynamicUnitProductionCommander`) has no
live parent - orphaned, issuing nothing. V2 covers every legacy product
EXCEPT one proven gap (audited 2026-11-09 against `DynamicGoals`):
covered: Gateway, Observatory (+Robo via scheduler prereq), Forge,
cannons (fortification + hidden-unit response, still live via
`ProtossStrategyResponse`), Stargate, Corsair, Observer pump, Citadel,
Archives, High Templar, Storm, Dark Templar, Support Bay, Reaver,
Shuttle (incl. scarabs: brewed from the Reaver by the live combat
specialist `SpecialistBehaviors`, NEXT #198 - not a production goal by
design), Tribunal, Arbiter, weapons/armor/singularity/legs, workers,
supply, gas, expansion, Zealot/Dragoon pumps. Gap (no V2 goal, no live
caller):
- Forge-FE natural wall-in: `WallInGoal` is decided on the board but its
  only emitter (`ProduceNaturalWallIn`) is orphaned, and its issuance
  door (legacy queue) no longer issues. Vehicle: Forge-FE game.
Verdict: the chain stays until the wall-in gap closes with a sim pin +
game proof; `ProduceGateway`/`ProduceDragoon` deprecation stands (V2 owns
those). No blind deletion.

## Migration #144-#147 (owner-sequenced; V2 is already the default)

Landed: catalogue groundwork through A4g, B0 flow map, B1a/B1b
dead-race dispatch, B1 family (Pylon/Forge/Assim/Core/Cybernetics),
B2 scoped, #147 queue semantics (`isWaiting`/`setStatus` sole
writer). Open picks (all owner-gated): B1c first live game proof
per gate (gateway threshold thicket waits with its owner), B2
reader family, APositionFinder deletion (blocked: sited-fallback
is load-bearing; cannon path needs a vehicle), cannon catalogue
migration (no cannon vehicle in 98 games). #147 deletion wave
waits on parity runs (review 2026-11-09). #156 (four reservation
models) belongs here, not a blind merge. #155 gates registry and
#157 status semantics: verified, closed (no lines).

## Siting (see also V3 single door)

V3 owns the door. Tracked here because the proof queue lives with
the buildings, not the plan:
- Edge tiles at/beyond map bounds (V3 S2 sets the 4-tile rule).
- Corner-stall Pylon (owner live finding): tile doctrine holds,
  the stall never presents. No action.
- The "why 35-tile" search excursion still wants a BuilderLog
  game with twin rows.
- Unpowered [123,26] Gateway churn (2026-11-03, fixed: footprint
  center, pinned `GatewayPsiEdgeTest`); absorb this rule into V3
  S1 at review, do not fork it.
- #213 Pylon #3-4 sited at the natural: fixed (pins at main).
  Open: live proof on an island/UMS map with an expanding game
  (owner call - nothing in the vetted library triggers it;
  TauCross/Destination cannot).
- #236 unreachable build sites: fixed, terminal
  `ReachableTiles.between(main, site)` per path. Open: live proof
  on a real map (UMS has no BWEM areas by construction).
- #84 Wine Pylon issuance: pipeline healthy, stalls self-recover.
  Open: STARTED-twin naming on a legacy-minted warp (needs a
  non-V2 game - evaporates when V3 deletes the legacy door).
- DONE 2026-11-09: Pylon cap bands own one home (SOLID S3 slice):
  `production.dynamic.supply.SupplyPolicy` answers the
  in-flight cap pure (1 below 40, pair at 40+, rich bank lifts
  early - identical semantics, pinned `SupplyPolicyTest` 3/3);
  `ProduceBuilding.tooManyPylonsInFlight` delegates, counting
  stays with `ConstructionRequests`. Demand bands
  (`NeedDynamicSupply`, already pure), the V2 emission guard
  (`DynamicGoals`) and the sim-exit read stay separate - four
  different decisions, not one; unifying thresholds would change
  live-proven balance (owner call). Open as-is corner: rich bank
  lifts the early cap entirely, not just to the pair (pinned
  as-is, tightening is a balance call).

## Construction lifecycle

- **#150 stuck-IN_PROGRESS rows.** In place: opening gate, cid/oid
  twins, resite/adopt guards, builderless cleanup, sim-pinned
  2400f age cap. Implemented since: post-warp self-match widened
  2.5->6 tiles + ownership guard (19/25 "builder gone" demotes
  were false, measured on retained games); refused-tile resite
  + legacy spiral consults RefusedTiles (TTL-exact re-notes
  measured). Open: owner visual check of PREPARING, live overlap
  proof, builder-dead freeze proof (WO-150b, HP trail wired);
  discriminating demote-guard twin named, not implemented (owner
  decision - paths are load-bearing). Live proof of both fixes
  (halted).
- **#151 refused-tile memory.** Landed (`RefusedTiles`, 2400f TTL,
  catalogue decline with own twin, V2-direct consult). Open: a
  refused tile re-siting to a working tile in a live game.
- **#235 builder double-link deadlock.** Fixed: exclusive ownership
  on handover + assign-optimal-builder only for surviving rows
  (duplicate-steal ordering verified STEAL->HOLDS). Open: live
  no-recurrence proof (halted).
- **Builder variance.** Census done: parking proven common,
  handoff unobserved in all twinned games. Open: why a parked
  builder never issues (the refused-tile shape is now covered by
  the resite fix; needs live confirmation); BuilderLog games.
- **Second Gateway lateness.** Intermittent; no blind patch.
  Re-measure in post-fix games; no action until data.
- **Gateway demand vs null position (#192).** Sim pins cover
  self-overlap, ground-distance search, Pylon anchoring and the
  bounded 480-attempt WAIT. Open: longer-siege calibration (needs
  a >28800-frame vehicle - Wine horizon caps every game) and
  reservation release while waiting (tracked in #202).

## Gas, tech timing, equilibrium

- **B1-Assim (landed + live-proven).** Fires on gateway + bank or
  nearly-done Core; zero `REFRESH_CANCEL` in the proof game.
- **Assimilator blocked-base loop.** Fixed. Open: none - closed
  unless a blocked-base game recurs.
- **#85 armor research.** Independently gated from weapons L3.
  Parked: needs a mid-game vehicle + a longer research window.
- **#234 equilibrium (decided: save-for-expansion).** Bank spends
  on the expansion instead of trickling. Open: live proof of the
  pre-saturation window.
- **#203 Zerg matchup brake.** Landed + pinned. Open: twin
  attribution (brake row vs warp row) in a gas-rich mid-game.
- **#212 worker acquisition (owner call).** A waiting builder
  mines instead of idling (landed, pinned). Parked: the 250-gate
  second half (no young-base stall ever observed); the
  duplicate-order half rides #144.
- **#217 post-expansion stall.** Worker cap + stall fixed
  (`PostFiftyProductionTest`); open: the no-enemy half (army
  saturated, nothing dies) and live proof of the pre-saturation
  window.

## Readiness and live proof queue (halted: games run on owner call)

- **#71 deciding run:** 700 s+, one-base, low-bank. Vehicle spec
  exists; needs a per-run horizon (Wine default blocks it).
- One quiet game each (never alongside the suite): V3 edge rule
  vs unpowered Gateway (Steamhammer shape), refused-tile resite,
  post-warp match, #235 handover, #236 on a real map, #213 island
  expansion, #150 overlap proof, #151 refused-tile re-site, #84
  legacy warp (if V3 hasn't deleted the door yet).
- Owner data before verdicts: #202 (minerals at stall, dominant
  twin rows, Gateway count/state); #96 (painter labels + missions
  game); #34 (UMS opponent map); #144-#147 parity runs (review
  2026-11-09).

## Dropped from tracking (closed, history in git)

#237 geyser self-block (live-proven). #155 gates registry
(verified). #157 status semantics (verified). Deletions are final
per §7.
