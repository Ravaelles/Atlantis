package atlantis.combat.micro.avoid;

import atlantis.combat.micro.AAttackEnemyUnit;
import atlantis.debug.APainter;
import atlantis.units.AUnit;
import atlantis.util.A;
import bwapi.Color;

public class AvoidUnit {

    public static boolean avoidUnit(AUnit unit, AUnit enemy) {
        APainter.paintCircle(enemy, 16, Color.Orange);

        if (unit.runFrom(enemy, getRunDistance(unit, enemy))) {
            unit.setTooltip(getTooltip(unit, enemy));
            return true;
        }

        return handleErrorRun(unit);
    }

    // =========================================================

    protected static double getRunDistance(AUnit unit, AUnit enemy) {
        return 3.5;
    }

    protected static String getTooltip(AUnit unit, AUnit enemy) {
        String dist = "(" + A.digit(unit.distanceTo(enemy)) + ")";

        if (enemy.invisible()) {
            return "MeleeRun" + dist;
        }
        else if (enemy.isRangedUnit()) {
            return "RangedRun" + dist;
        }
        else {
            return "MeleeRun" + dist;
        }
    }

    protected static boolean handleErrorRun(AUnit unit) {
        System.err.println("ERROR_RUN for " + unit.getShortNamePlusId());

        AAttackEnemyUnit.handleAttackNearbyEnemyUnits(unit);
        unit.setTooltip("Cant run, fight");

        return true;
    }

}
