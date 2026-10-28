# Legacy production walk queue

Owner order 2026-10-28: every class in dynamic/, orders/, requests/ walked one by one along _AI/PRODUCTION_V2/__MIGRATION.md (queue deletion Phase 4 LAST). Protoss paths only; TERRAN/ZERG moved to src-old/ (excluded, never repaired); neutral core touched only when it blocks a Protoss step.
Mark: [ ] todo, [x] done (commit hash), [~] excluded, [>] walked with an open delta noted on the row.


## dynamic
- [ ] Q1 `AbundanceCommander.java` [neutral] (P4?)
- [ ] Q2 `DynamicBuildingsCommander.java` [neutral] (P4?)
- [ ] Q3 `DynamicCommanderHelpers.java` [neutral] (P4?)
- [ ] Q4 `DynamicProductionCommander.java` [neutral] (P4?)
- [ ] Q5 `DynamicUnitAndTechProducerCommander.java` [neutral] (P4?)

## dynamic/expansion
- [ ] Q6 `ExpansionCommander.java` [neutral] (P5?)
- [ ] Q7 `PendingNextBase.java` [neutral] (P5?)

## dynamic/expansion/decision
- [ ] Q8 `CancelNotStartedBases.java` [neutral] (P5?)
- [ ] Q9 `ExpansionUnderPressure.java` [neutral] (P5?)
- [ ] Q10 `ShouldExpand.java` [neutral] (P5?)

## dynamic/expansion/protoss
- [ ] Q11 `ProtossCancelExpansionCommander.java` [PROTOSS] (P5?)
- [ ] Q12 `ProtossExpandNow.java` [PROTOSS] (P5?)
- [ ] Q13 `ProtossExpansionCommander.java` [PROTOSS] (P5?)
- [ ] Q14 `ProtossShouldExpand.java` [PROTOSS] (P5?)
- [ ] Q15 `ProtossShouldExpandToNaturalBase.java` [PROTOSS] (P5?)
- [ ] Q16 `ProtossShouldExpandToThirdAndLaterBase.java` [PROTOSS] (P5?)

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
- [ ] Q26 `ProtossDynamicBuildingsCommander.java` [PROTOSS] (P4-demand?)
- [ ] Q27 `ProtossDynamicTechResearch.java` [PROTOSS] (P4-demand?)
- [ ] Q28 `ProtossDynamicUnitProductionCommander.java` [PROTOSS] (P4-demand?)
- [ ] Q29 `ProtossNewGasBuildingCommander.java` [PROTOSS] (P4-demand?)
- [ ] Q30 `ProtossProductionDiagnostics.java` [PROTOSS] (P4-demand?)
- [ ] Q31 `ProtossSecureBasesCommander.java` [PROTOSS] (P4-demand?)
- [ ] Q32 `ProtossSpecificBuildingsCommander.java` [PROTOSS] (P4-demand?)

