package atlantis.combat.micro.terran.infantry.medic;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;

public class TooFarFromNearestInfantry extends Manager {
    private AUnit infantry;

    public TooFarFromNearestInfantry(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        infantry = Select.ourTerranInfantryWithoutMedics().nearestTo(unit);
        if (infantry != null && infantry.distToMoreThan(unit, 4)) {
            return true;
        }

        return false;
    }

    @Override
    public Manager handle() {
        unit.move(infantry, Actions.MOVE_FOCUS, "SemperFi", false);
        return usedManager(this);
    }
}
