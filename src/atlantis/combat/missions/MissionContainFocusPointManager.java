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
        APosition attackFocusPoint = focusPoint();
        if (attackFocusPoint == null) {
            return null;
        }

        return AMap.getNearestChokepoint(attackFocusPoint);
    }

    private APosition couldNotDefineFocusPoint() {
        return focusPoint();
    }

}
