package atlantis.combat.micro.avoid.special;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;

public class AvoidGuardian extends Manager {

    public AvoidGuardian(AUnit unit) {
        super(unit);
    }

    @Override
    public Manager handle() {
        return null;

//        if (unit.isAir() || unit.isBuilding() || unit.canAttackAirUnits()) {
//            return null;
//        }
//
//        AUnit guardian = unit.enemiesNear().guardians().effUndetected().inRadius(7.7, unit).nearestTo(unit);
//        if (guardian == null) {
//            return null;
//        }
//
//        unit.runningManager().runFromAndNotifyOthersToMove(guardian, "LURKER!");
//        return usedManager(this);
    }
}