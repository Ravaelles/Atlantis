package atlantis.terran;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;

public class ShouldLiftBuildingManager extends Manager {

    public ShouldLiftBuildingManager(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return !unit.isLifted() && unit.canLift() && unit.isWounded();
    }

    protected Manager handle() {
        if (unit.isUnderAttack(40) && unit.hpPercent() <= 38) {
            unit.lift();
            unit.setTooltip("LiftMeUp");
            return usedManager(this);
        }

        return null;
    }
}