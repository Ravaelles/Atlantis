package atlantis.combat.missions.defend;

import atlantis.combat.missions.MissionChanger;
import atlantis.combat.missions.Missions;
import atlantis.combat.missions.contain.TerranMissionChangerWhenContain;
import atlantis.game.A;
import atlantis.information.decisions.Decisions;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.generic.ArmyStrength;
import atlantis.information.strategy.GamePhase;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.util.Enemy;
import atlantis.util.We;

public class TerranMissionChangerWhenDefend extends MissionChanger {

    public static void changeMissionIfNeeded() {
        if (shouldChangeMissionToContain() && !TerranMissionChangerWhenContain.shouldChangeMissionToDefend()) {
            changeMissionTo(Missions.CONTAIN);
        }
    }

    // === CONTAIN =============================================

    public static boolean shouldChangeMissionToContain() {
        if (ArmyStrength.weAreMuchWeaker()) {
            if (DEBUG) reason = "We are much weaker (" + ArmyStrength.ourArmyRelativeStrength() + "%)";
            return false;
        }

        if (We.terran() && Enemy.protoss()) {
            if (Missions.counter() >= 2 && A.supplyUsed() <= 90 && A.seconds() <= 60 * 7) {
                return false;
            }
        }

        if (Enemy.zerg()) {
            if (Count.ourCombatUnits() <= 6) {
                return false;
            }
        }

        if (EnemyInfo.hiddenUnitsCount() >= 2 && Count.ofType(AUnitType.Terran_Science_Vessel) == 0) {
            return false;
        }

        // === Might be TRUE ===========================================

        if (Enemy.protoss()) {
            if (GamePhase.isEarlyGame() && EnemyUnits.discovered().combatUnits().count() >= 6) {
                return Count.ourCombatUnits() >= 13;
            }
        }

        if (Count.bunkers() >= 1) {
            if (Decisions.weHaveBunkerAndDontHaveToDefendAnyLonger()) {
                if (DEBUG) reason = "No longer have to defend (" + ArmyStrength.ourArmyRelativeStrength() + "%)";
                return true;
            }
        }

        if (ArmyStrength.weAreStronger()) {
            if (DEBUG) reason = "We are stronger (" + ArmyStrength.ourArmyRelativeStrength() + "%)";
            return true;
        }

        if (A.resourcesBalance() >= 250) {
            if (DEBUG) reason = "resources balance is good";
            return true;
        }

        return false;

//        if (EnemyInfo.isEnemyNearAnyOurBuilding()) {
//            return false;
//        }
//
////        if (TerranMissionChangerWhenContain.shouldChangeMissionToDefend()) {
////            return false;
////        }
//        // === Protoss ===================================================
//
//        if (Enemy.protoss()) {
////            if (Missions.isFirstMission()) {
//            if (Atlantis.LOST <= 3 && Count.ourCombatUnits() >= 4) {
//                return true;
//            }
//
//            return Count.ourCombatUnits() >= 15 || Count.tanks() >= 2;
//        }
//
//        // === Zerg ======================================================
//
//        else if (Enemy.zerg()) {
//            if (Atlantis.LOST <= 2) {
//                return true;
//            }
//
////            return A.supplyUsed(140);
//
//            if (A.resourcesBalance() >= 250) {
//                return true;
//            }
//
//            if (Missions.counter() <= 3) {
//                if (OurStrategy.get().isRush()) {
//                    return Count.ourCombatUnits() >= 2;
//                }
//            }
////
////            return Count.ourCombatUnits() >= Math.max(24, 12 + Missions.counter());
//            return Count.ourCombatUnits() >= 13 || Count.tanks() >= 3;
    }

}
