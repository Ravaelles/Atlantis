package atlantis.production.dynamic.terran.bunker;

import atlantis.architecture.Commander;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;

public abstract class HaveBunkerAt extends Commander {
    protected boolean bunkerExistsAtPosition() {
        if (Count.existingOrPlannedBuildingsNear(AUnitType.Terran_Bunker, 8, atPosition()) > 0) return true;

        return false;
    }

    protected abstract HasPosition atPosition();

    @Override
    protected boolean handle() {
        if ((new ReinforceWithBunkerAtNearestChoke(atPosition())).invokedCommander()) {
            return true;
        }

        return false;
    }
}
