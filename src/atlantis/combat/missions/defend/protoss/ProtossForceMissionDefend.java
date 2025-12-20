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
        int ourCombatUnits = Count.ourCombatUnits();
        double alphaEval = Alpha.evalOr(9.99);

        if (Enemy.protoss()) {
            if (A.s >= 9 * 60 && (Army.strengthWithoutCB() >= 120 || ourCombatUnits >= 10)) return false;

            if (ourCombatUnits <= 19 && (strength <= 165 || alphaEval <= 1.16)) return false;

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
            else {
                if (strength <= 200 && A.supplyUsed() <= 190 && !A.hasMinerals(1500)) return t("WeakVsPBig");

                return false;
            }
        }

        if (Enemy.zerg()) {
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

        if (Enemy.terran()) {
            if (Count.dragoons() >= EnemyUnits.tanks() * 3) return false;

            if (
                strength <= 150
                    && Count.darkTemplars() == 0
                    && Count.dragoons() * 2.9 < EnemyUnits.tanks()
            ) return t("WeakVsTerran");

            return false;
        }

        return false;
    }

    private static boolean t(String reason) {
        MissionChanger.reason = reason;
        return true;
    }
}
