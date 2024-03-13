package atlantis.combat.advance.focus;

import atlantis.architecture.Manager;
import atlantis.combat.missions.MissionManager;
import atlantis.units.AUnit;

public class HandleFocusPointPositioning extends MissionManager {
    public HandleFocusPointPositioning(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (unit.isSquadScout()) return false;

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
