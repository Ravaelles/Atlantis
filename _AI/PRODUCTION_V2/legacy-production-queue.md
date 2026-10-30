# Legacy production walk queue

Owner order 2026-10-28: every class in dynamic/, orders/, requests/ walked one by one along _AI/PRODUCTION_V2/__MIGRATION.md (queue deletion Phase 4 LAST). Protoss paths only; TERRAN/ZERG moved to src-old/ (excluded, never repaired); neutral core touched only when it blocks a Protoss step.
Mark: [ ] todo, [x] done (commit hash), [~] excluded, [>] walked with an open delta noted on the row.


## dynamic
- [x] Q1 `AbundanceCommander.java` [neutral] (P4?) — walked 2026-10-28: legacy shell; economy abundance is the worker-goal seam.
- [x] Q2 `DynamicBuildingsCommander.java` [neutral] (P4?) — walked 2026-10-28: Protoss-only; V2 engine owns policy (see Q26).
- [x] Q3 `DynamicCommanderHelpers.java` [neutral] (P4?) — walked 2026-10-28: buildToHave helper; V2 goals emit directly.
- [x] Q4 `DynamicProductionCommander.java` [neutral] (P4?) — walked 2026-10-28: legacy root shell; V2 engine owns policy (not registered in LIVE).
- [x] Q5 `DynamicUnitAndTechProducerCommander.java` [neutral] (P4?) — walked 2026-10-28: Protoss-only; V2 engine owns policy (see Q28).

## dynamic/expansion
- [x] Q6 `ExpansionCommander.java` [neutral] (P5?) — walked 2026-10-28: covered by V2 (Protoss-only shell; V2 expansion goal owns timing; last-expanded stamp is write-only).
- [x] Q7 `PendingNextBase.java` [neutral] (P5?) — deleted 2026-10-28 (zero callers; expansion siting is the planner seam).

## dynamic/expansion/decision
- [x] Q8 `CancelNotStartedBases.java` [neutral] (P5?) — walked 2026-10-28: covered by V2, no port. (in-flight suppression in every V2 goal).
- [ ] Q9 `ExpansionUnderPressure.java` [neutral] (P5?) — walked 2026-10-28: defend-rush gate has no V2 counterpart; tuning delta, needs a game proof.
- [x] Q10 `ShouldExpand.java` [neutral] (P5?) — walked 2026-10-28: covered by V2, no port. (Protoss-only; V2 expansion goal owns the timing).

## dynamic/expansion/protoss
- [x] Q11 `ProtossCancelExpansionCommander.java` [PROTOSS] (P5?) — walked 2026-10-28: covered by V2, no port. (in-flight suppression kills re-fire).
- [x] Q12 `ProtossExpandNow.java` [PROTOSS] (P5?) — walked 2026-10-28: covered by V2, no port. (V2 expansion goal).
- [x] Q13 `ProtossExpansionCommander.java` [PROTOSS] (P5?) — walked 2026-10-28: covered by V2, no port. (V2 expansion goal).
- [x] Q14 `ProtossShouldExpand.java` [PROTOSS] (P5?) — walked 2026-10-28: covered by V2, no port. (baseline timing; muta/rush/bank deltas stay open).
- [x] Q15 `ProtossShouldExpandToNaturalBase.java` [PROTOSS] (P5?) — walked 2026-10-28: covered by V2, no port. (natural siting is the planner seam).
- [x] Q16 `ProtossShouldExpandToThirdAndLaterBase.java` [PROTOSS] (P5?) — walked 2026-10-28: covered by V2, no port. (late-base siting is the planner seam; 4-cap vs 7-cap delta open).

## dynamic/expansion/secure/terran
- [~] Q17 `SecuringBaseAsTerran.java` [TERRAN] (P5?)
- [~] Q18 `SecuringWithBunker.java` [TERRAN] (P5?)
- [~] Q19 `SecuringWithTurret.java` [TERRAN] (P5?)

## dynamic/expansion/terran
- [~] Q20 `TerranEarlyExpansion.java` [TERRAN] (P5?)
- [~] Q21 `TerranExpansionCommander.java` [TERRAN] (P5?)
- [~] Q22 `TerranShouldExpand.java` [TERRAN] (P5?)
- [~] Q23 `TerranShouldExpandToNatural.java` [TERRAN] (P5?)

## dynamic/expansion/zerg
- [~] Q24 `ZergExpansionCommander.java` [ZERG] (P5?)
- [~] Q25 `ZergShouldExpand.java` [ZERG] (P5?)

