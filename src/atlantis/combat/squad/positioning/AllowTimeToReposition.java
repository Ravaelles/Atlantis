package atlantis.combat.squad.positioning;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;

public class AllowTimeToReposition extends Manager {
    public AllowTimeToReposition(AUnit unit) {
        super(unit);
    }

    protected Manager handle() {
        if (unit.isMoving() && unit.lastPositioningActionLessThanAgo(13) && !unit.isLeader()) {
            return usedManager(this);
        }

        return null;
    }
}
