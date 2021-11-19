package atlantis.combat.micro.avoid;

import atlantis.enemy.EnemyUnits;
import atlantis.units.AUnit;
import atlantis.units.Units;
import atlantis.util.Cache;

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
            return false;
        }

        return WantsToAvoid.units(unit, enemiesDangerouslyClose);
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
            "getUnitsToAvoid:" + unit.id() + "," + onlyDangerouslyClose,
            0,
            () -> {
                Units enemies = new Units();
                for (AUnit enemy : enemyUnitsToPotentiallyAvoid(unit)) {
                    enemies.addUnitWithValue(enemy, SafetyMargin.calculate(unit, enemy));
                }

                if (enemies.isEmpty()) {
                    return new Units();
                }

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
                .add(EnemyUnits.combatBuildings())
                .canAttack(unit, true, true, 6)
                .list();
    }

}
