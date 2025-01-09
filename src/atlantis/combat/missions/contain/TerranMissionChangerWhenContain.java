package atlantis.combat.missions.contain;

import atlantis.combat.retreating.RetreatManager;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.generic.ArmyStrength;
import atlantis.information.strategy.GamePhase;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.game.player.Enemy;

public class TerranMissionChangerWhenContain extends MissionChangerWhenContain {

//    public static void changeMissionIfNeeded() {
//        if (shouldChangeMissionToDefend() && !TerranMissionChangerWhenDefend.shouldChangeMissionToContain()) {
//            Missions.forceGlobalMissionDefend(reason);
//        }
//
//        else if (shouldChangeMissionToAttack() && !TerranMissionChangerWhenAttack.shouldChangeMissionToContain()) {
//            Missions.forceGlobalMissionAttack(reason);
//        }
//    }

    // =========================================================

    @Override
    public boolean shouldChangeMissionToDefend() {
        if (GamePhase.isEarlyGame()) {
            if (Enemy.protoss() && EnemyUnits.discovered().ofType(AUnitType.Protoss_Zealot).atLeast(4)) {
                if (Count.medics() <= 4) {
                    if (DEBUG) reason = "Enemy rush (" + ArmyStrength.ourArmyRelativeStrength() + "%)";
                    return true;
                }
            }
        }

        if (ArmyStrength.ourArmyRelativeStrength() < 200) {
            if (DEBUG) reason = "Let us not risk (" + ArmyStrength.ourArmyRelativeStrength() + "%)";
            return true;
        }

        if (EnemyInfo.isEnemyNearAnyOurBase() && A.supplyUsed() <= 70) {
            if (DEBUG) reason = "Enemy near our building";
            return true;
        }

        if (ArmyStrength.weAreWeaker() && RetreatManager.GLOBAL_RETREAT_COUNTER >= 2 && A.resourcesBalance() <= 300) {
            if (DEBUG) reason = "We are weaker (" + ArmyStrength.ourArmyRelativeStrength() + "%)";
            return true;
        }

        if (A.resourcesBalance() <= -400 && A.supplyUsed() <= 130 && !GamePhase.isLateGame()) {
            if (DEBUG) reason = "Too many resources lost";
            return true;
        }

//        if (EnemyInfo.hiddenUnitsCount() >= 2 && Count.ofType(AUnitType.Terran_Science_Vessel) == 0) {
//            if (DEBUG) reason = "Hidden units";
//            return true;
//        }

        return false;
    }

    @Override
    public boolean shouldChangeMissionToAttack() {
        if (A.supplyUsed() >= 190) {
            if (DEBUG) reason = "Supply blocked";
            return true;
        }

        if (A.supplyUsed() >= 80 && ArmyStrength.ourArmyRelativeStrength() >= 300) {
            if (DEBUG) reason = "Resources balance good";
            return true;
        }

        return false;
    }

    protected static boolean killsBalanceSaysSo() {
        if (AGame.timeSeconds() <= 400 && AGame.killsLossesResourceBalance() >= 900) return true;

        return AGame.timeSeconds() <= 700 && AGame.killsLossesResourceBalance() >= 1600;
    }

}
