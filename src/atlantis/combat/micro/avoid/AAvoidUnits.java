package atlantis.combat.micro.avoid;

import atlantis.debug.APainter;
import atlantis.enemy.EnemyUnits;
import atlantis.units.AUnit;
import atlantis.units.Units;
import atlantis.util.A;
import atlantis.util.Cache;
import bwapi.Color;

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

//        if (unit.isSquadScout()) {
//            System.out.println(A.now() + " // " + unit.hp() + " // " + enemiesDangerouslyClose.size());
//        }

        if (enemiesDangerouslyClose.isEmpty()) {
            return false;
        }

//        System.out.println(unit.idWithHash() + " (" + unit.hp() + "hp) has " + enemiesDangerouslyClose.size() + " enemies around");
        AUnit first = enemiesDangerouslyClose.first();
        int firstValue = (int) enemiesDangerouslyClose.valueFor(first);

        APainter.paintLine(unit, unit.targetPosition(), Color.Grey);
        for (AUnit enemy : enemiesDangerouslyClose.list()) {
            APainter.paintLine(enemy, unit, Color.Orange);
            APainter.paintTextCentered(unit, A.dist(unit, enemy), Color.Yellow);
        }
        APainter.paintTextCentered(unit.position().translateByTiles(0, -1),
                "C=" + enemiesDangerouslyClose.size() + "(" + first.shortName() + ":" + firstValue + ")",
                Color.Teal);

        if (WantsToAvoid.units(unit, enemiesDangerouslyClose)) {
            return true;
        }

//        System.err.println("WantsToAvoid false for " + unit.shortNamePlusId());
        return false;
    }

    public static void clearCache() {
        cache.clear();
    }

    // =========================================================

    private static boolean shouldSkip(AUnit unit) {
        return unit.isLoaded();
    }

    public static Units unitsToAvoid(AUnit unit) {
        return unitsToAvoid(unit, true);
    }

    public static Units unitsToAvoid(AUnit unit, boolean onlyDangerouslyClose) {
        return cache.get(
            "unitsToAvoid:" + unit.id() + "," + onlyDangerouslyClose,
            2,
            () -> {
                Units enemies = new Units();
                for (AUnit enemy : enemyUnitsToPotentiallyAvoid(unit)) {
//                    APainter.paintLine(enemy, unit, Color.Yellow);
                    enemies.addUnitWithValue(enemy, SafetyMargin.calculate(unit, enemy));
                }

                if (enemies.isEmpty()) {
                    return new Units();
                }

//                System.out.println(unit + " enemies near = " + enemyUnitsToPotentiallyAvoid(unit).size());
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
        return unit.enemiesNearby()
                .add(EnemyUnits.combatUnitsToBetterAvoid())
                .removeDuplicates()
                .canAttack(unit, true, true, 5)
                .list();
    }

}
