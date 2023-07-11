package atlantis.combat.micro.avoid.special;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;

public class AvoidLurkers extends Manager {

    public AvoidLurkers(AUnit unit) {
        super(unit);
    }

    @Override
    public Manager handle() {
        if (unit.isAir() || unit.isBuilding()) {
            return null;
        }

        AUnit lurker = unit.enemiesNear().lurkers().effUndetected().inRadius(7.7, unit).nearestTo(unit);
        if (lurker == null) {
            return null;
        }

        unit.runningManager().runFromAndNotifyOthersToMove(lurker, "LURKER!");
        return usedManager(this);
    }
}