## dynamic/protoss
- [x] Q26 `ProtossDynamicBuildingsCommander.java` [PROTOSS] (P4-demand?) — walked 2026-10-28: covered by V2, no port. (commander shell; V2 engine owns policy).
- [x] Q27 `ProtossDynamicTechResearch.java` [PROTOSS] (P4-demand?) — walked 2026-10-28: covered by V2, no port. (queue-then-issue is the V2 dispatcher seam).
- [x] Q28 `ProtossDynamicUnitProductionCommander.java` [PROTOSS] (P4-demand?) — walked 2026-10-28: covered by V2, no port. (commander shell; V2 engine owns policy).
- [x] Q29 `ProtossNewGasBuildingCommander.java` [PROTOSS] (P4-demand?) — walked 2026-10-28: covered by V2, no port. (V2 gas goal).
- [x] Q30 `ProtossProductionDiagnostics.java` [PROTOSS] (P4-demand?) — walked 2026-10-28: covered by V2, no port. (idle-rich watchdog is the health-watcher seam).
- [x] Q31 `ProtossSecureBasesCommander.java` [PROTOSS] (P4-demand?) — walked 2026-10-28: covered by V2, no port. (fortification + detection goals).
- [x] Q32 `ProtossSpecificBuildingsCommander.java` [PROTOSS] (P4-demand?) — walked 2026-10-28: covered by V2, no port. (commander shell; V2 engine owns policy).

## dynamic/protoss/buildings
- [x] Q33 `ProduceArbiterTribunal.java` [PROTOSS] (P4-demand?) — ported 2026-10-28 as the V2 Tribunal goal (unknown reads late; `ReaverChainTest`).
- [x] Q34 `ProduceCannonAtMain.java` [PROTOSS] (P4-demand?) — walked 2026-10-28: covered by V2, no port. (fortification + detection cannons).
- [x] Q35 `ProduceCannonAtNatural.java` [PROTOSS] (P4-demand?) — walked 2026-10-28: covered by V2, no port. (fortification + detection cannons).
- [x] Q36 `ProduceCannonAtNaturalOrMain.java` [PROTOSS] (P4-demand?) — walked 2026-10-28: covered by V2, no port. (dispatcher over Q34/Q35).
- [x] Q37 `ProduceCitadelOfAdun.java` [PROTOSS] (P4-demand?) — ported 2026-10-28 as the V2 Citadel goal (verbatim gates; throttle stays behind; `CitadelLegsTest`).
- [x] Q38 `ProduceCyberneticsCore.java` [PROTOSS] (P4-demand?) — walked 2026-10-28: covered by V2, no port. (scheduler pulls Core as Dragoon prereq).
- [x] Q39 `ProduceFirstAssimilator.java` [PROTOSS] (P4-demand?) — walked 2026-10-28: covered by V2, no port. (V2 gas goal).
- [x] Q40 `ProduceForge.java` [PROTOSS] (P4-demand?) — walked 2026-10-28: covered by V2, no port. (board ForgeGoal + detection Forge).
- [x] Q41 `ProduceGateway.java` [PROTOSS] (P4-demand?) — walked 2026-10-28: covered by V2, no port. (V2 gateway demand).
- [x] Q42 `ProduceNaturalWallIn.java` [PROTOSS] (P4-demand?) — walked 2026-10-28: covered by V2, no port. (board WallInGoal gate).
- [x] Q43 `ProduceObservatory.java` [PROTOSS] (P4-demand?) — walked 2026-10-28: covered by V2, no port. (detection chain).
- [ ] Q44 `ProducePylonNearEveryBase.java` [PROTOSS] (P4-demand?) — walked 2026-10-28: legacy-only per-base power (7min+); open, needs an expansion vehicle.
- [x] Q45 `ProduceRoboticsFacility.java` [PROTOSS] (P4-demand?) — walked 2026-10-28: covered by V2, no port. (detection chain).
- [x] Q46 `ProduceRoboticsSupportBay.java` [PROTOSS] (P4-demand?) — ported 2026-10-28 as the V2 Bay goal (verbatim minus Terran floor; `ReaverChainTest`).
- [x] Q47 `ProduceShieldBatteryAtNatural.java` [PROTOSS] (P4-demand?) — deleted 2026-10-28 (dead upstream, sole caller unhooked).
- [x] Q48 `ProduceStargate.java` [PROTOSS] (P4-demand?) — ported 2026-10-28 as V2 Stargate+Corsair goals (`StargateCorsairTest` 6/6).
- [x] Q49 `ProduceTemplarArchives.java` [PROTOSS] (P4-demand?) — ported 2026-10-28 as the V2 Archives goal (mission + BO-permission clauses dropped, noted; `TemplarChainTest`).

