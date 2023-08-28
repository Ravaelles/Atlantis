package atlantis.combat.micro.avoid;

import atlantis.architecture.Manager;
import atlantis.debug.painter.APainter;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import bwapi.Color;

public class AvoidSingleEnemy extends Manager {
    protected final RunError runError;
    private final AUnit enemy;

    public AvoidSingleEnemy(AUnit unit, AUnit enemy) {
        super(unit);
        this.enemy = enemy;
        runError = new RunError(unit);
    }

    public Manager avoid() {
        APainter.paintCircle(enemy, 16, Color.Orange);

        if (enemy.position() == null) {
//            System.err.println("enemy.position() is NULL for " + enemy);
            return null;
        }

//        if (enemy.isCombatBuilding()) {
//            return (new AvoidCombatBuilding(unit, enemy)).invoke();
//        }

        if (unit.runningManager().runFrom(
            enemy, calculateRunDistance(enemy), Actions.RUN_ENEMY, false
        )) {
//            unit.setTooltip(getTooltip(enemy));
            return usedManager(this);
        }

        return runError.handleErrorRun(unit);
    }

    protected double calculateRunDistance(AUnit enemy) {
        if (enemy.isCombatBuilding()) {
            return 0.25;
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
}