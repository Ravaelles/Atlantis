package atlantis.combat.micro.avoid;

import atlantis.architecture.Manager;
import atlantis.combat.micro.avoid.dont.DontAvoidEnemy;
import atlantis.debug.painter.APainter;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import bwapi.Color;

public class AvoidGroupOfEnemies extends Manager {
    protected final RunError runError;
    private final HasPosition centerOfEnemies;

    public AvoidGroupOfEnemies(AUnit unit, HasPosition centerOfEnemies) {
        super(unit);
        this.centerOfEnemies = centerOfEnemies;
        runError = new RunError(unit);
    }

    public Manager avoid() {
        if (doNotAvoid()) return null;

//        System.err.println("!!!!!!!!!!!!! AVOID PARENTS = " + parentsStack());
//        if (true) return null;

//        if (enemy.isCombatBuilding()) {
//            return (new AvoidCombatBuilding(unit, enemy)).invoke(this);
//        }

//        A.printStackTrace("AvoidSingleEnemy");

        if (unit.runningManager().runFrom(
            centerOfEnemies, calculateRunDistance(), Actions.RUN_ENEMY, false
        )) {
            return usedManager(this);
        }

//        System.err.println(A.now() + " AvoidSingleEnemy - run error for " + unit);

        return runError.handleErrorRun(unit);
    }

    private boolean doNotAvoid() {
        if (centerOfEnemies == null) return true;
        if (unit.effUndetected()) return true;
        if (unit.hp() >= 33 && unit.isRepairerOfAnyKind()) return true;

        if ((new DontAvoidEnemy(unit)).invoke(this) != null) return true;

        return false;
    }

    protected double calculateRunDistance() {
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

    @Override
    public String toString() {
        String target = unit.runningFrom() == null ? "NULL_FROM" : unit.runningFrom().type().name();
        return super.toString() + "(" + target + ")";
    }
}
