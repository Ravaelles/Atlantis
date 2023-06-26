package atlantis.combat.micro.avoid;

import atlantis.combat.micro.avoid.buildings.AvoidCombatBuildings;
import atlantis.combat.micro.avoid.margin.SafetyMargin;
import atlantis.debug.painter.APainter;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.Units;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;
import atlantis.util.cache.Cache;
import bwapi.Color;

import java.util.List;
import java.util.stream.Collectors;

public abstract class AvoidEnemies {

    protected static AUnit unit;
    private static Cache<Units> cache = new Cache<>();

    // =========================================================

    public static boolean avoidEnemiesIfNeeded(AUnit unit) {
        if (shouldSkip(unit)) {
            return false;
        }

        Units enemiesDangerouslyClose = unitsToAvoid(unit);

        if (enemiesDangerouslyClose.isEmpty()) {
//            System.err.println("@ " + A.now() + " - No-one close");
//            AUnit nearestEnemy = unit.enemiesNear().nearestTo(unit);
//            APainter.paintTextCentered(unit.position().translateByTiles(0, -1),
//                    "C=0(" + (nearestEnemy != null ? A.dist(unit, nearestEnemy) : "-") + ")",
//                    Color.Green                           );
            return false;
        }

//        APainter.paintLine(unit, unit.targetPosition(), Color.Grey);
//        for (AUnit enemy : enemiesDangerouslyClose.list()) {
//            APainter.paintLine(enemy, unit, Color.Orange);
////            APainter.paintTextCentered(unit, A.dist(unit, enemy), Color.Yellow);
//        }

        // =========================================================

        if (
            onlyEnemyCombatBuildingsAreNear(enemiesDangerouslyClose)
                || unit.isAir()
                || unit.hp() <= 40
                || unit.combatEvalRelative() <= 2.7
        ) {
            if (AvoidCombatBuildings.update(unit, enemiesDangerouslyClose)) {
                unit.addLog("KeepAway");
                return true;
            }
        }

        // Only ENEMY WORKERS
        if (
            unit.hpPercent() >= 70
            && Select.from(enemiesDangerouslyClose).workers().size() == enemiesDangerouslyClose.size()
        ) {
            unit.addLog("FightWorkers");
            return false;
        }

        else {
            if (WantsToAvoid.unitOrUnits(unit, enemiesDangerouslyClose)) {
                return true;
            }
        }

        return false;
    }

    public static void clearCache() {
        cache.clear();
    }

    // =========================================================

    private static boolean shouldSkip(AUnit unit) {
        if (
            unit.hp() <= 16 && unit.isMelee() && unit.isCombatUnit()
            && unit.enemiesNear().groundUnits().effVisible().inRadius(1, unit).notEmpty()
        ) {
            unit.setTooltipTactical("Kamikaze");
            return true;
        }

        if (
            unit.lastActionLessThanAgo(5, Actions.ATTACK_UNIT)
            && unit.lastStartedAttackMoreThanAgo(8)
        ) {
            unit.setTooltipTactical("StartAttack");
            return true;
        }

        return unit.isLoaded();
    }

    private static boolean onlyEnemyCombatBuildingsAreNear(Units enemiesDangerouslyClose) {
        return Select.from(enemiesDangerouslyClose).combatBuildings(false).size() == enemiesDangerouslyClose.size();
    }

    public static Units unitsToAvoid(AUnit unit) {
        return unitsToAvoid(unit, true);
    }

    public static Units unitsToAvoid(AUnit unit, boolean onlyDangerouslyClose) {
        return cache.get(
            "unitsToAvoid:" + unit.id() + "," + onlyDangerouslyClose,
            0,
            () -> {
                Units enemies = new Units();
//                System.out.println("enemyUnitsToPotentiallyAvoid(unit) = " + enemyUnitsToPotentiallyAvoid(unit).size());
                for (AUnit enemy : enemyUnitsToPotentiallyAvoid(unit)) {
                    double safetyMargin = SafetyMargin.calculate(unit, enemy);
//                    System.err.println(
//                        enemy + " // " + String.format("%.2f", safetyMargin) + " // " + A.dist(enemy.distTo(unit))
//                    );
//                    APainter.paintLine(enemy, unit, Color.Yellow);
                    enemies.addUnitWithValue(enemy, safetyMargin);
                }
//                enemies.print("Enemies to avoid");

//                System.out.println(unit + " enemies near = " + enemyUnitsToPotentiallyAvoid(unit).size());

                if (enemies.isEmpty()) {
                    return new Units();
                }

//                for (AUnit enemy : enemyUnitsToPotentiallyAvoid(unit)) {
////                    //System.out.println(enemy + " which is " + A.dist(enemy, unit) + " away");
//                    System.out.println(enemy + " has value: " + enemies.valueFor(enemy));
//                }

                if (onlyDangerouslyClose) {
                    return enemies.replaceUnitsWith(
                            enemies.stream()
                                    .filter(e -> enemies.valueFor(e) < 0)
                                    .collect(Collectors.toList())
                    );
                } else {
                    return enemies;
                }
            }
        );
    }

//    public static double lowestSafetyMarginForAnyEnemy(AUnit unit) {
//        Units enemies = unitsToAvoid(unit, false);
//        if (enemies.isNotEmpty()) {
//            return enemies.lowestValue();
//        }
//
////        return 9876;
//        return 0;
//    }

    public static boolean shouldAvoidAnyUnit(AUnit unit) {
        return unitsToAvoid(unit).isNotEmpty();
    }

    public static boolean shouldNotAvoidAnyUnit(AUnit unit) {
        return !shouldAvoidAnyUnit(unit);
    }

    // =========================================================

    protected static List<? extends AUnit> enemyUnitsToPotentiallyAvoid(AUnit unit) {
        return unit.enemiesNear()
//                .nonBuildings() // This is because we rely on AvoidCombatBuildings
                .removeDuplicates()
                .onlyCompleted()
                .canAttack(unit, true, true, 4.5)
                .havingPosition()
                .list();
    }

}