## dynamic/protoss/decisions
- [x] Q50 `AllowProduceDragoon.java` [PROTOSS] (P4-demand?) — ported 2026-10-28 as V2 army gates (188 cap, 184 veto + rich bypass, DT-saving; `ArmyGatesTest` 7/7).
- [x] Q51 `AllowProduceZealot.java` [PROTOSS] (P4-demand?) — ported 2026-10-28 as V2 army gates (180/175 brakes, DT-saving; `ArmyGatesTest` 7/7).

## dynamic/protoss/prioritize
- [x] Q52 `PrioritizeCyberneticsOverZealotsAndGateways.java` [PROTOSS] (P4-demand?) — walked 2026-10-28: covered by V2, no port. (first Core rides the BO; later Cores are scheduler prereqs).
- [x] Q53 `PrioritizeGatewaysVsProtoss.java` [PROTOSS] (P4-demand?) — walked 2026-10-28, ported 2026-10-29: 6-gateway cap vs unfortified Protoss (ordering half stays in V2 bands); `TechUpgradesTest` 3/3 cap pins.
- [x] Q54 `PrioritizeProtossBuildingsOverCombatUnits.java` [PROTOSS] (P4-demand?) — walked 2026-10-28: covered by V2, no port. (V2 priority bands order economy before army).
- [x] Q55 `ProtossCriticalStuffInQueue.java` [PROTOSS] (P4-demand?) — walked 2026-10-28: covered by V2, no port. (250 reserve for imminent tech is the wallet/reservation seam).

## dynamic/protoss/reinforce
- [x] Q56 `BuildPylonFirst.java` [PROTOSS] (P4-demand?) — walked 2026-10-28: covered by V2, no port. (power siting is the planner seam - PylonPowerSiting).
- [x] Q57 `ProtossExtraEarlyCannonCommander.java` [PROTOSS] (P4-demand?) — walked 2026-10-28: covered by V2, no port. (early-cannon siting rides fortification + detection goals).
- [x] Q58 `ProtossSecureBaseWithCannons.java` [PROTOSS] (P4-demand?) — walked 2026-10-28: covered by V2, no port. (counts delegate to board CannonGoal; siting is the planner).
- [x] Q59 `RequestCannonAt.java` [PROTOSS] (P4-demand?) — walked 2026-10-28: covered by V2, no port. (exact-cannon construction is the planner seam).
- [x] Q60 `ShouldSecureProtossBase.java` [PROTOSS] (P4-demand?) — walked 2026-10-28: covered by V2, no port. (gates already answer through board CannonGoal).

## dynamic/protoss/tech
- [x] Q61 `ResearchLegEnhancements.java` [PROTOSS] (P4-demand?) — ported 2026-10-28 as the V2 Legs goal (Citadel + 90 + 4 zealots; `CitadelLegsTest` 6/6).
- [x] Q62 `ResearchNow.java` [PROTOSS] (P4-demand?) — walked 2026-10-28: covered by V2, no port. (issue-at-free-building is the V2 dispatcher seam).
- [x] Q63 `ResearchProtossGroundArmor.java` [PROTOSS] (P4-demand?) — ported 2026-10-29 as the V2 armor ladder (weapons-L1 gate, verbatim bank/weak gates; `TechUpgradesTest`).
- [x] Q64 `ResearchProtossGroundWeapons.java` [PROTOSS] (P4-demand?) — ported 2026-10-29 as the V2 weapons ladder (verbatim gates, weak veto with minute-10 expiry; `TechUpgradesTest` 10/10).
- [x] Q65 `ResearchPsionicStorm.java` [PROTOSS] (P4-demand?) — ported 2026-10-28 as the V2 Storm goal (caster-standing gate; `TemplarChainTest` 7/7).
- [x] Q66 `ResearchSingularityCharge.java` [PROTOSS] (P4-demand?) — walked 2026-10-28: covered by V2, no port. (V2 tech goal).
- [x] Q67 `TechResearchCommander.java` [PROTOSS] (P4-demand?) — walked 2026-10-28: covered by V2, no port. (queue-then-issue is the V2 dispatcher seam).
- [ ] Q68 `TooWeakToTech.java` [PROTOSS] (P4-demand?) — walked 2026-10-28; rechecked 2026-10-31: the researcher chunk is fully ported/covered (Q61-Q67, Q69 done; weak veto lives verbatim in the V2 ladders), but the legacy researchers still call this predicate, so it dies WITH them in Phase 4, not alone. No standalone slice.
- [x] Q69 `UpgradeResearchCommander.java` [PROTOSS] (P4-demand?) — walked 2026-10-28: covered by V2, no port. (queue-then-issue is the V2 dispatcher seam).

