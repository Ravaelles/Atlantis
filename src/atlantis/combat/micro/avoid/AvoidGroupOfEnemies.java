package atlantis.combat.micro.avoid;

import atlantis.architecture.Manager;
import atlantis.combat.micro.avoid.dont.DontAvoidEnemy;
import atlantis.game.A;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.Units;
import atlantis.units.actions.Actions;
import atlantis.units.select.Count;
import atlantis.units.select.Have;

import static atlantis.units.actions.Actions.RUN_ENEMY;

public class AvoidGroupOfEnemies extends Manager {
    protected final RunError runError;
    private final Units enemies;
    private final HasPosition centerOfEnemies;

    public AvoidGroupOfEnemies(AUnit unit, Units enemies, HasPosition centerOfEnemies) {
        super(unit);
        this.enemies = enemies;
        this.centerOfEnemies = centerOfEnemies;
        runError = new RunError(unit);
    }

    public Manager avoid() {
        if (doNotAvoid()) return processDontAvoid();

//        System.err.println("!!!!!!!!!!!!! AVOID PARENTS = " + parentsStack());
//        if (true) return null;

//        if (enemy.isCombatBuilding()) {
//            return (new AvoidCombatBuilding(unit, enemy)).invoke(this);
//        }

//        A.printStackTrace("AvoidSingleEnemy");

//        if (
//            unit.hp() >= 40
//                && A.seconds() <= 400
//                && Have.main()
//                && Count.workers() >= 10
//                && unit.distToBase() >= 50
//                && unit.moveToMain(RUN_ENEMY, "AvoidGroupToBase")
//        ) {
//            return usedManager(this);
//        }

        if (unit.runningManager().runFrom(
            centerOfEnemies, calculateRunDistance(), RUN_ENEMY, allowedToNotifyNearUnitsToMakeSpace()
        )) {
            return usedManager(this);
        }

//        System.err.println(A.now() + " AvoidSingleEnemy - run error for " + unit);

        return runError.handleErrorRun(unit);
    }

    private boolean allowedToNotifyNearUnitsToMakeSpace() {
        return unit.distToNearestChokeCenter() <= 4;
    }

    private Manager processDontAvoid() {
        unit.runningManager().stopRunning();
        return null;
    }

    private boolean doNotAvoid() {
        if (centerOfEnemies == null) return true;
        if (unit.effUndetected()) return true;
        if (unit.hp() >= 33 && unit.isRepairerOfAnyKind()) return true;

        if ((new DontAvoidEnemy(unit)).applies()) return true;

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
//        String target = unit.runningFrom() == null ? "NULL_FROM" : unit.runningFrom().type().name();
        return super.toString() + "(" + enemies.size() + ")";
    }
}
