package atlantis.combat.micro.avoid;

import atlantis.combat.retreating.RetreatManager;
import atlantis.units.AUnit;
import atlantis.units.Units;
import atlantis.units.select.Select;
import atlantis.util.cache.Cache;

import java.util.List;
import java.util.stream.Collectors;

public abstract class AAvoidUnits {

    protected static AUnit unit;
    private static Cache<Units> cache = new Cache<>();

    // =========================================================

    public static boolean avoidEnemiesIfNeeded(AUnit unit) {
        if (shouldSkip(unit)) {
            return false;
        }

        Units enemiesDangerouslyClose = unitsToAvoid(unit);

        if (enemiesDangerouslyClose.isEmpty()) {
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

        // Only COMBAT BUILDINGS
        if (onlyCombatBuildingsAreDangerouslyClose(enemiesDangerouslyClose)) {
            if (
                    RetreatManager.shouldNotEngageCombatBuilding(unit)
                    && AvoidCombatBuildingsFix.handle(unit, enemiesDangerouslyClose)
            ) {
                unit.addLog("KeepAway");
                return true;
            }

            return false;
        }

        // Only ENEMY WORKERS
        if (unit.hpPercent() >= 70 && Select.from(enemiesDangerouslyClose).workers().size() == enemiesDangerouslyClose.size()) {
            unit.addLog("FightWorkers");
            return false;
        }

        else {
            // Standard case
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
        return unit.isLoaded();
    }

    private static boolean onlyCombatBuildingsAreDangerouslyClose(Units enemiesDangerouslyClose) {
        return Select.from(enemiesDangerouslyClose).combatBuildings(false).size() == enemiesDangerouslyClose.size();
    }

    public static Units unitsToAvoid(AUnit unit) {
        return unitsToAvoid(unit, true);
    }

    public static Units unitsToAvoid(AUnit unit, boolean onlyDangerouslyClose) {
        return cache.get(
            "unitsToAvoid:" + unit.id() + "," + onlyDangerouslyClose,
            4,
            () -> {
                Units enemies = new Units();
//                System.out.println("enemyUnitsToPotentiallyAvoid(unit) = " + enemyUnitsToPotentiallyAvoid(unit).size());
                for (AUnit enemy : enemyUnitsToPotentiallyAvoid(unit)) {
//                    APainter.paintLine(enemy, unit, Color.Yellow);
                    enemies.addUnitWithValue(enemy, SafetyMargin.calculate(unit, enemy));
                }
//                enemies.print("Enemies to avoid");

//                System.out.println(unit + " enemies near = " + enemyUnitsToPotentiallyAvoid(unit).size());

                if (enemies.isEmpty()) {
                    return new Units();
                }

//                for (AUnit enemy : enemyUnitsToPotentiallyAvoid(unit)) {
//                    System.out.println(enemy + " which is " + A.dist(enemy, unit) + " away from " + unit);
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

    public static double lowestSafetyMarginForAnyEnemy(AUnit unit) {
        Units enemies = unitsToAvoid(unit, false);
        if (enemies.isNotEmpty()) {
            return enemies.lowestValue();
        }

        return 9876;
    }

    public static boolean shouldAvoidAnyUnit(AUnit unit) {
        return unitsToAvoid(unit).isNotEmpty();
    }

    public static boolean shouldNotAvoidAnyUnit(AUnit unit) {
        return !shouldAvoidAnyUnit(unit);
    }

    // =========================================================

    protected static List<? extends AUnit> enemyUnitsToPotentiallyAvoid(AUnit unit) {
        return unit.enemiesNear()
                .removeDuplicates()
                .onlyCompleted()
                .havingPosition()
                .canAttack(unit, true, true, 4.5)
                .list();
    }

}
