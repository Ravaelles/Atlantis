package atlantis.protoss.observer;

import atlantis.architecture.Manager;
import atlantis.map.position.HasPosition;
import atlantis.production.constructing.Construction;
import atlantis.production.constructing.ConstructionRequests;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;
import atlantis.util.Enemy;

import java.util.ArrayList;

public class DetectNewBasePotentiallyBlocked extends Manager {
    private HasPosition baseConstruction;

    public DetectNewBasePotentiallyBlocked(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (Enemy.protoss()) return false;

        ArrayList<Construction> basesNotStarted = ConstructionRequests.notStartedOfType(AUnitType.Protoss_Nexus);
        if (!basesNotStarted.isEmpty()) {
            baseConstruction = basesNotStarted.get(0).buildPosition();
        }

        return baseConstruction != null && isNearestObserverToConstructionPosition();
    }

    private boolean isNearestObserverToConstructionPosition() {
        AUnit nearestUnit = Select.ourOfType(AUnitType.Protoss_Observer).nearestTo(baseConstruction);

        return unit.equals(nearestUnit);
    }

    @Override
    public Manager handle() {
        if (unit.move(baseConstruction, Actions.MOVE_REVEAL, null)) {
            return usedManager(this, "DetectNewBasePotentiallyBlocked");
        }

        return null;
    }
}
