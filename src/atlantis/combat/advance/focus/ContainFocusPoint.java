package atlantis.combat.advance.focus;

import atlantis.architecture.Manager;
import atlantis.combat.missions.MissionManager;
import atlantis.units.AUnit;

public class ContainFocusPoint extends MissionManager {
    public ContainFocusPoint(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.enemiesNear().inRadius(10, unit).empty();
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            OnWrongSideOfFocusPoint.class,
            TooFarFromFocusPoint.class,
            TooCloseToFocusPoint.class,
        };
    }
}
