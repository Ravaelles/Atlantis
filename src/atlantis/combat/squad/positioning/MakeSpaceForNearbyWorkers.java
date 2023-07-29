package atlantis.combat.squad.positioning;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;

public class MakeSpaceForNearbyWorkers extends Manager {
    public MakeSpaceForNearbyWorkers(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (unit.enemiesNearInRadius(12) > 0) {
            return false;
        }

        return true;
    }

    public Manager handle() {
        AUnit nearWorker = Select.ourWorkers().inRadius(1.5, unit).first();

        if (nearWorker != null) {
            if (unit.isTankSieged()) {
                unit.unsiege();
            } else {
                unit.move(Select.main(), Actions.MOVE_SPACE, "Space4Worker");
            }
            return usedManager(this);
        }

        return null;
    }
}
