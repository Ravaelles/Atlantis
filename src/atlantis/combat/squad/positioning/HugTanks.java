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
            && !unitIsOvercrowded()
            && unit.friendsNear().tanks().inRadius(5, unit).empty();
    }

    @Override
    public Manager handle() {
        if (unit.isMissionDefend()) {
            return null;
        }

        // Too far from nearest tank
        AUnit nearestTank = Select.ourTanks().nearestTo(unit);
        if (nearestTank != null && unit.distTo(nearestTank) > 4.5) {
            if (goToNearestTank(nearestTank)) {
                return usedManager(this);
            }
        }

        return null;
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

//    protected boolean tankIsOvercrowded(AUnit tank) {
//        return tank.friendsInRadius(2).groundUnits().atLeast(5)
//            || tank.friendsInRadius(4).groundUnits().atLeast(9);
//    }
}
