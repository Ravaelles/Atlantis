package atlantis.combat.missions.contain;

import atlantis.Atlantis;
import atlantis.combat.missions.MissionChanger;
import atlantis.combat.missions.Missions;
import atlantis.combat.missions.attack.ZergMissionChangerWhenAttack;
import atlantis.combat.retreating.RetreatManager;
import atlantis.game.A;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.generic.ArmyStrength;
import atlantis.information.strategy.GamePhase;
import atlantis.units.select.Select;

public class ZergMissionChangerWhenContain extends MissionChanger {

    public static void changeMissionIfNeeded() {
        if (shouldChangeMissionToDefend()) {
            MissionChanger.changeMissionTo(MissionChanger.defendOrSpartaMission());
        } else if (shouldChangeMissionToAttack()) {
            MissionChanger.changeMissionTo(Missions.ATTACK);
        }
    }

    // === DEFEND ==============================================

    public static boolean shouldChangeMissionToDefend() {
        if (ArmyStrength.ourArmyRelativeStrength() <= 100) {
            if (RetreatManager.GLOBAL_RETREAT_COUNTER >= 2 && A.resourcesBalance() <= 300) {
                if (DEBUG) reason = "We are weaker (" + ArmyStrength.ourArmyRelativeStrength() + "%)";
                return true;
            }

            if (DEBUG) reason = "Eh, we are weaker (" + ArmyStrength.ourArmyRelativeStrength() + "%)";
            return true;
        }

        if (defendAgainstMassZerglings()) {
            if (DEBUG) reason = "Mass zerglings";
            return true;
        }

        if (EnemyInfo.isEnemyNearAnyOurBase() && A.supplyUsed() <= 70) {
            if (DEBUG) reason = "Enemy near our building";
            return true;
        }

        if (A.resourcesBalance() <= -400 && A.supplyUsed() <= 130 && !GamePhase.isLateGame()) {
            if (DEBUG) reason = "Too many resources lost";
            return true;
        }

        return false;
    }

    // === ATTACK ==============================================

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

//        if (ArmyStrength.weAreMuchStronger() && !EnemyInfo.hasDefensiveLandBuilding(true)) {
//            if (DEBUG) reason = "Much stronger";
//            return true;
//        }

        if (A.resourcesBalance() >= 410 && ArmyStrength.weAreMuchStronger()) {
            if (DEBUG) reason = "Resources balance good";
            return true;
        }

        return false;
    }

}