## dynamic/protoss/buildings
- [ ] Q33 `ProduceArbiterTribunal.java` [PROTOSS] (P4-demand?) — walked 2026-10-28: legacy-only, dead without Arbiter demand; ports behind the Archives chain.
- [x] Q34 `ProduceCannonAtMain.java` [PROTOSS] (P4-demand?) — walked 2026-10-28: covered by V2, no port. (fortification + detection cannons).
- [x] Q35 `ProduceCannonAtNatural.java` [PROTOSS] (P4-demand?) — walked 2026-10-28: covered by V2, no port. (fortification + detection cannons).
- [x] Q36 `ProduceCannonAtNaturalOrMain.java` [PROTOSS] (P4-demand?) — walked 2026-10-28: covered by V2, no port. (dispatcher over Q34/Q35).
- [ ] Q37 `ProduceCitadelOfAdun.java` [PROTOSS] (P4-demand?) — walked 2026-10-28: legacy-only (supply 70 + Obs); ports with the Archives/DT chain.
- [x] Q38 `ProduceCyberneticsCore.java` [PROTOSS] (P4-demand?) — walked 2026-10-28: covered by V2, no port. (scheduler pulls Core as Dragoon prereq).
- [x] Q39 `ProduceFirstAssimilator.java` [PROTOSS] (P4-demand?) — walked 2026-10-28: covered by V2, no port. (V2 gas goal).
- [x] Q40 `ProduceForge.java` [PROTOSS] (P4-demand?) — walked 2026-10-28: covered by V2, no port. (board ForgeGoal + detection Forge).
- [x] Q41 `ProduceGateway.java` [PROTOSS] (P4-demand?) — walked 2026-10-28: covered by V2, no port. (V2 gateway demand).
- [x] Q42 `ProduceNaturalWallIn.java` [PROTOSS] (P4-demand?) — walked 2026-10-28: covered by V2, no port. (board WallInGoal gate).
- [x] Q43 `ProduceObservatory.java` [PROTOSS] (P4-demand?) — walked 2026-10-28: covered by V2, no port. (detection chain).
- [ ] Q44 `ProducePylonNearEveryBase.java` [PROTOSS] (P4-demand?) — walked 2026-10-28: legacy-only per-base power (7min+); open, needs an expansion vehicle.
- [x] Q45 `ProduceRoboticsFacility.java` [PROTOSS] (P4-demand?) — walked 2026-10-28: covered by V2, no port. (detection chain).
- [ ] Q46 `ProduceRoboticsSupportBay.java` [PROTOSS] (P4-demand?) — walked 2026-10-28: legacy-only; ports with the Reaver/Shuttle chain.
- [ ] Q47 `ProduceShieldBatteryAtNatural.java` [PROTOSS] (P4-demand?) — walked 2026-10-28: disabled upstream (`if (true) return false`); delete at phase exit.
- [x] Q48 `ProduceStargate.java` [PROTOSS] (P4-demand?) — ported 2026-10-28 as V2 Stargate+Corsair goals (`StargateCorsairTest` 6/6).
- [ ] Q49 `ProduceTemplarArchives.java` [PROTOSS] (P4-demand?) — walked 2026-10-28: legacy-only vs Zerg; ports with the DT/HT chain.

## dynamic/protoss/decisions
- [x] Q50 `AllowProduceDragoon.java` [PROTOSS] (P4-demand?) — ported 2026-10-28 as V2 army gates (188 cap, 184 veto + rich bypass, DT-saving; `ArmyGatesTest` 7/7).
- [x] Q51 `AllowProduceZealot.java` [PROTOSS] (P4-demand?) — ported 2026-10-28 as V2 army gates (180/175 brakes, DT-saving; `ArmyGatesTest` 7/7).

## dynamic/protoss/prioritize
- [ ] Q52 `PrioritizeCyberneticsOverZealotsAndGateways.java` [PROTOSS] (P4-demand?)
- [ ] Q53 `PrioritizeGatewaysVsProtoss.java` [PROTOSS] (P4-demand?)
- [ ] Q54 `PrioritizeProtossBuildingsOverCombatUnits.java` [PROTOSS] (P4-demand?)
- [ ] Q55 `ProtossCriticalStuffInQueue.java` [PROTOSS] (P4-demand?)

## dynamic/protoss/reinforce
- [ ] Q56 `BuildPylonFirst.java` [PROTOSS] (P4-demand?)
- [ ] Q57 `ProtossExtraEarlyCannonCommander.java` [PROTOSS] (P4-demand?)
- [ ] Q58 `ProtossSecureBaseWithCannons.java` [PROTOSS] (P4-demand?)
- [ ] Q59 `RequestCannonAt.java` [PROTOSS] (P4-demand?)
- [ ] Q60 `ShouldSecureProtossBase.java` [PROTOSS] (P4-demand?)

## dynamic/protoss/tech
- [ ] Q61 `ResearchLegEnhancements.java` [PROTOSS] (P4-demand?)
- [ ] Q62 `ResearchNow.java` [PROTOSS] (P4-demand?)
- [ ] Q63 `ResearchProtossGroundArmor.java` [PROTOSS] (P4-demand?)
- [ ] Q64 `ResearchProtossGroundWeapons.java` [PROTOSS] (P4-demand?)
- [ ] Q65 `ResearchPsionicStorm.java` [PROTOSS] (P4-demand?)
- [ ] Q66 `ResearchSingularityCharge.java` [PROTOSS] (P4-demand?)
- [ ] Q67 `TechResearchCommander.java` [PROTOSS] (P4-demand?)
- [ ] Q68 `TooWeakToTech.java` [PROTOSS] (P4-demand?)
- [ ] Q69 `UpgradeResearchCommander.java` [PROTOSS] (P4-demand?)

