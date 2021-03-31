package atlantis.combat.micro;

import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;

public class AAvoidInvisibleEnemyManager {

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
        double safetyMargin = 1.5;
        double safeDist = (type.isMeleeUnit() ? 1 : type.getGroundWeapon().maxRange()) + safetyMargin;

        AUnit hiddenEnemy = Select.enemyOfType(AUnitType.Protoss_Dark_Templar).inRadius(2.6, unit).first();
        if (hiddenEnemy != null) {
            unit.runFrom(hiddenEnemy);
            return true;
        }

        return false;
    }
}
