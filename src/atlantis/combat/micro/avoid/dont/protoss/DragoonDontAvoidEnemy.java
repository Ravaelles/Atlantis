package atlantis.combat.micro.avoid.dont.protoss;

import atlantis.combat.retreating.protoss.ProtossTooBigBattleToRetreat;
import atlantis.units.AUnit;
import atlantis.units.select.Selection;
import atlantis.util.Enemy;

public class DragoonDontAvoidEnemy {
    public static boolean dontAvoid(AUnit unit) {
        if (!unit.isDragoon()) return false;
//        if (true) return false;

        if (dontAvoidCombatBuildings(unit)) return true;
        if (Enemy.protoss()) return vsProtoss(unit);
        if (Enemy.terran()) return vsTerran(unit);
        if (Enemy.zerg()) return vsZerg(unit);

        return false;
    }

    private static boolean dontAvoidCombatBuildings(AUnit unit) {
        if (unit.hp() <= 30) return false;

        Selection combatBuildings = unit.enemiesNear().combatBuildingsAntiLand().inRadius(9, unit);
        if (combatBuildings.empty()) return false;

        return unit.enemiesNear().combatUnits().nonBuildings().inRadius(1.8, unit).empty()
            && unit.addLog("GoonFightCB");
    }

    private static boolean vsProtoss(AUnit unit) {
        if (unit.isHealthy()) return true;
        if (ProtossTooBigBattleToRetreat.PvP_doNotRetreat(unit)) return true;

        int meleeEnemiesNearCount = unit.meleeEnemiesNearCount(1.5);
        if (meleeEnemiesNearCount > 0) {
            return meleeNearButQuiteHealthy(unit);
        }

        return unit.woundHp() <= 30
            && (unit.lastAttackFrameAgo() > 30 * 2 || unit.combatEvalRelative() > 1.3)
            && (unit.isHealthy() || meleeEnemiesNearCount == 0);
//            && A.println("Don't avoid " + unit.typeWithUnitId());
    }

    private static boolean vsTerran(AUnit unit) {
        if (unit.isHealthy()) return true;
        if (ProtossTooBigBattleToRetreat.PvP_doNotRetreat(unit)) return true;

        int meleeEnemiesNearCount = unit.meleeEnemiesNearCount(1.5);
        if (meleeEnemiesNearCount > 0) {
            return meleeNearButQuiteHealthy(unit);
        }

        return false;
    }

    private static boolean vsZerg(AUnit unit) {
        if (unit.isHealthy()) return true;
//        if (ProtossTooBigBattleToRetreat.PvP_doNotRetreat(unit)) return true;

        if (unit.cooldown() <= 7) {
            int meleeEnemiesNearCount = unit.meleeEnemiesNearCount(1.5);
            if (meleeEnemiesNearCount > 0) {
                return meleeNearButQuiteHealthy(unit);
            }
        }

        return unit.lastAttackFrameAgo() > 30 * 3;
    }

    private static boolean meleeNearButQuiteHealthy(AUnit unit) {
        if (
            unit.cooldown() <= 10
                && unit.lastAttackFrameMoreThanAgo(30 * (unit.shields() >= 16 ? 2 : 5))
        ) return true;

        return (unit.woundHp() <= 9 || unit.cooldown() <= 7) && unit.shields() >= 6;
    }
}
