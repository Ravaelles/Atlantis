package atlantis.combat.micro.avoid;

import atlantis.combat.micro.AAttackEnemyUnit;
import atlantis.debug.APainter;
import atlantis.position.APosition;
import atlantis.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.Units;
import atlantis.util.A;
import bwapi.Color;

public class Avoid {

    public static boolean unit(AUnit unit, AUnit enemy) {
        APainter.paintCircle(enemy, 16, Color.Orange);

        if (unit.runningManager().runFrom(enemy.position(), getRunDistance(unit))) {
            unit.setTooltip(getTooltip(unit, enemy));
            return true;
        }

        return handleErrorRun(unit);
    }

    public static boolean groupOfUnits(AUnit unit, Units enemiesDangerouslyClose) {
        HasPosition runFrom = defineRunFromForGroupOfUnits(enemiesDangerouslyClose);
        APainter.paintCircle(runFrom, 6, Color.Orange);
        APainter.paintCircle(runFrom, 4, Color.Orange);
        APainter.paintCircle(runFrom, 2, Color.Orange);

        if (unit.runningManager().runFrom(runFrom, getRunDistance(unit))) {
            unit.setTooltip("GroupAvoid(" + A.digit(unit.distTo(runFrom)) + ")");
            return true;
        }

        return handleErrorRun(unit);
    }

    // =========================================================

    private static HasPosition defineRunFromForGroupOfUnits(Units enemiesDangerouslyClose) {
        if (enemiesDangerouslyClose.size() >= 3) {
            Units nearestEnemies = new Units();
            nearestEnemies.addUnit(enemiesDangerouslyClose.get(0));
            nearestEnemies.addUnit(enemiesDangerouslyClose.get(1));
            nearestEnemies.addUnit(enemiesDangerouslyClose.get(2));
            return nearestEnemies.average();
        }

        for (AUnit enemy : enemiesDangerouslyClose.list()) {
            APainter.paintCircle(enemy, 16, Color.Orange);
        }

        return enemiesDangerouslyClose.first();
    }

    protected static double getRunDistance(AUnit unit) {
        if (unit.isVulture()) {
            return 4.5;
        }

        if (unit.isInfantry()) {
            return 2.7;
        }

        return 3.5;
    }

    protected static String getTooltip(AUnit unit, AUnit enemy) {
        String dist = "(" + A.digit(unit.distTo(enemy)) + ")";

        if (enemy.isMelee()) {
            return "MeleeRun" + dist;
        }
        else if (enemy.isRanged()) {
            return "RangedRun" + dist;
        }
        else {
            return "MeleeRun" + dist;
        }
    }

    protected static boolean handleErrorRun(AUnit unit) {
//        System.err.println("ERROR_RUN for " + unit.nameWithId());

        AAttackEnemyUnit.handleAttackNearbyEnemyUnits(unit);
        unit.setTooltip("Cant run, fight");

        return true;
    }

}
