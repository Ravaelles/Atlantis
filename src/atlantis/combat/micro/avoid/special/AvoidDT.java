package atlantis.combat.micro.avoid.special;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Selection;

public class AvoidDT extends Manager {

    public AvoidDT(AUnit unit) {
        super(unit);
    }

    @Override
    public Manager handle() {
        if (unit.isAir() || unit.isBuilding()) {
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