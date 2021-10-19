package atlantis.combat.micro.avoid;

import atlantis.combat.micro.AAttackEnemyUnit;
import atlantis.debug.APainter;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;
import atlantis.units.Units;
import bwapi.Color;

import java.util.List;

public abstract class AAvoidUnits {

    protected static AUnit unit;
    private static double _lastSafetyMargin;

    // =========================================================

    public static boolean avoid(AUnit unit) {
//        if (!Missions.isGlobalMissionAttack()) {
//            if (AAvoidEnemyDefensiveBuildings.avoid(unit, false)) {
//                return true;
//            }
//        }

        AUnit enemyDangerouslyClose = getUnitToAvoid(unit);
        if (enemyDangerouslyClose != null) {
            return AvoidUnit.avoidUnit(unit, enemyDangerouslyClose);
        }

        return false;
    }

    // =========================================================

    public static AUnit getUnitToAvoid(AUnit unit) {
        Units enemies = new Units();
        for (AUnit enemy : enemyUnitsToTakeIntoAccount(unit)) {
            enemies.addUnitWithValue(enemy, SafetyMargin.calculate(enemy, unit));
        }

        if (enemies.isEmpty()) {
            return null;
        }

        AUnit enemyDangerouslyClose = enemies.getUnitWithLowestValue();
        double safetyMargin = _lastSafetyMargin = enemies.getValueFor(enemyDangerouslyClose);
        return safetyMargin > 0 ? enemyDangerouslyClose : null;
    }

    public static double lowestSafetyMarginForAnyEnemy(AUnit unit) {
        if (getUnitToAvoid(unit) != null) {
            return _lastSafetyMargin;
        }

        return 999;
    }

    public static boolean shouldAvoidAnyUnit(AUnit unit) {
        return getUnitToAvoid(unit) != null;
    }

    // =========================================================

    protected static List<AUnit> enemyUnitsToTakeIntoAccount(AUnit unit) {
        return Select.enemyRealUnits(true, true, true)
                .canAttack(unit, false)
                .inRadius(13, unit)
                .list();
    }

}
