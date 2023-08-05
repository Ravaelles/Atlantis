package atlantis.combat.squad.positioning;

import atlantis.architecture.Manager;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.util.We;

public class HugTanks extends Manager {
    public HugTanks(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return We.terran()
            && !unit.isAir()
            && Count.tanks() > 0
            && unit.friendsNear().tanks().inRadius(5, unit).empty();
    }

    @Override
    public Manager handle() {
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

    private boolean shouldGoToTank(AUnit nearestTank) {
        double distToTank = unit.distTo(nearestTank);

        if (distToTank < 4.5) return false;

        if (
            unit.isHealthy()
                && unit.combatEvalRelative() >= 2.5
                && unit.lastUnderAttackMoreThanAgo(30 * 10)
        ) return false;

        if (tankIsOvercrowded(nearestTank)) return false;

        if (distToTank >= 8) return true;

        return false;
    }

    private boolean goToNearestTank(AUnit tank) {
        if (tank == null) {
            return false;
        }

        HasPosition goTo = tank;
        unit.move(goTo, Actions.MOVE_FORMATION, "HugTanks", false);
        unit.addLog("HugTanks");
        return true;
    }

    protected boolean unitIsOvercrowded() {
        return unit.friendsInRadius(2).groundUnits().atLeast(5)
            || unit.friendsInRadius(4).groundUnits().atLeast(10);
    }

    protected boolean tankIsOvercrowded(AUnit tank) {
        return tank.friendsInRadius(2).groundUnits().atLeast(5);
//            || tank.friendsInRadius(4).groundUnits().atLeast(9);
    }
}
