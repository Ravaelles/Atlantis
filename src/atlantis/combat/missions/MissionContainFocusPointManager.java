package atlantis.combat.missions;

import atlantis.map.AChokepoint;
import atlantis.map.AMap;
import atlantis.position.APosition;

public class MissionContainFocusPointManager extends MissionFocusPointManager {

    @Override
    public APosition focusPoint() {
        return getChokepoint().getCenter();
    }

    public AChokepoint getChokepoint() {
        APosition attackFocusPoint = MissionAttackFocusPoint.focusPoint();
        if (attackFocusPoint == null) {
            return null;
        }

        return AMap.getNearestChokepoint(attackFocusPoint);
    }

    private static APosition couldNotDefineFocusPoint() {
        return MissionAttackFocusPoint.focusPoint();
    }

}
