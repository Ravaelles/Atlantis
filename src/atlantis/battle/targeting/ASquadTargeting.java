package atlantis.battle.targeting;

import atlantis.units.AUnit;
import atlantis.units.select.Selection;
import atlantis.util.AConsole;
import atlantis.util.We;

public class ASquadTargeting {
    public static AUnit useSquadTargetIfPossible(AUnit unit) {
        if (unit == null || unit.squad() == null) return null;
        if (unit.friendsNear().combatUnits().inRadius(6, unit).atLeast(6)) return null;

        AUnit enemy = unit.squad().targeting().lastTargetIfAlive();

        if (
            isValidTarget(unit, enemy)
                && unit.distTo(enemy) <= 9
                && unit.hasWeaponRangeToAttack(enemy, allowThisManyTilesOutsideRange(unit))
//                && unit.enemiesNear().canBeAttackedBy(unit, 0).empty()
        ) {
//            if (DEBUG) AConsole.println("SqL enemy = " + enemy.typeWithUnitId());

            return enemy;
        }
        
        return null;
    }

    private static int allowThisManyTilesOutsideRange(AUnit unit) {
        return 0;

//        if (We.protoss()) {
//            if (unit.hp() <= 35) {
//                if (Enemy.protoss()) return 0;
//
//                return 0;
//            }
//        }
//
//        return unit.isRanged() ? 3 : 5;
    }

    private static boolean isValidTarget(AUnit unit, AUnit enemy) {
        if (enemy == null) return false;
        if (enemy.isABuilding() && !enemy.isCombatBuilding()) return false;
        if (enemy.isZergling()) return false;
        if (enemy.isWorker()) return false;
        if (enemy.hp() <= 15) return false;
        if (enemy.isDeadMan()) return false;
        if (preventForZealots(unit, enemy)) return false;
        if (!unit.hasWeaponToAttackThisUnit(enemy)) return false;

        return true;
    }

    /**
     * Nearest enemy this unit can damage within maxDist tiles, or null.
     * Engagement, not focus: the focus exclusions above (lings, workers,
     * ranged mirrors, wounded) decide WHICH target to shoot once fighting -
     * this only decides WHETHER the march stops for a fight. Owner
     * 2026-10-19: the cluster walked past enemies to the anchor because
     * nothing ever set a target ({@code setLastTarget} had zero callers),
     * so every unit took the anchor-move branch every frame.
     */
    public static AUnit nearestEngageableEnemy(AUnit unit, double maxDist) {
        if (unit == null) return null;
        Selection enemies = unit.enemiesNear();
        if (enemies == null) return null;
        AUnit best = null;
        double bestDist = maxDist;
        for (AUnit enemy : enemies.list()) {
            if (enemy == null || !enemy.isAlive()) continue;
            if (!unit.hasWeaponToAttackThisUnit(enemy)) continue;
            double dist = unit.distTo(enemy);
            if (dist < bestDist) {
                best = enemy;
                bestDist = dist;
            }
        }
        return best;
    }

    private static boolean preventForZealots(AUnit unit, AUnit enemy) {
        if (!We.protoss() || !unit.isZealot()) return false;

        if (unit.distTo(enemy) > 1.3) return false;
//        if (preventOvercrowdedZealots(unit, enemy)) return true;
        if (preventZealotsTargetingRanged(unit, enemy)) return true;

        return false;
    }

    private static boolean preventOvercrowdedZealots(AUnit unit, AUnit enemy) {
        return unit.allUnitsNear().inRadius(0.5, unit).atLeast(2);
    }

    private static boolean preventZealotsTargetingRanged(AUnit unit, AUnit enemy) {
        return enemy.isRanged();
    }
}
