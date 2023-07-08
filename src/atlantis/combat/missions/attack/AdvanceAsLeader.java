package atlantis.combat.missions.attack;

import atlantis.combat.missions.focus.AFocusPoint;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.architecture.Manager;

public class AdvanceAsLeader extends Manager {

    public AdvanceAsLeader(AUnit unit) {
        super(unit);
    }

    protected boolean advanceAsLeader(AFocusPoint focusPoint) {
        if (unit.squad().cohesionPercent() <= 75) {
            return unit.holdPosition("LeaderWait");
        }

        unit.move(focusPoint, Actions.MOVE_FOCUS, "LeaderAdvance");
        return true;
    }
}