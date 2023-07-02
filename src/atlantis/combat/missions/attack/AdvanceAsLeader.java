package atlantis.combat.missions.attack;

import atlantis.combat.missions.focus.AFocusPoint;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class AdvanceAsLeader {

    protected static boolean advanceAsLeader(AUnit unit, AFocusPoint focusPoint) {
        if (unit.squad().cohesionPercent() <= 75) {
            return unit.holdPosition("LeaderWait");
        }

        unit.move(focusPoint, Actions.MOVE_FOCUS, "LeaderAdvance");
        return true;
    }
}