## dynamic/protoss/units
- [x] Q70 `GatewayClosestToEnemy.java` [PROTOSS] (P4-demand?) — closed 2026-10-29 per owner: earliest-available registry wins, no proximity input anywhere; no value proof pursued.
- [x] Q71 `ProduceArbiters.java` [PROTOSS] (P4-demand?) — ported 2026-10-28 as the V2 Arbiter pump (cap 2; `ReaverChainTest`).
- [x] Q72 `ProduceCorsairs.java` [PROTOSS] (P4-demand?) — ported 2026-10-28 as the V2 Corsair pump (Zerg branch; Wraith branch needs enemyIsTerran, unported).
- [x] Q73 `ProduceDarkTemplar.java` [PROTOSS] (P4-demand?) — ported 2026-10-28 as the V2 DT pump (tiered caps, bay-outranks-first, lifetime cap becomes loss replacement).
- [x] Q74 `ProduceDragoon.java` [PROTOSS] (P4-demand?) — walked 2026-10-28: covered by V2, no port. (floor + pump + gates).
- [x] Q75 `ProduceHighTemplar.java` [PROTOSS] (P4-demand?) — ported 2026-10-28 as the V2 HT pump (tiered caps 2/3/4/6); the upstream disable dies with the legacy caller.
- [x] Q76 `ProduceObserver.java` [PROTOSS] (P4-demand?) — ported 2026-10-28 as the V2 observer pump (flag 3-or-1-per-40, lurkers 4, losses replaced; routine clock-eyes unported; HiddenUnitDetectionGoalsTest +1).
- [x] Q77 `ProduceReavers.java` [PROTOSS] (P4-demand?) — ported 2026-10-28 as the V2 Reaver pump (eyes-first vs air; `ReaverChainTest` 8/8).
- [x] Q78 `ProduceScarabs.java` [PROTOSS] (P4-demand?) — walked 2026-10-28: covered by V2, no port. (per-reaver rearming is the specialist-behavior seam, already running).
- [x] Q79 `ProduceShuttle.java` [PROTOSS] (P4-demand?) — ported 2026-10-28 as the V2 Shuttle goal (reaver-follow + weak-line gates; `ReaverChainTest`).
- [x] Q80 `ProduceZealot.java` [PROTOSS] (P4-demand?) — walked 2026-10-28: covered by V2, no port. (floor + gates + mix).
- [x] Q81 `ProtossBuildingToProduce.java` [PROTOSS] (P4-demand?) — walked 2026-10-28: train-at-best-building is what the V2 dispatcher does; nearest-enemy nuance rides Q70.
- [x] Q82 `TrainQueuedDragoonImmediately.java` [PROTOSS] (P4-demand?) — walked 2026-10-28: covered by absence - no queue, no expediting; the dispatcher issues the top plan item every frame.
- [x] Q83 `ZealotInsteadDragoon.java` [PROTOSS] (P4-demand?) — ported 2026-10-28 as the V2 ling-flood mix (dead guard dropped; pump gated too; `ArmyGatesTest` +2).

## dynamic/supply
- [>] Q84 `NeedDynamicSupply.java` [neutral] (P2-authorship) — walked 2026-10-28: pure contract, already board-consumed (`GoalBoardFeeder`), no engine deps; convergence with the flat-4 V2 goal open (WO-144 d1).
- [>] Q85 `ProtossNeedDynamicSupply.java` [neutral-by-path, PROTOSS-by-content] (P2-authorship) — walked 2026-10-28: bands 6/10/14 + unfinished veto, pinned (`ProtossNeedDynamicSupplyTest`); no refactor needed until the WO-144 d1 game proof.

