package atlantis.combat.advance;

import atlantis.architecture.Manager;
import atlantis.combat.missions.MissionManager;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class AdvanceStandard extends MissionManager {
    public AdvanceStandard(AUnit unit) {
        super(unit);
    }

    public Manager handle() {
        if (focusPoint != null) {
            if (unit.isTankSieged()) {
                unit.unsiege();
            } else {
                unit.move(focusPoint, Actions.MOVE_FOCUS, "LeaderAdvance");
            }
            return usedManager(this);
        }

        return null;
    }
}
