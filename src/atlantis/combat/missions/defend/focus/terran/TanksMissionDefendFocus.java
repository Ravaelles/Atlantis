package atlantis.combat.missions.defend.focus.terran;

import atlantis.combat.advance.focus.AFocusPoint;
import atlantis.combat.missions.defend.focus.MissionDefendFocusPoint;
import atlantis.map.high.FindHighGround;
import atlantis.map.position.APosition;
import atlantis.units.select.Select;

public class TanksMissionDefendFocus extends MissionDefendFocusPoint {
    @Override
    public AFocusPoint focusPoint() {
        if (focusPoint().isAroundChoke()) {
            AFocusPoint highGround = aroundHighGround();
            if (highGround != null) {
                return highGround;
            }
        }

        return super.focusPoint();
    }

    protected AFocusPoint aroundHighGround() {
        AFocusPoint focus = (new MissionDefendFocusPoint()).focusPoint();

        APosition highGround = FindHighGround.findNear(focus, 8);

        if (highGround == null) return null;
        else return new AFocusPoint(highGround, Select.main(), "HiGround");
    }
}
