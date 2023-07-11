package atlantis.information.strategy;

import atlantis.combat.missions.MissionChanger;
import atlantis.game.A;
import atlantis.information.enemy.EnemyFlags;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.decisions.OurStrategicBuildings;
import atlantis.information.enemy.EnemyUnits;
import atlantis.map.Chokes;
import atlantis.production.requests.ProductionRequests;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;

public class EnemyUnitDiscoveredResponse {

    public static void updateEnemyUnitDiscovered(AUnit enemyUnit) {
        if (A.isUms()) {
            return;
        }

        if (OurStrategy.get().isRushOrCheese() && GamePhase.isEarlyGame()) {
            return;
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
        }
    }

    // =========================================================

    private static void handleHiddenUnitDetected(AUnit enemyUnit) {
        if (!enemyUnit.isCloaked()
                && !enemyUnit.isLurker()
                && !enemyUnit.isDT() && !enemyUnit.is(AUnitType.Protoss_Templar_Archives)
        ) {
            return;
        }

        EnemyFlags.HAS_HIDDEN_COMBAT_UNIT = true;

//        if (enemyUnit.effVisible()) {
//            return;
//        }

//        if (enemyUnit.is(AUnitType.Protoss_Dark_Templar)) {
        if (enemyUnit.is(AUnitType.Protoss_Dark_Templar, AUnitType.Zerg_Lurker)) {
            OurStrategicBuildings.setDetectorsNeeded(2);

            ProductionRequests.getInstance().requestDetectorQuick(Chokes.mainChokeCenter());

//            ProductionRequests.getInstance().requestDetectorQuick(
//                    Chokes.natural(Select.mainBase().position()).getCenter()
//            );
        }
    }

}
