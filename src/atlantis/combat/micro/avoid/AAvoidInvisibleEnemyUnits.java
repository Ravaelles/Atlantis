package atlantis.combat.micro.avoid;

import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;

public class AAvoidInvisibleEnemyUnits extends AAvoidUnits {

    /**
     * Avoid Dark Templars, Lurkers.
     */
    public static boolean avoid(AUnit unit) {
        if (avoidInvisible(unit, AUnitType.Protoss_Dark_Templar)) {
            return true;
        }

        return avoidInvisible(unit, AUnitType.Zerg_Lurker);
    }

    // =========================================================

    private static boolean avoidInvisible(AUnit unit, AUnitType type) {
        double safetyMargin = 3.0 + (unit.isWorker() ? 0.6 : 0) + (type.isMeleeUnit() ? 0 : type.getWeaponRangeAgainst(unit));

        AUnit hiddenEnemy;
        if (unit.isWorker()) {
            hiddenEnemy = Select.enemyOfType(type).inRadius(safetyMargin, unit).first();
        } else {
            hiddenEnemy = Select.enemyOfType(type).invisible().inRadius(safetyMargin, unit).first();
        }

        if (hiddenEnemy != null) {
            unit.runFrom(hiddenEnemy, 3.0 + Math.max(1.8, unit.lastStartedRunningAgo() / 150.0));
            return true;
        }

        return false;
    }
}
