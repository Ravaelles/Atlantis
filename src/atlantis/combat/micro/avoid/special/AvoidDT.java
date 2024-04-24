package atlantis.combat.micro.avoid.special;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;

public class AvoidDT extends Manager {
    public AvoidDT(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isGroundUnit() && !unit.isABuilding();
    }

    @Override
    protected Manager handle() {
        if (unit.isMoving()) {
            if (unit.isRunning()) return null;
            if (unit.lastActionLessThanAgo(6, Actions.RUN_ENEMY)) return null;
        }

        AUnit dt = unit.enemiesNear().ofType(AUnitType.Protoss_Dark_Templar)
            .inRadius(2.4 + unit.woundPercent() / 110.0, unit)
            .effUndetected()
            .nearestTo(unit);

        if (dt == null) return null;

        unit.runningManager().runFromAndNotifyOthersToMove(dt, "DT!");
        return usedManager(this);
    }
}
