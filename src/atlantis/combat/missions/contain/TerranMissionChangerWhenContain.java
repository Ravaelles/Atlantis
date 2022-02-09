package atlantis.combat.missions.contain;

import atlantis.Atlantis;
import atlantis.combat.missions.MissionChanger;
import atlantis.combat.missions.Missions;
import atlantis.combat.missions.attack.TerranMissionChangerWhenAttack;
import atlantis.combat.missions.defend.TerranMissionChangerWhenDefend;
import atlantis.combat.retreating.RetreatManager;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.generic.ArmyStrength;
import atlantis.information.strategy.GamePhase;
import atlantis.information.strategy.OurStrategy;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.util.Enemy;

public class TerranMissionChangerWhenContain extends MissionChanger {

    public static void changeMissionIfNeeded() {
        if (shouldChangeMissionToDefend() && !TerranMissionChangerWhenDefend.shouldChangeMissionToContain()) {
            Missions.setGlobalMissionDefend();
        }

        else if (shouldChangeMissionToAttack() && !TerranMissionChangerWhenAttack.shouldChangeMissionToContain()) {
            Missions.setGlobalMissionAttack();
        }
    }

    // =========================================================

    public static boolean shouldChangeMissionToDefend() {
        if (GamePhase.isEarlyGame()) {
            if (Enemy.protoss() && EnemyUnits.visibleAndFogged().ofType(AUnitType.Protoss_Zealot).atLeast(4)) {
                if (Count.medics() <= 4) {
                    if (DEBUG) debugReason = "Enemy rush (" + ArmyStrength.ourArmyRelativeStrength() + "%)";
                    return true;
                }
            }
        }

        if (ArmyStrength.weAreMuchWeaker()) {
            if (DEBUG) debugReason = "Much weaker (" + ArmyStrength.ourArmyRelativeStrength() + "%)";
            return true;
        }

        if (EnemyInfo.isEnemyNearAnyOurBuilding() && A.supplyUsed() <= 70) {
            if (DEBUG) debugReason = "Enemy near our building";
            return true;
        }

        if (ArmyStrength.weAreWeaker() && RetreatManager.GLOBAL_RETREAT_COUNTER >= 2 && A.resourcesBalance() <= 300) {
            if (DEBUG) debugReason = "We are weaker (" + ArmyStrength.ourArmyRelativeStrength() + "%)";
            return true;
        }

        if (A.resourcesBalance() <= -400 && A.supplyUsed() <= 130) {
            if (DEBUG) debugReason = "Too many resources lost";
            return true;
        }

        return false;
    }

    protected static boolean shouldChangeMissionToAttack() {
        if (A.supplyUsed() >= 180) {
            if (DEBUG) debugReason = "Supply blocked";
            return true;
        }

        if (ArmyStrength.weAreMuchStronger()) {
            if (DEBUG) debugReason = "Much stronger";
            return true;
        }

        if (A.resourcesBalance() >= 410 && ArmyStrength.weAreStronger()) {
            if (DEBUG) debugReason = "Resources balance good";
            return true;
        }

        return false;
    }

    protected static boolean killsBalanceSaysSo() {
        if (AGame.timeSeconds() <= 400 && AGame.killsLossesResourceBalance() >= 900) {
            return true;
        }

        return AGame.timeSeconds() <= 700 && AGame.killsLossesResourceBalance() >= 1600;
    }

}
