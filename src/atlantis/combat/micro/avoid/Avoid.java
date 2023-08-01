package atlantis.combat.micro.avoid;

import atlantis.architecture.Manager;
import atlantis.combat.micro.avoid.buildings.CircumnavigateCombatBuilding;
import atlantis.debug.painter.APainter;
import atlantis.game.A;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.Units;
import atlantis.units.actions.Actions;
import atlantis.units.select.Selection;
import bwapi.Color;

public class Avoid extends Manager {
    private final RunError runError;

    public Avoid(AUnit unit) {
        super(unit);
        runError = new RunError(unit);
    }

    @Override
    public boolean applies() {
        return !unit.isABuilding();
    }

    public Manager singleUnit(AUnit enemy) {
        APainter.paintCircle(enemy, 16, Color.Orange);

        if (enemy.position() == null) {
//            System.err.println("enemy.position() is NULL for " + enemy);
            return null;
        }

        if (enemy.isCombatBuilding()) {
            return (new CircumnavigateCombatBuilding(unit)).handle(enemy);
        }

        if (unit.runningManager().runFrom(enemy.position(), calculateRunDistance(enemy), Actions.RUN_ENEMY, false)) {
//            unit.setTooltip(getTooltip(enemy));
            return usedManager(this);
        }

        return runError.handleErrorRun(unit);
    }

//    public Manager groupOfUnits(Units enemiesDangerouslyClose) {
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

    protected double calculateRunDistance(AUnit enemy) {
        if (enemy.isCombatBuilding()) {
            return 0.5;
        }

        if (unit.isVulture()) {
            return 4.5;
        }
        else if (unit.isInfantry()) {
            return 2.7;
        }
        else {
            return 3.5;
        }
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

}
