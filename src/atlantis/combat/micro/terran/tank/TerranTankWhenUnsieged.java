package atlantis.combat.micro.terran.tank;

import atlantis.architecture.Manager;
import atlantis.combat.micro.terran.tank.sieging.ThinkOfSieging;
import atlantis.terran.repair.UnitBeingReparedManager;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class TerranTankWhenUnsieged extends Manager {
    public TerranTankWhenUnsieged(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (!unit.isTankUnsieged() || !TankDecisions.siegeResearched()) return false;

        if (recentlySiegedOrUngieged()) return false;

        return true;
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            UnitBeingReparedManager.class,
            ThinkOfSieging.class,
        };
    }

    private boolean recentlySiegedOrUngieged() {
        return unit.lastActionLessThanAgo(30 * (4 + (unit.id() % 4)), Actions.UNSIEGE)
            || unit.lastActionLessThanAgo(30 * (4 + (unit.id() % 4)), Actions.SIEGE);
    }
}