## dynamic/terran
- [~] Q86 `TerranDynamicBuildingsCommander.java` [TERRAN] (P4?)
- [~] Q87 `TerranDynamicFactoryUnits.java` [TERRAN] (P4?)
- [~] Q88 `TerranDynamicInfantry.java` [TERRAN] (P4?)
- [~] Q89 `TerranDynamicTechResearch.java` [TERRAN] (P4?)
- [~] Q90 `TerranDynamicUnitsCommander.java` [TERRAN] (P4?)
- [~] Q91 `TerranNewGasBuildingCommander.java` [TERRAN] (P4?)
- [~] Q92 `TerranSpecificBuildingsCommander.java` [TERRAN] (P4?)

## dynamic/terran/abundance
- [~] Q93 `Abundance.java` [TERRAN] (P4?)
- [~] Q94 `AbundanceProduce.java` [TERRAN] (P4?)
- [~] Q95 `TerranAbundance.java` [TERRAN] (P4?)
- [~] Q96 `TerranAbundanceEarlyToMidGame.java` [TERRAN] (P4?)
- [~] Q97 `TerranAbundanceLateGame.java` [TERRAN] (P4?)
- [~] Q98 `TerranAbundanceTech.java` [TERRAN] (P4?)

## dynamic/terran/buildings
- [~] Q99 `ProduceAcademy.java` [TERRAN] (P4?)
- [~] Q100 `ProduceAddon.java` [TERRAN] (P4?)
- [~] Q101 `ProduceArmory.java` [TERRAN] (P4?)
- [~] Q102 `ProduceBarracks.java` [TERRAN] (P4?)
- [~] Q103 `ProduceBunker.java` [TERRAN] (P4?)
- [~] Q104 `ProduceComsatStation.java` [TERRAN] (P4?)
- [~] Q105 `ProduceControlTower.java` [TERRAN] (P4?)
- [~] Q106 `ProduceEngineeringBay.java` [TERRAN] (P4?)
- [~] Q107 `ProduceFactory.java` [TERRAN] (P4?)
- [~] Q108 `ProduceMachineShop.java` [TERRAN] (P4?)
- [~] Q109 `ProduceScienceFacility.java` [TERRAN] (P4?)
- [~] Q110 `ProduceStarport.java` [TERRAN] (P4?)
- [~] Q111 `ProduceTurretForBunker.java` [TERRAN] (P4?)
- [~] Q112 `ShouldProduceNewBunker.java` [TERRAN] (P4?)

## dynamic/terran/bunker
- [~] Q113 `HaveBunkerAt.java` [TERRAN] (P4?)
- [~] Q114 `HaveBunkerAtMainChoke.java` [TERRAN] (P4?)
- [~] Q115 `HaveBunkerAtNaturalChoke.java` [TERRAN] (P4?)
- [~] Q116 `ReinforceWithBunkerAtNearestChoke.java` [TERRAN] (P4?)
- [~] Q117 `TerranReinforceBasesWithBunkers.java` [TERRAN] (P4?)

## dynamic/terran/tech
- [~] Q118 `CloakingField.java` [TERRAN] (P4?)
- [~] Q119 `Lockdown.java` [TERRAN] (P4?)
- [~] Q120 `ResearchSiegeMode.java` [TERRAN] (P4?)
- [~] Q121 `ResearchStimpacks.java` [TERRAN] (P4?)
- [~] Q122 `ResearchU238.java` [TERRAN] (P4?)
- [~] Q123 `TerranInfantryArmor.java` [TERRAN] (P4?)
- [~] Q124 `TerranInfantryWeapons.java` [TERRAN] (P4?)

## dynamic/terran/turrets
- [~] Q125 `OffensiveTurretsCommander.java` [TERRAN] (P4?)
- [~] Q126 `ReinforceBunkersWithTurrets.java` [TERRAN] (P4?)
- [~] Q127 `TerranMissileTurret.java` [TERRAN] (P4?)
- [~] Q128 `TerranSecureBaseWithTurrets.java` [TERRAN] (P4?)
- [~] Q129 `TurretsForMain.java` [TERRAN] (P4?)
- [~] Q130 `TurretsForNonMain.java` [TERRAN] (P4?)

## dynamic/terran/turrets/offensive
- [~] Q131 `AnyOffensiveTurretNeeded.java` [TERRAN] (P4?)
- [~] Q132 `OffensiveTurrets.java` [TERRAN] (P4?)
- [~] Q133 `TurretNeededHereCommander.java` [TERRAN] (P4?)
- [~] Q134 `TurretsToContainEnemy.java` [TERRAN] (P4?)

