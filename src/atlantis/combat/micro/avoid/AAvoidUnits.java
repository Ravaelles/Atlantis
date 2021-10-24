package atlantis.combat.micro.avoid;

import atlantis.units.AUnit;
import atlantis.units.Select;
import atlantis.units.Units;
import atlantis.units.actions.UnitActions;

import java.util.List;
import java.util.stream.Collectors;

public abstract class AAvoidUnits {

    protected static AUnit unit;

    // =========================================================

    public static boolean avoid(AUnit unit) {
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
        for (AUnit enemy : searchAmongEnemyUnits(unit)) {
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

    protected static List<AUnit> searchAmongEnemyUnits(AUnit unit) {
        return Select.enemyRealUnits(true, true, true)
                .canAttack(unit, false, true)
                .inRadius(13, unit)
                .list();
    }

}
