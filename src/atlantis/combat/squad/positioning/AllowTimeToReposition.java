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
            && unit.lastPositioningActionLessThanAgo(allowedTime())
            && unit.enemiesNear().empty();
//            && unit.enemiesThatCanAttackMe(4).empty();
//            && unit.lastActionLessThanAgo(91)
//            && !unit.isLeader();
    }

    private int allowedTime() {
        return unit.enemiesNear().count() == 0 ? 50 : 10;
    }

    protected Manager handle() {
        return usedManager(this);
    }
}
