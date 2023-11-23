package atlantis.combat.missions.defend.focus.terran;

import atlantis.combat.advance.focus.AFocusPoint;
import atlantis.combat.missions.defend.focus.MissionDefendFocus;
import atlantis.map.high.FindHighGround;
import atlantis.map.position.APosition;
import atlantis.units.select.Select;

public class TanksMissionDefendFocus extends MissionDefendFocus {
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
        AFocusPoint focus = (new MissionDefendFocus()).focusPoint();

        APosition highGround = FindHighGround.findNear(focus, 8);

        if (highGround == null) return null;
        else return new AFocusPoint(highGround, Select.main(), "HiGround");
    }
}
