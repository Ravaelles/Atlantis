package atlantis.combat.micro.terran.lifted;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;

public class LandBuildingThatLifted extends Manager {
    public LandBuildingThatLifted(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return false;
//        return unit.isLifted() && unit.isBase();
    }

    public Manager handle() {
        if (land()) {
            return usedManager(this);
        }

        return null;
    }

    private boolean land() {
        return false;
    }
}
