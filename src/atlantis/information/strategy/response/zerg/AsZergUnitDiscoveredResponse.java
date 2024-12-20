package atlantis.information.strategy.response.zerg;

import atlantis.combat.missions.MissionChanger;
import atlantis.information.decisions.OurStrategicBuildings;
import atlantis.information.enemy.EnemyFlags;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.generic.ArmyStrength;
import atlantis.information.strategy.GamePhase;
import atlantis.information.strategy.OurStrategy;
import atlantis.map.choke.Chokes;
import atlantis.map.path.OurClosestBaseToEnemy;
import atlantis.production.constructing.position.modifier.PositionModifier;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.production.requests.ProductionRequests;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.util.We;

public class AsZergUnitDiscoveredResponse {
    public static void updateEnemyUnitDiscovered(AUnit enemyUnit) {
        if (OurStrategy.get().isRushOrCheese()) {
            whenRushOrCheese(enemyUnit);
        }

        // HIDDEN units and buildings to produce it
        handleHiddenUnitDetected(enemyUnit);

        // COMBAT buildings
        if (enemyUnit.type().isCombatBuildingOrCreepColony()) {
            if (GamePhase.isEarlyGame()) {
                EnemyInfo.startedWithCombatBuilding = true;
            }
//            if (Missions.isFirstMission()) {
//                MissionChanger.forceMissionContain();
////                CurrentBuildOrder.set(TerranStrategies.TERRAN_Mech.buildOrder());
//            }
        }

        if (GamePhase.isEarlyGame()) {
            if (enemyUnit.isZergling()) {
                if (EnemyUnits.count(AUnitType.Zerg_Zergling) >= 6) {
                    MissionChanger.forceMissionSpartaOrDefend("LotsOfZerglings");
                }
            }

            else if (enemyUnit.isZealot()) {
                if (Count.ourCombatUnits() <= 4) {
                    MissionChanger.forceMissionSpartaOrDefend("CautionWithZealot");
                }
            }

            if (EnemyUnits.discovered().buildings().atMost(3)) OurClosestBaseToEnemy.clearCache();
        }
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
                && (ArmyStrength.ourArmyRelativeStrength() < 120 || Count.medics() <= 1)
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

    // =========================================================

    private static void handleHiddenUnitDetected(AUnit enemyUnit) {
        if (!enemyUnit.isCloaked()
            && !enemyUnit.isLurker()
            && !enemyUnit.isLurkerEgg()
            && !enemyUnit.isDT()
            && !enemyUnit.is(AUnitType.Protoss_Templar_Archives)
        ) {
            return;
        }

        EnemyFlags.HAS_HIDDEN_COMBAT_UNIT = true;

//        if (enemyUnit.effVisible()) {
//            return;
//        }

//        if (enemyUnit.is(AUnitType.Protoss_Dark_Templar)) {
        if (enemyUnit.is(AUnitType.Protoss_Dark_Templar, AUnitType.Zerg_Lurker, AUnitType.Zerg_Lurker_Egg)) {
            OurStrategicBuildings.setDetectorsNeeded(2);

            ProductionRequests.getInstance().requestDetectorQuick(Chokes.mainChokeCenter());

//            ProductionRequests.getInstance().requestDetectorQuick(
//                    Chokes.natural(Select.mainBase().position()).getCenter()
//            );
        }
    }
}
