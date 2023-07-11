package atlantis.combat.missions.attack;

import atlantis.combat.missions.MissionManager;
import atlantis.combat.missions.focus.AFocusPoint;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.architecture.Manager;

public class AdvanceAsLeader extends MissionManager {

    public AdvanceAsLeader(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return squad.isLeader(unit);
    }

    @Override
    public Manager handle() {
        if (unit.squad().cohesionPercent() <= 75) {
            unit.holdPosition("LeaderWait");
            return usedManager(this);
        }

        unit.move(focus, Actions.MOVE_FOCUS, "LeaderAdvance");
        return usedManager(this);
    }
}