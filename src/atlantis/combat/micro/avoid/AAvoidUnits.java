package atlantis.combat.micro.avoid;

import atlantis.units.AUnit;
import atlantis.units.select.Select;
import atlantis.units.Units;

import java.util.List;
import java.util.stream.Collectors;

public abstract class AAvoidUnits {

    protected static AUnit unit;

    // =========================================================

    public static boolean avoidEnemiesIfNeeded(AUnit unit) {
        Units enemiesDangerouslyClose = getUnitsToAvoid(unit);
        if (enemiesDangerouslyClose.isEmpty()) {
            return false;
        }

        return WantsToAvoid.units(unit, enemiesDangerouslyClose);
    }

    // =========================================================

    public static Units getUnitsToAvoid(AUnit unit) {
        return getUnitsToAvoid(unit, true);
    }

    public static Units getUnitsToAvoid(AUnit unit, boolean onlyDangerouslyClose) {
        Units enemies = new Units();
        for (AUnit enemy : searchProblematicEnemyUnits(unit)) {
            enemies.addUnitWithValue(enemy, SafetyMargin.calculate(enemy, unit));
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
        }
        else {
            return enemies;
        }
    }

    public static double lowestSafetyMarginForAnyEnemy(AUnit unit) {
        Units enemies = getUnitsToAvoid(unit, false);
        if (enemies.isNotEmpty()) {
            return enemies.lowestValue();
        }

        return 9876;
    }

    public static boolean shouldAvoidAnyUnit(AUnit unit) {
        return getUnitsToAvoid(unit).isNotEmpty();
    }

    public static boolean shouldNotAvoidAnyUnit(AUnit unit) {
        return !shouldAvoidAnyUnit(unit);
    }

    // =========================================================

    protected static List<? extends AUnit> searchProblematicEnemyUnits(AUnit unit) {
        return Select.enemyRealUnits(true, true, true)
                .canAttack(unit, true, true, 6)
                .inRadius(14, unit)
                .list();
    }

}
