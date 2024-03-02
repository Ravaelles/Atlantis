package atlantis.combat.missions.defend;

import atlantis.combat.missions.Missions;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.enemy.EnemyWhoBreachedBase;
import atlantis.information.generic.ArmyStrength;
import atlantis.information.strategy.EnemyStrategy;
import atlantis.information.strategy.GamePhase;
import atlantis.information.strategy.OurStrategy;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.util.Enemy;

public class ProtossMissionChangerWhenDefend extends MissionChangerWhenDefend {

    // === CONTAIN =============================================

//    private boolean changeFromSpartaToDefend() {
//        if (Missions.isGlobalMissionSparta() && Count.basesWithUnfinished() >= 2) return true;
//
//        return false;
//    }

    public boolean canChange() {
        if (EnemyInfo.isEnemyNearAnyOurBase()) return false;

        if (A.seconds() <= 200) {
            if (!OurStrategy.get().isRushOrCheese()) return false;
        }

        int relativeStrength = ArmyStrength.ourArmyRelativeStrength();

        if (A.seconds() <= 360) {
            if (AGame.killsLossesResourceBalance() < 0) return false;
            else {
                if (Enemy.terran() && relativeStrength >= 110) {
                    reason = "Early game pressure (" + relativeStrength + "%)";
                    return true;
                }

                if (Enemy.protoss() && relativeStrength >= 200) {
                    reason = "Early game push (" + relativeStrength + "%)";
                    return true;
                }

//                else {
//                    return false;
//                }
            }
        }

//        if (GamePhase.isEarlyGame() && Count.dragoons() <= 3) {
        if (GamePhase.isEarlyGame()) {
            if (
                EnemyStrategy.get().isRushOrCheese()
                    && (A.resourcesBalance() < 350 || !ArmyStrength.weAreMuchStronger())
            ) return false;

            if (Count.cannons() >= 1 && Count.ourCombatUnits() <= 8) return false;

            if (EnemyUnits.discovered().ofType(AUnitType.Protoss_Zealot).atLeast(4)) return false;
        }

        return true;
    }

    public boolean shouldChangeMissionToAttack() {
        if (beBraveProtoss()) {
            if (DEBUG) reason = "Brave Protoss! (" + ArmyStrength.ourArmyRelativeStrength() + "%)";
        }

        if (!canChange()) return false;

        if (Missions.isGlobalMissionSparta()) {
            return whenSparta();
        }

        if (ArmyStrength.ourArmyRelativeStrength() >= 240 && Count.dragoons() >= 2) {
            if (DEBUG) reason = "Ah, much stronger (" + ArmyStrength.ourArmyRelativeStrength() + "%)";
            return true;
        }

        return false;
    }

    private static boolean beBraveProtoss() {
        return AGame.killsLossesResourceBalance() >= 300
            && Count.ourCombatUnits() >= 3
            && ArmyStrength.ourArmyRelativeStrength() >= 150
            && EnemyWhoBreachedBase.noone()
            && Select.enemyCombatUnits().atMost(3);
    }

    private boolean whenSparta() {
        if (ArmyStrength.ourArmyRelativeStrength() >= 400 && (
            AGame.killsLossesResourceBalance() >= 800 || Count.dragoons() >= 8
        )) {
            if (DEBUG) reason = "Spartans strong! (" + ArmyStrength.ourArmyRelativeStrength() + "%)";
            return true;
        }

        return false;
    }

    public boolean shouldChangeMissionToContain() {
        if (!canChange()) return false;

        if (ArmyStrength.ourArmyRelativeStrength() < 200) return false;

        if (EnemyInfo.isEnemyNearAnyOurBase()) return false;

//        if ((GamePhase.isEarlyGame() && A.resourcesBalance() >= 300)) {
//            if (DEBUG) reason = "resources balance is good";
//            return true;
//        }

//        if (A.supplyUsed(90)) {
//            if (DEBUG) reason = "Supply quite big";
//            return true;
//        }

        if (DEBUG) reason = "Why not";
        return true;
    }

}
