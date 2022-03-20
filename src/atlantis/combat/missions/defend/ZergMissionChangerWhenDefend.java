package atlantis.combat.missions.defend;

import atlantis.Atlantis;
import atlantis.combat.missions.MissionChanger;
import atlantis.combat.missions.Missions;
import atlantis.combat.missions.attack.ProtossMissionChangerWhenAttack;
import atlantis.combat.missions.contain.MissionChangerWhenContain;
import atlantis.combat.missions.contain.ProtossMissionChangerWhenContain;
import atlantis.game.A;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.generic.ArmyStrength;
import atlantis.information.strategy.EnemyStrategy;
import atlantis.information.strategy.GamePhase;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Select;

public class ZergMissionChangerWhenDefend extends MissionChangerWhenContain {

    public static void changeMissionIfNeeded() {
        if (!canChange()) {
            return;
        }

        if (shouldChangeMissionToAttack() && !ProtossMissionChangerWhenAttack.shouldChangeMissionToDefend()) {
            MissionChanger.changeMissionTo(Missions.ATTACK);
        }
        else if (shouldChangeMissionToContain() && !ProtossMissionChangerWhenContain.shouldChangeMissionToDefend()) {
            changeMissionTo(Missions.CONTAIN);
        }
    }

    // === CONTAIN =============================================

    private static boolean canChange() {
        if (EnemyInfo.isEnemyNearAnyOurBase()) {
            return false;
        }

        if (GamePhase.isEarlyGame()) {
            if (
                EnemyStrategy.get().isRushOrCheese()
                    && (A.resourcesBalance() < 350 || !ArmyStrength.weAreMuchStronger())
            ) {
                return false;
            }

            if (Count.cannons() >= 1 && Count.ourCombatUnits() <= 8) {
                return false;
            }

            if (EnemyUnits.discovered().ofType(AUnitType.Protoss_Zealot).atLeast(4)) {
                return false;
            }
        }

        return true;
    }

    private static boolean shouldChangeMissionToAttack() {
        if (A.supplyUsed() >= 195) {
            if (DEBUG) reason = "Maxed out";
            return true;
        }

        if (Atlantis.LOST >= 5 && A.supplyUsed() <= 50) {
            return false;
        }

        if (ArmyStrength.ourArmyRelativeStrength() >= 300) {
            if (DEBUG) reason = "So much stronger (" + ArmyStrength.ourArmyRelativeStrength() + "%)";
            return true;
        }

        return false;
    }

    private static boolean shouldChangeMissionToContain() {
        if (ArmyStrength.ourArmyRelativeStrength() >= 170) {
            if (DEBUG) reason = "We are stronger (" + ArmyStrength.ourArmyRelativeStrength() + "%)";
            return true;
        }

        if ((GamePhase.isEarlyGame() && A.resourcesBalance() >= 300)) {
            if (DEBUG) reason = "resources balance is good";
            return true;
        }

        if (A.supplyUsed(90)) {
            if (DEBUG) reason = "Supply quite big";
            return true;
        }

        return false;
    }

}
