package atlantis.combat.micro.avoid;

import atlantis.architecture.Manager;
import atlantis.debug.painter.APainter;
import atlantis.game.A;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.Units;
import atlantis.units.select.Selection;
import bwapi.Color;

public class Avoid extends Manager {
    protected AUnit enemy;

    public Avoid(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return !unit.isABuilding() && !unit.effUndetected();
    }

    public Manager singleUnit(AUnit enemy) {
        this.enemy = enemy;

        return (new AvoidSingleEnemy(unit, enemy)).avoid();
    }

//    public Manager groupOfUnits(Units enemiesDangerouslyClose) {
//        this.enemy = enemy;
//
//        HasPosition runFrom = defineRunFromForGroupOfUnits(enemiesDangerouslyClose);
//
//        if (runFrom == null) {
//            System.err.println("Run from group is null for " + unit);
//            enemiesDangerouslyClose.print("Group of units to run from");
//            return null;
//        }
//
//        APainter.paintCircle(runFrom, 6, Color.Orange);
//        APainter.paintCircle(runFrom, 4, Color.Orange);
//        APainter.paintCircle(runFrom, 2, Color.Orange);
//
//        if (unit.runningManager().runFrom(
//            runFrom, calculateRunDistance(enemiesDangerouslyClose.first()), Actions.RUN_ENEMIES, false)
//        ) {
////            APainter.paintCircleFilled(unit.position(), 5, Color.Green);
////            APainter.paintCircleFilled(runFrom, 5, Color.Red);
//            unit.setTooltip("GroupAvoid(" + A.digit(unit.distTo(runFrom)) + ")");
//            return usingManager(this);
//        }
//
//        return runError.handleErrorRun(unit);
//    }

    // =========================================================

    private HasPosition defineRunFromForGroupOfUnits(Units enemiesDangerouslyClose) {
        Selection enemies = enemiesDangerouslyClose.selection().havingPosition();

        int takeOnly = unit.isDragoon() ? 2 : 3;
        if (enemies.size() >= takeOnly) {
            return enemies.limit(takeOnly).units().average();
        }

        for (AUnit enemy : enemiesDangerouslyClose.list()) {
            APainter.paintCircle(enemy, 16, Color.Orange);
        }

        return enemies.first().translatePercentTowards(15, enemies.second());
    }

    protected String getTooltip(AUnit enemy) {
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

    @Override
    public String toString() {
        return "Avoid(" + enemy.type() + ")";
    }
}
