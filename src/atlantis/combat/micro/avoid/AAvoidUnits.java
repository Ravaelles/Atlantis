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
//        if (unit.isFirstCombatUnit()) {
//            System.out.println("--------------- " + unit);
//            System.out.println(enemiesDangerouslyClose);
//        }

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

//        if (unit.isFirstCombatUnit()) {
//            enemies.print();
//        }

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
//        AUnit enemyDangerouslyClose = enemies.getUnitWithLowestValue();
//        double safetyMargin = _lastSafetyMargin = enemies.getValueFor(enemyDangerouslyClose);
//        return safetyMargin > 0 ? enemyDangerouslyClose : null;
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
