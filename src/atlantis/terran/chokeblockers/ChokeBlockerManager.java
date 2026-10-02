package atlantis.terran.chokeblockers;

import atlantis.architecture.Manager;
import atlantis.architecture.ManagerFactory;
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
    protected ManagerFactory[] managers() {
        return new ManagerFactory[]{
            ChokeBlockerRunAsProtoss::new,
            ChokeBlockerRepairOther::new,
            ChokeBlockerMoveAway::new,
            ChokeBlockerFight::new,
            ChokeBlockerMoveToBlock::new,
        };
    }
}
