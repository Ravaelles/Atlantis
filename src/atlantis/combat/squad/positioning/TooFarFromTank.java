package atlantis.combat.squad.positioning;

import atlantis.architecture.Manager;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.util.We;

public class TooFarFromTank extends Manager {
    public static final int MAX_DIST_FROM_TANK = 5;
    public static final int MIN_DIST_FROM_TANK = 2;
    private int tanks;

    public TooFarFromTank(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        tanks = Count.tanks();
        return We.terran()
            && !unit.isAir()
            && tanks >= 1
            && !unitIsOvercrowded();
    }

    @Override
    protected Manager handle() {
        if (unit.isMissionDefend()) {
            return null;
        }

        // Too far from nearest tank
        AUnit nearestTank = Select.ourTanks().nearestTo(unit);
        if (nearestTank != null && shouldGoToTank(nearestTank)) {
            goToNearestTank(nearestTank);
            return usedManager(this);
        }

        return null;
    }

    private boolean shouldGoToTank(AUnit tank) {
        if (tankIsOvercrowded(tank)) return false;

        double distToTank = unit.distTo(tank);

        if (tanks <= 1 && distToTank > MAX_DIST_FROM_TANK) return false;
        if (distToTank < MIN_DIST_FROM_TANK) return false;

        if (
            unit.isHealthy()
                && unit.combatEvalRelative() >= 2.5
                && unit.lastUnderAttackMoreThanAgo(30 * 10)
        ) return false;

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
