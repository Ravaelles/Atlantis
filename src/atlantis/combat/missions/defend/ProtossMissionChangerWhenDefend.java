package atlantis.combat.missions.defend;

import atlantis.combat.missions.Missions;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.generic.ArmyStrength;
import atlantis.information.strategy.EnemyStrategy;
import atlantis.information.strategy.GamePhase;
import atlantis.information.strategy.OurStrategy;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
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

        if (A.seconds() <= 360) {
            if (AGame.killsLossesResourceBalance() < 0) return false;
            else {
                if (Enemy.terran() && ArmyStrength.ourArmyRelativeStrength() >= 110) {
                    reason = "Early game pressure (" + ArmyStrength.ourArmyRelativeStrength() + "%)";
                    return true;
                }
                else {
                    return false;
                }
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
        if (!canChange()) return false;

        if (ArmyStrength.ourArmyRelativeStrength() >= 200 && Count.dragoons() >= 4) {
            if (DEBUG) reason = "So much stronger (" + ArmyStrength.ourArmyRelativeStrength() + "%)";
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
