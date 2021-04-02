package atlantis.combat.missions;

import atlantis.map.AChokepoint;
import atlantis.position.APosition;

public abstract class MissionFocusPointManager {

    public abstract APosition focusPoint();

    /**
     * Makes sense only for certain missions e.g. CONTAIN.
     */
    public AChokepoint getChokepoint() {
        return null;
    }

}
