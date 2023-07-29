package atlantis.combat.micro.avoid.special;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.select.Selection;

public class AvoidReavers extends Manager {
    public AvoidReavers(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isGroundUnit();
    }

    @Override
    public Manager handle() {
        if (unit.isAir() || unit.isABuilding()) {
            return null;
        }

        AUnit reaver = unit.enemiesNear().reavers().effUndetected().inRadius(9.4, unit).nearestTo(unit);
        if (reaver == null) {
            return null;
        }

        Selection friendsNear = unit.friendsNear().combatUnits();
        if (
            friendsNear.inRadius(4, unit).atLeast(5) && friendsNear.inRadius(6, unit).atLeast(8)
        ) {
            return null;
        }

        unit.runningManager().runFromAndNotifyOthersToMove(reaver, "REAVER!");
        return usedManager(this);
    }
}