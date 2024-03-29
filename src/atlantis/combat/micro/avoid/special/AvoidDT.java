package atlantis.combat.micro.avoid.special;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;

public class AvoidDT extends Manager {

    public AvoidDT(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isGroundUnit();
    }

    @Override
    public Manager handle() {
        if (unit.isAir() || unit.isABuilding()) {
            return null;
        }

        AUnit dt = unit.enemiesNear().ofType(AUnitType.Protoss_Dark_Templar).effUndetected()
            .inRadius(2.5, unit).nearestTo(unit);
        if (dt == null) {
            return null;
        }

        unit.runningManager().runFromAndNotifyOthersToMove(dt, "DT!");
        return usedManager(this);
    }
}