## dynamic/terran/units
- [~] Q135 `ForceProduceUnit.java` [TERRAN] (P4?)
- [~] Q136 `MechInsteadOfInfantry.java` [TERRAN] (P4?)
- [~] Q137 `ProduceGhosts.java` [TERRAN] (P4?)
- [~] Q138 `ProduceGoliaths.java` [TERRAN] (P4?)
- [~] Q139 `ProduceMarines.java` [TERRAN] (P4?)
- [~] Q140 `ProduceMedicsAndFirebats.java` [TERRAN] (P4?)
- [~] Q141 `ProduceScienceVessels.java` [TERRAN] (P4?)
- [~] Q142 `ProduceTanks.java` [TERRAN] (P4?)
- [~] Q143 `ProduceVultures.java` [TERRAN] (P4?)
- [~] Q144 `ProduceWraiths.java` [TERRAN] (P4?)
- [~] Q145 `ShouldProduceInfantry.java` [TERRAN] (P4?)

## dynamic/workers
- [x] Q146 `AutoProduceWorkersCommander.java` [neutral] (P5?) — walked 2026-10-28: covered by V2, no port. (Protoss-only; V2 worker goal + dispatcher train).

## dynamic/zerg
- [~] Q147 `ZergDynamicBuildingsCommander.java` [ZERG] (P4?)
- [~] Q148 `ZergDynamicTechResearch.java` [ZERG] (P4?)
- [~] Q149 `ZergDynamicUnitsCommander.java` [ZERG] (P4?)
- [~] Q150 `ZergExpansionCommander.java` [ZERG] (P4?)
- [~] Q151 `ZergNewGasBuildingCommander.java` [ZERG] (P4?)

## dynamic/zerg/units
- [~] Q152 `ProduceHydras.java` [ZERG] (P4?)
- [~] Q153 `ProduceMutas.java` [ZERG] (P4?)
- [~] Q154 `ProduceZerglings.java` [ZERG] (P4?)

## orders/build
- [>] Q155 `ABuildOrder.java` [neutral] (P4-queue-last) — walked 2026-10-28: Phase 4; V2 BuildOrderRow reads it via fromLegacy (do not touch).
- [>] Q156 `ABuildOrderFactory.java` [neutral] (P4-queue-last) — walked 2026-10-28: Phase 4; Protoss-only branches (do not touch).
- [>] Q157 `ABuildOrderLoader.java` [neutral] (P4-queue-last) — walked 2026-10-28: Phase 4; V2 consumes the memorized opening through it.
- [>] Q158 `BuildOrderFileNotFoundException.java` [neutral] (P4-queue-last) — walked 2026-10-28: Phase 4, dies with the loader.
- [>] Q159 `BuildOrderNotationConverter.java` [neutral] (P4-queue-last) — walked 2026-10-28: Phase 4, dies with the loader.
- [>] Q160 `BuildOrderOwnsGame.java` [neutral] (P4-queue-last) — walked 2026-10-28: Phase 4; opening-ownership gate read by both policies.
- [>] Q161 `BuildOrderRowParser.java` [neutral] (P4-queue-last) — walked 2026-10-28: Phase 4, dies with the loader.
- [>] Q162 `BuildOrderSetting.java` [neutral] (P4-queue-last) — walked 2026-10-28: Phase 4, dies with the loader.
- [>] Q163 `BuildOrderSettings.java` [neutral] (P4-queue-last) — walked 2026-10-28: Phase 4, dies with the loader.
- [>] Q164 `CurrentBuildOrder.java` [neutral] (P4-queue-last) — walked 2026-10-28: Phase 4; V2 engine reads it, dies with the queue.
- [>] Q165 `ProtossBuildOrder.java` [neutral] (P4-queue-last) — walked 2026-10-28: Phase 4; the opening the V2 timeline will own.
- [>] Q166 `TerranBuildOrder.java` [neutral] (P4-queue-last) — walked 2026-10-28: Phase 4; dormant under D8, dies with the loader.
- [~] Q167 `ZergBuildOrder.java` [neutral] (P4-queue-last) — moved to src-old/ with the Zerg exclusion.

