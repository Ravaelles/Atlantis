package atlantis.combat.missions.defend.protoss;

import atlantis.combat.missions.MissionChanger;
import atlantis.combat.squad.squads.alpha.Alpha;
import atlantis.game.A;
import atlantis.game.player.Enemy;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.generic.Army;
import atlantis.units.AUnit;
import atlantis.units.select.Count;

public class ProtossForceMissionDefend {
    public static boolean check(int strength, int combatUnits) {
        if (Enemy.protoss()) return vsProtoss(strength, combatUnits);
        if (Enemy.zerg()) return vsZerg(strength, combatUnits);
        if (Enemy.terran()) return vsTerran(strength);

        return false;
    }

    private static boolean vsTerran(int strength) {
        if (Count.dragoons() >= EnemyUnits.tanks() * 3) return false;

        if (
            strength <= 150
                && Count.darkTemplars() == 0
                && Count.dragoons() * 2.9 < EnemyUnits.tanks()
        ) return t("WeakVsTerran");

        return false;
    }

    private static boolean vsProtoss(int strength, int combatUnits) {
        int ourCombatUnits = Count.ourCombatUnits();
        double alphaEval = Alpha.evalOr(9.5);

        if (A.s >= 9 * 60 && (strength >= 120 || ourCombatUnits >= 10)) return false;

        if (
            EnemyInfo.hasRanged()
                && ourCombatUnits <= 19
                && strength <= 260
                && (strength <= 165 || alphaEval <= 1.16)
        ) return t("WeakEarlyGame(" + strength + "%," + A.digit(alphaEval) + ")");

        if (combatUnits < 20) {
            if (EnemyInfo.hasRanged()) {
                if (
                    strength <= 300
                        && Count.dragoons() <= 30
                        && Alpha.evalOr(3) <= 1.5
                        && Alpha.groundDistToMain() >= 60
                ) return t("WeakVsPRanged");

                if (
                    Count.dragoons() <= 25
                        && Alpha.evalOr(3) <= 1.5
                ) return t("WeakPCohesion");
            }
            else {
                if (Count.dragoons() > 0) return false;

                if (strength <= 170) return t("WeakVsPMelee");
            }
        }

        if (combatUnits >= 20) {
            if (strength <= 200 && A.supplyUsed() <= 190 && !A.hasMinerals(1500)) return t("WeakVsPBig");

            return false;
        }

        return false;
    }

    private static boolean vsZerg(int strength, int combatUnits) {
        if (combatUnits <= 26 && Army.strengthWithoutOurCB() <= 300 && Count.zealots() <= 3 && Alpha.evalOr(0) <= 5) {
            return t("TooFewZealots");
        }

        if (combatUnits < 20) {
            if (EnemyInfo.hasRanged()) {
                if (strength <= 170 && A.s <= 60 * 12) return t("WeakVsZRanged");
            }
            else {
                if (strength <= 220) return t("WeakVsZMelee");
                if (strength <= 300 && !Alpha.get().isCohesionPercentOkay()) return t("WeakZCohesion");
            }
        }

        return false;
    }

    private static boolean t(String reason) {
//        System.err.println("[ProtossForceMissionDefend] Reason: " + reason);
        MissionChanger.reason = reason;
        return true;
    }
}
