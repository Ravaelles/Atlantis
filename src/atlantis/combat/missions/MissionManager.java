package atlantis.combat.missions;

import atlantis.architecture.Manager;
import atlantis.combat.advance.focus.AFocusPoint;
import atlantis.units.AUnit;

public class MissionManager extends Manager {
    protected Mission mission;
    protected AFocusPoint focusPoint;

    public MissionManager(AUnit unit) {
        super(unit);
        mission = unit.mission();
        focusPoint = mission.focusPoint();
    }
}