## orders/production/queue
- [>] Q168 `AbstractQueue.java` [neutral] (P4-queue-last) — walked 2026-10-28: Phase 4, queue base type (do not touch).
- [>] Q169 `ClearCountCache.java` [neutral] (P4-queue-last) — walked 2026-10-28: Phase 4, count-cache hygiene dies with the queue.
- [>] Q170 `CountInQueue.java` [neutral] (P4-queue-last) — walked 2026-10-28: Phase 4; still read by legacy gates, dies with them.
- [>] Q171 `CurrentQueue.java` [neutral] (P4-queue-last) — walked 2026-10-28: Phase 4, dies with the queue (do not touch).
- [>] Q172 `PriorityWaiters.java` [neutral] (P4-queue-last) — walked 2026-10-28: Phase 4; spend-gate read by legacy producers, V2 wallet owns the seam.
- [>] Q173 `Queue.java` [neutral] (P4-queue-last) — walked 2026-10-28: Phase 4; carries the V2-LIVE early-return guard, dies last.
- [>] Q174 `QueueFactory.java` [neutral] (P4-queue-last) — walked 2026-10-28: Phase 4, dies with the queue (do not touch).
- [>] Q175 `QueueInitializer.java` [neutral] (P4-queue-last) — walked 2026-10-28: Phase 4, dies with the queue (do not touch).
- [>] Q176 `QueueLastStatus.java` [neutral] (P4-queue-last) — walked 2026-10-28: Phase 4, dies with the queue (do not touch).
- [>] Q177 `RemoveFromQueue.java` [neutral] (P4-queue-last) — walked 2026-10-28: Phase 4, dies with the queue (do not touch).
- [>] Q178 `ReservedResources.java` [neutral] (P4-queue-last) — walked 2026-10-28: Phase 4; reservation math moves to the V2 wallet.
- [>] Q179 `SoonInQueue.java` [neutral] (P4-queue-last) — walked 2026-10-28: Phase 4, dies with the queue (do not touch).

## orders/production/queue/add
- [>] Q180 `AddToQueue.java` [neutral] (P4-queue-last) — walked 2026-10-28: Phase 4; V2 engine fallback touchpoint, dies with the queue.
- [>] Q181 `AddToQueueToHave.java` [neutral] (P4-queue-last) — walked 2026-10-28: Phase 4, dies with the queue (do not touch).
- [>] Q182 `History.java` [neutral] (P4-queue-last) — walked 2026-10-28: Phase 4, dies with the queue (do not touch).
- [>] Q183 `IndexForNewOrder.java` [neutral] (P4-queue-last) — walked 2026-10-28: Phase 4, dies with the queue (do not touch).
- [>] Q184 `OnlyOneAllowedOfType.java` [neutral] (P4-queue-last) — walked 2026-10-28: Phase 4, dies with the queue (do not touch).
- [>] Q185 `PreventDuplicateOrders.java` [neutral] (P4-queue-last) — walked 2026-10-28: Phase 4, dies with the queue (do not touch).
- [>] Q186 `TrimQueue.java` [neutral] (P4-queue-last) — walked 2026-10-28: Phase 4, dies with the queue (do not touch).

## orders/production/queue/events
- [>] Q187 `OrderStatusWasChanged.java` [neutral] (P4-queue-last) — walked 2026-10-28: Phase 4, dies with the queue (do not touch).

## orders/production/queue/order
- [>] Q188 `ForcedDirectProductionOrder.java` [neutral] (P4-queue-last) — walked 2026-10-28: Phase 4; direct-train record still minted by the shared order funnel.
- [>] Q189 `OrderReservations.java` [neutral] (P4-queue-last) — walked 2026-10-28: Phase 4; moves to the V2 wallet.
- [>] Q190 `OrderStatus.java` [neutral] (P4-queue-last) — walked 2026-10-28: Phase 4, dies with the queue (do not touch).
- [>] Q191 `Orders.java` [neutral] (P4-queue-last) — walked 2026-10-28: Phase 4, dies with the queue (do not touch).
- [>] Q192 `OrdersFilters.java` [neutral] (P4-queue-last) — walked 2026-10-28: Phase 4, dies with the queue (do not touch).
- [>] Q193 `ProductionOrder.java` [neutral] (P4-queue-last) — walked 2026-10-28: Phase 4; the synthetic carrier GameOrderDirector mints - the seam to replace.
- [>] Q194 `ProductionOrderHandler.java` [neutral] (P4-queue-last) — walked 2026-10-28: Phase 4, dies with the queue (do not touch).
- [>] Q195 `ProductionOrderPriority.java` [neutral] (P4-queue-last) — walked 2026-10-28: Phase 4; priority vocabulary maps onto V2 bands at deletion.

