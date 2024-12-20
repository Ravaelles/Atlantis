package atlantis.protoss.reaver;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import bwapi.Color;

public class ReaverIsLoaded extends Manager {
    public ReaverIsLoaded(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return false;
//        return unit.isLoaded();
    }

    @Override
    public Manager handle() {
        unit.paintCircleFilled(9, Color.Brown);
        return usedManager(this);
    }
}