## dynamic/protoss/units
- [ ] Q70 `GatewayClosestToEnemy.java` [PROTOSS] (P4-demand?)
- [ ] Q71 `ProduceArbiters.java` [PROTOSS] (P4-demand?)
- [x] Q72 `ProduceCorsairs.java` [PROTOSS] (P4-demand?) — ported 2026-10-28 as the V2 Corsair pump (Zerg branch; Wraith branch needs enemyIsTerran, unported).
- [ ] Q73 `ProduceDarkTemplar.java` [PROTOSS] (P4-demand?)
- [ ] Q74 `ProduceDragoon.java` [PROTOSS] (P4-demand?)
- [ ] Q75 `ProduceHighTemplar.java` [PROTOSS] (P4-demand?)
- [x] Q76 `ProduceObserver.java` [PROTOSS] (P4-demand?) — ported 2026-10-28 as the V2 observer pump (flag 3-or-1-per-40, lurkers 4, losses replaced; routine clock-eyes unported; HiddenUnitDetectionGoalsTest +1).
- [ ] Q77 `ProduceReavers.java` [PROTOSS] (P4-demand?)
- [ ] Q78 `ProduceScarabs.java` [PROTOSS] (P4-demand?)
- [ ] Q79 `ProduceShuttle.java` [PROTOSS] (P4-demand?)
- [ ] Q80 `ProduceZealot.java` [PROTOSS] (P4-demand?)
- [ ] Q81 `ProtossBuildingToProduce.java` [PROTOSS] (P4-demand?)
- [ ] Q82 `TrainQueuedDragoonImmediately.java` [PROTOSS] (P4-demand?)
- [ ] Q83 `ZealotInsteadDragoon.java` [PROTOSS] (P4-demand?)

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
- [ ] Q146 `AutoProduceWorkersCommander.java` [neutral] (P5?)

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
- [ ] Q155 `ABuildOrder.java` [neutral] (P4-queue-last)
- [ ] Q156 `ABuildOrderFactory.java` [neutral] (P4-queue-last)
- [ ] Q157 `ABuildOrderLoader.java` [neutral] (P4-queue-last)
- [ ] Q158 `BuildOrderFileNotFoundException.java` [neutral] (P4-queue-last)
- [ ] Q159 `BuildOrderNotationConverter.java` [neutral] (P4-queue-last)
- [ ] Q160 `BuildOrderOwnsGame.java` [neutral] (P4-queue-last)
- [ ] Q161 `BuildOrderRowParser.java` [neutral] (P4-queue-last)
- [ ] Q162 `BuildOrderSetting.java` [neutral] (P4-queue-last)
- [ ] Q163 `BuildOrderSettings.java` [neutral] (P4-queue-last)
- [ ] Q164 `CurrentBuildOrder.java` [neutral] (P4-queue-last)
- [ ] Q165 `ProtossBuildOrder.java` [neutral] (P4-queue-last)
- [ ] Q166 `TerranBuildOrder.java` [neutral] (P4-queue-last)
- [ ] Q167 `ZergBuildOrder.java` [neutral] (P4-queue-last)

## orders/production/queue
- [ ] Q168 `AbstractQueue.java` [neutral] (P4-queue-last)
- [ ] Q169 `ClearCountCache.java` [neutral] (P4-queue-last)
- [ ] Q170 `CountInQueue.java` [neutral] (P4-queue-last)
- [ ] Q171 `CurrentQueue.java` [neutral] (P4-queue-last)
- [ ] Q172 `PriorityWaiters.java` [neutral] (P4-queue-last)
- [ ] Q173 `Queue.java` [neutral] (P4-queue-last)
- [ ] Q174 `QueueFactory.java` [neutral] (P4-queue-last)
- [ ] Q175 `QueueInitializer.java` [neutral] (P4-queue-last)
- [ ] Q176 `QueueLastStatus.java` [neutral] (P4-queue-last)
- [ ] Q177 `RemoveFromQueue.java` [neutral] (P4-queue-last)
- [ ] Q178 `ReservedResources.java` [neutral] (P4-queue-last)
- [ ] Q179 `SoonInQueue.java` [neutral] (P4-queue-last)

