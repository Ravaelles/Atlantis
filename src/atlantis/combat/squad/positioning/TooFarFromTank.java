package atlantis.combat.squad.positioning;

import atlantis.architecture.Manager;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.We;

public class TooFarFromTank extends Manager {
    public static final double MAX_DIST_FROM_TANK = 4.6;
    public static final int MIN_DIST_FROM_TANK = 2;
    private double distToTank;

    public TooFarFromTank(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (unit.isMissionDefend()) return false;

        return We.terran()
            && !unit.isAir()
            && (Count.tanks()) >= 1
            && !unitIsOvercrowded();
//            && (unit.squad().units().tanks().atMost(4) || !unitIsOvercrowded());
    }

    @Override
    protected Manager handle() {

        // Too far from nearest tank
        AUnit nearestTank = Select.ourTanks().exclude(unit).nearestTo(unit);
        if (nearestTank != null && shouldGoToTank(nearestTank)) {
            if (goToNearestTank(nearestTank)) return usedManager(this);
        }

        return null;
    }

    private boolean shouldGoToTank(AUnit tank) {
        distToTank = unit.distTo(tank);

        if (distToTank < MIN_DIST_FROM_TANK) return false;

        if (distToTank > MAX_DIST_FROM_TANK + (unit.id() % 4) / 2.0) return true;

        if (distToTank <= 7 && tankIsOvercrowded(tank)) return false;

        Selection tanksInSquad = unit.squad().units().tanks();

        if (!unit.isTank()) {
            if (tanksInSquad.atLeast(4) && tanksInSquad.inRadius(10, unit).atMost(3)) return true;
        }

//        if (
//            unit.isHealthy()
//                && unit.combatEvalRelative() >= 2.5
//                && unit.lastUnderAttackMoreThanAgo(30 * 10)
//        ) return false;

        return false;
    }

    private boolean goToNearestTank(AUnit tank) {
        if (tank == null) return false;

        HasPosition goTo = tank;
        unit.move(goTo, Actions.MOVE_FORMATION, "TooFarFromTank", false);
        unit.addLog("TooFarFromTank");
        return true;
    }

    protected boolean unitIsOvercrowded() {
        return unit.friendsInRadius(2).groundUnits().atLeast(6)
            && unit.friendsInRadius(4).groundUnits().atLeast(13);
    }

    protected boolean tankIsOvercrowded(AUnit tank) {
        return tank.friendsInRadius(0.5).groundUnits().atLeast(4)
            || tank.friendsInRadius(2).groundUnits().atLeast(7)
            || tank.friendsInRadius(4).groundUnits().atLeast(13);
    }
}
