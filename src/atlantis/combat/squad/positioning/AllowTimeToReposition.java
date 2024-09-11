package atlantis.combat.squad.positioning;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;

public class AllowTimeToReposition extends Manager {
    public AllowTimeToReposition(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (true) return false; // @ToDo

        return unit.isMoving()
            && unit.lastPositioningActionLessThanAgo(13)
            && unit.lastActionLessThanAgo(91)
            && unit.enemiesNear().empty()
            && !unit.isLeader();
    }

    protected Manager handle() {
        if (unit.isMoving()) return usedManager(this);

        return null;
    }
}