## orders/production/queue/add
- [ ] Q180 `AddToQueue.java` [neutral] (P4-queue-last)
- [ ] Q181 `AddToQueueToHave.java` [neutral] (P4-queue-last)
- [ ] Q182 `History.java` [neutral] (P4-queue-last)
- [ ] Q183 `IndexForNewOrder.java` [neutral] (P4-queue-last)
- [ ] Q184 `OnlyOneAllowedOfType.java` [neutral] (P4-queue-last)
- [ ] Q185 `PreventDuplicateOrders.java` [neutral] (P4-queue-last)
- [ ] Q186 `TrimQueue.java` [neutral] (P4-queue-last)

## orders/production/queue/events
- [ ] Q187 `OrderStatusWasChanged.java` [neutral] (P4-queue-last)

## orders/production/queue/order
- [ ] Q188 `ForcedDirectProductionOrder.java` [neutral] (P4-queue-last)
- [ ] Q189 `OrderReservations.java` [neutral] (P4-queue-last)
- [ ] Q190 `OrderStatus.java` [neutral] (P4-queue-last)
- [ ] Q191 `Orders.java` [neutral] (P4-queue-last)
- [ ] Q192 `OrdersFilters.java` [neutral] (P4-queue-last)
- [ ] Q193 `ProductionOrder.java` [neutral] (P4-queue-last)
- [ ] Q194 `ProductionOrderHandler.java` [neutral] (P4-queue-last)
- [ ] Q195 `ProductionOrderPriority.java` [neutral] (P4-queue-last)

## orders/production/queue/updater
- [ ] Q196 `IsOrderCompleted.java` [neutral] (P4-queue-last)
- [ ] Q197 `IsOrderInProgress.java` [neutral] (P4-queue-last)
- [ ] Q198 `QueueRefresher.java` [neutral] (P4-queue-last)
- [ ] Q199 `ReadyToProduceOrder.java` [neutral] (P4-queue-last)
- [ ] Q200 `ThisManyUnitsByDefault.java` [neutral] (P4-queue-last)

## orders/requirements
- [ ] Q201 `AllowToProduceEarlyWithoutRequirements.java` [neutral] (P4?)
- [ ] Q202 `Requirements.java` [neutral] (P4?)

## orders/zerg
- [~] Q203 `ProduceZergUnit.java` [ZERG] (P4?)

## requests
- [ ] Q204 `ADetectorRequest.java` [neutral] (P4?)
- [ ] Q205 `AntiAirBuildingCommander.java` [neutral] (P4?)
- [ ] Q206 `AntiLandBuildingCommander.java` [neutral] (P4?)
- [ ] Q207 `DynamicBuildingCommander.java` [neutral] (P4?)
- [ ] Q208 `ProductionRequests.java` [neutral] (P4?)
- [ ] Q209 `ProtossRequests.java` [neutral] (P4?)
- [ ] Q210 `RemoveExcessiveOrders.java` [neutral] (P4?)
- [ ] Q211 `TerranRequests.java` [neutral] (P4?)

## requests/produce
- [ ] Q212 `BuildGate.java` [neutral] (P4-produce-pipe)
- [ ] Q213 `BuildGates.java` [neutral] (P4-produce-pipe)
- [ ] Q214 `ProduceAddon.java` [neutral] (P4-produce-pipe)
- [ ] Q215 `ProduceBuilding.java` [neutral] (P4-produce-pipe)
- [ ] Q216 `ProduceOrdersFromQueue.java` [neutral] (P4-produce-pipe)
- [ ] Q217 `ProduceUnit.java` [neutral] (P4-produce-pipe)
- [ ] Q218 `ProduceWorker.java` [neutral] (P4-produce-pipe)

## requests/protoss
- [ ] Q219 `ProtossPhotonCannonAntiAir.java` [PROTOSS] (P4-demand?)
- [ ] Q220 `ProtossPhotonCannonAntiLand.java` [PROTOSS] (P4-demand?)

## requests/zerg
- [~] Q221 `ZergRequests.java` [ZERG] (P4?)
- [~] Q222 `ZergSporeColony.java` [ZERG] (P4?)
- [~] Q223 `ZergSunkenColony.java` [ZERG] (P4?)
