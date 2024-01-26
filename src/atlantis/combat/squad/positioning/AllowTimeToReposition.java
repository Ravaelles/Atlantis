package atlantis.combat.squad.positioning;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;

public class AllowTimeToReposition extends Manager {
    public AllowTimeToReposition(AUnit unit) {
        super(unit);
    }

    protected Manager handle() {
        if (!unit.looksIdle() && unit.isMoving() && unit.lastPositioningActionLessThanAgo(13)) {
            return usedManager(this);
        }

        return null;
    }
}
