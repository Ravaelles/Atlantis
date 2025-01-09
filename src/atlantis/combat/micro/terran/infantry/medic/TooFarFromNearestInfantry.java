package atlantis.combat.micro.terran.infantry.medic;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;

public class TooFarFromNearestInfantry extends Manager {
    private AUnit infantry;
    private double distTo;

    public TooFarFromNearestInfantry(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        infantry = unit.friendsNear().terranInfantryWithoutMedics().nearestTo(unit);

        if (infantry == null) infantry = Select.ourTerranInfantryWithoutMedics().nearestTo(unit);
        if (infantry == null) return false;

        distTo = infantry.distTo(unit);
        return true;
    }

    @Override
    public Manager handle() {
        if (distTo >= 1) {
            unit.move(infantry, Actions.MOVE_FOCUS, "SemperFi", false);
            return usedManager(this);
        }

        if (unit.moveAwayFrom(infantry, 0.2, Actions.MOVE_FORMATION)) {
            return usedManager(this, "Separate");
        }

        return null;
    }
}
