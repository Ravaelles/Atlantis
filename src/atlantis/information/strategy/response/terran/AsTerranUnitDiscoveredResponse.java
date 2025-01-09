package atlantis.information.strategy.response.terran;

import atlantis.combat.missions.MissionChanger;

import atlantis.information.enemy.EnemyFlags;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.generic.ArmyStrength;
import atlantis.information.strategy.Strategy;
import atlantis.information.strategy.response.enemy_base.WhenEnemyBaseDiscovered;
import atlantis.information.strategy.response.enemy_cb.WhenCBDiscovered;
import atlantis.information.strategy.response.enemy_hidden.EnemyHiddenUnits;
import atlantis.map.choke.Chokes;
import atlantis.production.constructions.position.modifier.PositionModifier;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.production.requests.ProductionRequests;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.util.We;

public class AsTerranUnitDiscoveredResponse {
    public static boolean updateEnemyUnitDiscovered(AUnit enemyUnit) {
        if (enemyUnit.isBase()) WhenEnemyBaseDiscovered.whenBaseDiscovered(enemyUnit);
        if (enemyUnit.isCombatBuilding()) WhenCBDiscovered.whenCBDiscovered(enemyUnit);

        if (Strategy.get().isRushOrCheese()) {
            whenRushOrCheese(enemyUnit);
        }

        return EnemyHiddenUnits.handleHiddenUnitDetected(enemyUnit)
            || EnemyHiddenUnits.handleBuildingLeadingToHiddenUnits(enemyUnit);

//        handleHiddenUnitDetected(enemyUnit)
//            || handleBuildingLeadingToHiddenUnits(enemyUnit);
//
//        // HIDDEN units and buildings to produce it
////        handleHiddenUnitDetected(enemyUnit);
//
//        // COMBAT buildings
//        if (enemyUnit.type().isCombatBuildingOrCreepColony()) {
//            if (GamePhase.isEarlyGame()) {
//                EnemyInfo.startedWithCombatBuilding = true;
//            }
////            if (Missions.isFirstMission()) {
////                MissionChanger.forceMissionContain();
//////                CurrentBuildOrder.set(TerranStrategies.TERRAN_Mech.buildOrder());
////            }
//        }
//
//        if (GamePhase.isEarlyGame() && !Strategy.get().isRushOrCheese()) {
//            if (enemyUnit.isZergling()) {
//                if (EnemyUnits.count(AUnitType.Zerg_Zergling) >= 6) {
//                    MissionChanger.forceMissionSpartaOrDefend("LotsOfZerglingsT");
//                }
//            }
//
//            else if (enemyUnit.isZealot()) {
//                if (Count.ourCombatUnits() <= 4) {
//                    MissionChanger.forceMissionSpartaOrDefend("CautionWithZealot");
//                }
//            }
//
//            if (EnemyUnits.discovered().buildings().atMost(3)) OurClosestBaseToEnemy.clearCache();
//        }
    }

    // =========================================================

    private static void whenRushOrCheese(AUnit enemyUnit) {
        if (asTerranDontPushWhenEnemyHasDragoons()) return;
        if (asTerranDontHaveBunkersAndEnemyIsStrong()) return;
    }

    private static boolean asTerranDontPushWhenEnemyHasDragoons() {
        if (!We.terran()) return false;

        if (
            EnemyUnits.has(AUnitType.Protoss_Dragoon)
                && (ArmyStrength.ourArmyRelativeStrength() <= 130 || Count.medics() <= 1)
        ) {
            MissionChanger.forceMissionSpartaOrDefend("Enemy dragoons & not enough power");
            return true;
        }

        return false;
    }

    private static boolean asTerranDontHaveBunkersAndEnemyIsStrong() {
        if (
            We.terran()
                && ArmyStrength.ourArmyRelativeStrength() <= 97
                && Count.clearCache()
                && Count.bunkersWithUnfinished() == 0
                && Count.withPlanned(AUnitType.Terran_Bunker) == 0
        ) {
            ProductionOrder order = AddToQueue.withTopPriority(AUnitType.Terran_Bunker);
            if (order != null) {
                order.setMinSupply(1);
                order.setModifier(PositionModifier.MAIN_CHOKE);

//                System.out.println("Count.bunkersWithUnfinished() = " + Count.bunkersWithUnfinished());
//                System.out.println("Count.withPlanned(AUnitType.Terran_Bunker) = " + Count.withPlanned(AUnitType.Terran_Bunker));
                return true;
            }

//            AddToQueue.withTopPriority(AUnitType.Terran_Bunker, Select.mainOrAnyBuilding());

//            if (ArmyStrength.ourArmyRelativeStrength() <= 60) {
//                AddToQueue.withHighPriority(AUnitType.Terran_Bunker, Chokes.mainChoke());
//            }
        }
        return false;
    }

//    // =========================================================
//
//    private static void handleHiddenUnitDetected(AUnit enemyUnit) {
//        if (!enemyUnit.isLurker() && !enemyUnit.isDT()) return;
//
//        EnemyFlags.HAS_HIDDEN_COMBAT_UNIT = true;
//
//        if (enemyUnit.is(AUnitType.Protoss_Dark_Templar, AUnitType.Zerg_Lurker)) {
//            ProductionRequests.getInstance().requestDetectorQuick(Chokes.mainChokeCenter());
//        }
//    }
}
