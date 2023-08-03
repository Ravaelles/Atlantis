package atlantis.combat.missions.defend;

import atlantis.combat.micro.terran.tank.TankDecisions;
import atlantis.combat.missions.Missions;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.information.decisions.Decisions;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.generic.ArmyStrength;
import atlantis.information.strategy.EnemyStrategy;
import atlantis.information.strategy.GamePhase;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.util.Enemy;
import atlantis.util.We;

public class TerranMissionChangerWhenDefend extends MissionChangerWhenDefend {

    // === ATTACK ==============================================

    public boolean shouldChangeMissionToAttack() {
//        if (true) return false;

//        if (!shouldChangeMissionToAttack()) {
//            return false;
//        }

        if (
            A.supplyUsed() <= 90
                && AGame.killsLossesResourceBalance() <= 600
        ) return false;

        int ourRelativeStrength = ArmyStrength.ourArmyRelativeStrength();

        if (
            ourRelativeStrength <= 600
                && EnemyUnits.discovered().combatBuildingsAntiLand().atLeast(2)
                && Count.tanks() <= 1
                && !TankDecisions.siegeResearched()
        ) {
            return false;
        }

        if (ourRelativeStrength >= 350 && !EnemyInfo.isEnemyNearAnyOurBase()) {
            if (DEBUG) reason = "Comfortably stronger (" + ourRelativeStrength + "%)";
            return true;
        }

        return false;
    }

    // === CONTAIN =============================================

    public boolean shouldChangeMissionToContain() {
        if (true) return false;

        int ourRelativeStrength = ArmyStrength.ourArmyRelativeStrength();

        if (A.supplyUsed() >= 130) {
            if (DEBUG) reason = "Supply allows it (" + ourRelativeStrength + "%)";
            return true;
        }

        if (ourRelativeStrength >= 400) {
            if (DEBUG) reason = "Much stronger (" + ourRelativeStrength + "%)";
            return true;
        }

        if (ourRelativeStrength < 160) {
            if (DEBUG) reason = "We are not much stronger (" + ourRelativeStrength + "%)";
            return false;
        }

        if (ourRelativeStrength < 300 && GamePhase.isEarlyGame() && A.resourcesBalance() <= -150) {
            return false;
        }

        if (ourRelativeStrength < 300 && EnemyStrategy.get().isRushOrCheese() && A.supplyUsed() <= 110) {
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

        if (Count.tanks() >= 2 && !TankDecisions.siegeResearched()) {
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
                if (DEBUG) reason = "No longer have to defend (" + ourRelativeStrength + "%)";
                return true;
            }
        }

        if (ArmyStrength.weAreStronger()) {
            if (DEBUG) reason = "We are stronger (" + ourRelativeStrength + "%)";
            return true;
        }

        if (A.resourcesBalance() >= 250) {
            if (DEBUG) reason = "resources balance is good";
            return true;
        }

        return false;
    }
}
