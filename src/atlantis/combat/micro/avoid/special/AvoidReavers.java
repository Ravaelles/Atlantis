package atlantis.combat.micro.avoid.special;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.select.Selection;
import atlantis.util.We;

public class AvoidReavers extends Manager {
    public AvoidReavers(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isGroundUnit() && !unit.isABuilding() && !unit.isTank() && !unit.isMissionDefend();
    }

    @Override
    protected Manager handle() {
        AUnit reaver = unit.enemiesNear().reavers().inRadius(9.9, unit).nearestTo(unit);
        if (reaver == null) {
            return null;
        }

        if (enoughForcesNotToRunFromReaver(reaver)) return null;

        if (unit.isCombatUnit()) {
            Selection friendsNear = unit.friendsNear().combatUnits();
            if (
                friendsNear.inRadius(4, unit).atLeast(5) && friendsNear.inRadius(6, unit).atLeast(8)
            ) {
                return null;
            }
        }

        unit.runningManager().runFromAndNotifyOthersToMove(reaver, "REAVER!");
        return usedManager(this);
    }

    private boolean enoughForcesNotToRunFromReaver(AUnit reaver) {
        int MIN_FORCES_TO_FIGHT = We.terran() ? 11 : (We.protoss() ? 6 : 9);

        return reaver
            .enemiesNear()
            .combatUnits()
            .havingAntiGroundWeapon()
            .inRadius(16, unit)
            .atLeast(MIN_FORCES_TO_FIGHT);
    }
}
