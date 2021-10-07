package atlantis.combat.micro;

import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;

public class AAvoidInvisibleEnemyUnits {

    /**
     * Avoid Dark Templars, Lurkers, but ignore Protoss Observers.
     */
    public static boolean avoidInvisibleUnits(AUnit unit) {
        if (handledAvoid(unit, AUnitType.Protoss_Dark_Templar)) {
            return true;
        }

        if (handledAvoid(unit, AUnitType.Zerg_Lurker)) {
            return true;
        }

        return false;
    }

    public static boolean handledAvoid(AUnit unit, AUnitType type) {
        double safetyMargin = 2.7 + (type.isMeleeUnit() ? 0 : type.getGroundWeapon().maxRange());

        AUnit hiddenEnemy = Select.enemyOfType(type).inRadius(safetyMargin, unit).first();
        if (hiddenEnemy != null) {
            unit.runFrom(hiddenEnemy);
            return true;
        }

        return false;
    }
}