## orders/production/queue/updater
- [>] Q196 `IsOrderCompleted.java` [neutral] (P4-queue-last) — walked 2026-10-28: Phase 4; readiness polling is the V2 scheduler seam.
- [>] Q197 `IsOrderInProgress.java` [neutral] (P4-queue-last) — walked 2026-10-28: Phase 4; readiness polling is the V2 scheduler seam.
- [>] Q198 `QueueRefresher.java` [neutral] (P4-queue-last) — walked 2026-10-28: Phase 4; refresh polling is the V2 scheduler seam.
- [>] Q199 `ReadyToProduceOrder.java` [neutral] (P4-queue-last) — walked 2026-10-28: Phase 4; readiness polling is the V2 scheduler seam.
- [>] Q200 `ThisManyUnitsByDefault.java` [neutral] (P4-queue-last) — walked 2026-10-28: Phase 4, dies with the queue (do not touch).

## orders/requirements
- [x] Q201 `AllowToProduceEarlyWithoutRequirements.java` [neutral] (P4?) — walked 2026-10-28: covered by V2, no port. (door guard stays per WO-144 non-goals).
- [x] Q202 `Requirements.java` [neutral] (P4?) — walked 2026-10-28: covered by V2, no port. (door guards stay per WO-144 non-goals).

## orders/zerg
- [~] Q203 `ProduceZergUnit.java` [ZERG] (P4?)

## requests
- [>] Q204 `ADetectorRequest.java` [neutral] (P4?) — walked 2026-10-28: detector requests ride the detection chain; queue half waits for Phase 4.
- [x] Q205 `AntiAirBuildingCommander.java` [neutral] (P4?) — walked 2026-10-28: covered by V2, no port. (Protoss-only; doctrine ported as V2 cannon goals).
- [x] Q206 `AntiLandBuildingCommander.java` [neutral] (P4?) — walked 2026-10-28: covered by V2, no port. (Protoss-only; doctrine ported as V2 cannon goals).
- [x] Q207 `DynamicBuildingCommander.java` [neutral] (P4?) — walked 2026-10-28: covered by V2, no port. (Protoss-only branches; doctrine ported).
- [x] Q208 `ProductionRequests.java` [neutral] (P4?) — walked 2026-10-28: covered by V2, no port. (Protoss-only singleton; policy moved to goals).
- [x] Q209 `ProtossRequests.java` [neutral] (P4?) — walked 2026-10-28: covered by V2, no port. (request shell; policy moved to goals).
- [>] Q210 `RemoveExcessiveOrders.java` [neutral] (P4?) — walked 2026-10-28: queue-trimming dies with the queue in Phase 4.
- [x] Q211 `TerranRequests.java` [neutral] (P4?) — walked 2026-10-28: covered by V2, no port. (dormant under D8; never repaired).

## requests/produce
- [>] Q212 `BuildGate.java` [neutral] (P4-produce-pipe) — walked 2026-10-28: execution seam, keep until Phase 4.
- [>] Q213 `BuildGates.java` [neutral] (P4-produce-pipe) — walked 2026-10-28: execution seam, keep until Phase 4.
- [>] Q214 `ProduceAddon.java` [neutral] (P4-produce-pipe) — walked 2026-10-28: execution seam, keep until Phase 4.
- [>] Q215 `ProduceBuilding.java` [neutral] (P4-produce-pipe) — walked 2026-10-28: execution seam (Protoss-only branches), keep until Phase 4.
- [>] Q216 `ProduceOrdersFromQueue.java` [neutral] (P4-produce-pipe) — walked 2026-10-28: legacy issue path, dies with the queue in Phase 4.
- [>] Q217 `ProduceUnit.java` [neutral] (P4-produce-pipe) — walked 2026-10-28: execution seam, keep until Phase 4.
- [>] Q218 `ProduceWorker.java` [neutral] (P4-produce-pipe) — walked 2026-10-28: execution seam, keep until Phase 4.

## requests/protoss
- [x] Q219 `ProtossPhotonCannonAntiAir.java` [PROTOSS] (P4-demand?) — walked 2026-10-28: covered by V2, no port. (picked up by the ported AntiAir commander).
- [x] Q220 `ProtossPhotonCannonAntiLand.java` [PROTOSS] (P4-demand?) — walked 2026-10-28: covered by V2, no port. (picked up by the ported AntiLand commander).

## requests/zerg
- [~] Q221 `ZergRequests.java` [ZERG] (P4?)
- [~] Q222 `ZergSporeColony.java` [ZERG] (P4?)
- [~] Q223 `ZergSunkenColony.java` [ZERG] (P4?)
