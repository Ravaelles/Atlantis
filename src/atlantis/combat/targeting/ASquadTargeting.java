package atlantis.combat.targeting;

import atlantis.units.AUnit;
import atlantis.units.select.Selection;
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

    public static boolean isValidTarget(AUnit unit, AUnit enemy) {
        if (enemy == null) return false;
        // Positionless fogged memory is not a target (owner 2026-10-26:
        // cleared addresses must not crash or attract acquisition).
        if (!enemy.hasPosition()) return false;
        // Undetected enemies are not targets: the engine refuses the
        // order, so naming one only spams "not detected" every frame
        // (live: dragoons surrounding an unseen Observer). Weakest-first
        // would otherwise prefer exactly these - unreadable hp scans low.
        if (!enemy.isDetected()) return false;
        if (enemy.isABuilding() && !enemy.isCombatBuilding()) return false;
        if (enemy.isZergling()) return false;
        if (enemy.isWorker()) return false;
        // No hp floor: a nearly dead enemy is the BEST target, not an
        // invalid one (owner: dragoons kept shooting the healthiest
        // chewer while 8hp ones were unselectable - finishing is rule
        // (2) of the agreed #184 ladder, inverse of the old hp<=15 ban).
        // Dead units never reach here: every caller filters isAlive
        // before validity (focus scan, keepable, shared, squad-last).
        if (enemy.isDeadMan()) return false;
        if (preventForZealots(unit, enemy)) return false;

        return unit.hasWeaponToAttackThisUnit(enemy);
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
            if (!enemy.hasPosition()) continue;
            if (!enemy.isDetected()) continue;
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
        return preventZealotsTargetingRanged(unit, enemy);
    }

    private static boolean preventOvercrowdedZealots(AUnit unit, AUnit enemy) {
        return unit.allUnitsNear().inRadius(0.5, unit).atLeast(2);
    }

    private static boolean preventZealotsTargetingRanged(AUnit unit, AUnit enemy) {
        return enemy.isRanged();
    }
}
