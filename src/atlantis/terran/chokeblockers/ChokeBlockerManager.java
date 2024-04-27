package atlantis.terran.chokeblockers;

import atlantis.architecture.Manager;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;

public class ChokeBlockerManager extends Manager {
    public ChokeBlockerManager(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (!NeedChokeBlockers.check()) return false;

        return unit.enemiesNear().notEmpty()
            || unit.friendsNear().nonBuildings().inRadius(8, unit).atMost(14);
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            ChokeBlockerRepairOther.class,
            ChokeBlockerMoveAway.class,
            ChokeBlockerFight.class,
            ChokeBlockerMoveToBlock.class,
        };
    }
}
