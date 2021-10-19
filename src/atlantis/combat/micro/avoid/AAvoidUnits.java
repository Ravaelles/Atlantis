package atlantis.combat.micro.avoid;

import atlantis.units.AUnit;
import atlantis.units.Select;
import atlantis.units.Units;

import java.util.List;
import java.util.stream.Collectors;

public abstract class AAvoidUnits {

    protected static AUnit unit;
//    private static double _lastSafetyMargin;

    // =========================================================

    public static boolean avoid(AUnit unit) {
        Units enemiesDangerouslyClose = getUnitsToAvoid(unit);

        if (enemiesDangerouslyClose.isEmpty()) {
            return false;
        }
        System.out.println("--------------- " + unit);
        System.out.println(enemiesDangerouslyClose);

        return WantsToAvoid.units(unit, enemiesDangerouslyClose);
    }

    // =========================================================

    public static Units getUnitsToAvoid(AUnit unit) {
        Units enemies = new Units();
        for (AUnit enemy : searchAmongEnemyUnits(unit)) {
            enemies.addUnitWithValue(enemy, SafetyMargin.calculate(enemy, unit));
        }

        if (enemies.isEmpty()) {
            return new Units();
        }

        return new Units(
                enemies.stream()
                .filter(e -> enemies.valueFor(e) < 0)
                .collect(Collectors.toList())
        );
//        AUnit enemyDangerouslyClose = enemies.getUnitWithLowestValue();
//        double safetyMargin = _lastSafetyMargin = enemies.getValueFor(enemyDangerouslyClose);
//        return safetyMargin > 0 ? enemyDangerouslyClose : null;
    }

    public static double lowestSafetyMarginForAnyEnemy(AUnit unit) {
        Units enemies = getUnitsToAvoid(unit);
        if (enemies.isNotEmpty()) {
            return enemies.lowestValue();
//            return _lastSafetyMargin = enemies.lowestValue();
        }

        return 999;
    }

    public static boolean shouldAvoidAnyUnit(AUnit unit) {
        return getUnitsToAvoid(unit) != null;
    }

    // =========================================================

    protected static List<AUnit> searchAmongEnemyUnits(AUnit unit) {
        return Select.enemyRealUnits(true, true, true)
                .canAttack(unit, false)
                .inRadius(13, unit)
                .list();
    }

}
