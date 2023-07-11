package atlantis.combat.missions;

import atlantis.architecture.Manager;
import atlantis.combat.missions.focus.AFocusPoint;
import atlantis.units.AUnit;

public class MissionManager extends Manager {

    protected Mission mission;
    protected AFocusPoint focus;

    public MissionManager(AUnit unit) {
        super(unit);
        mission = unit.mission();
        focus = mission.focusPoint();
    }
}
