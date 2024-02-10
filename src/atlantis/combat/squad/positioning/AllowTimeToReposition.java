package atlantis.combat.squad.positioning;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;

public class AllowTimeToReposition extends Manager {
    public AllowTimeToReposition(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isMoving()
            && unit.lastPositioningActionLessThanAgo(13)
            && unit.lastActionLessThanAgo(91)
            && unit.enemiesNear().empty()
            && !unit.isLeader();
    }

    protected Manager handle() {
        return usedManager(this);
    }
}
