package atlantis.combat.squad.positioning;

import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.architecture.Manager;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.util.We;

public class ComeCloserToTanks extends Manager {
    public ComeCloserToTanks(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return We.terran() && Count.tanks() > 0 && !unitIsOvercrowded();
    }

    @Override
    public Manager handle() {
        if (unit.isMissionDefend()) {
            return null;
        }

        // Too far from nearest tank
        if (squad.units().tanks().count() >= 1) {
            if (goToNearestTank()) {
                return usedManager(this);
            }
        }

        return null;
    }

    private boolean goToNearestTank() {
        AUnit tank = Select.ourTanks().nearestTo(unit);
        if (tank != null && !tankIsOvercrowded(tank)) {
            APosition goTo = unit.translateTilesTowards(1.5, tank)
                .makeFreeOfAnyGroundUnits(1.5, 0.25, unit);

            if (goTo != null && unit.move(goTo, Actions.MOVE_FORMATION, "HugTanks", false)) {
                unit.addLog("HugTanks");
                return true;
            }
        }

        return false;
    }

    protected boolean unitIsOvercrowded() {
        return unit.friendsInRadius(2).groundUnits().atLeast(5)
            || unit.friendsInRadius(4).groundUnits().atLeast(10);
    }

    protected boolean tankIsOvercrowded(AUnit tank) {
        return tank.friendsInRadius(2).groundUnits().atLeast(5)
            || tank.friendsInRadius(4).groundUnits().atLeast(9);
    }
}
