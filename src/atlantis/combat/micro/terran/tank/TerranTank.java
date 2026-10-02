package atlantis.combat.micro.terran.tank;

import atlantis.architecture.Manager;
import atlantis.architecture.ManagerFactory;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class TerranTank extends Manager {
    public TerranTank(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isTank();
    }

    @Override
    protected ManagerFactory[] managers() {
        return new ManagerFactory[]{
            TerranTankWhenUnsieged::new,
            TerranTankWhenSieged::new,
        };
    }

    public static boolean wantsToUnsiege(AUnit unit) {
        if (unit.lastActionLessThanAgo(30 * (6 + unit.id() % 3), Actions.SIEGE) || unit.hasCooldown()) return false;
        if (unit.lastActionLessThanAgo(30 * (12 + unit.id() % 4), Actions.UNSIEGE)) return false;

        unit.unsiege();
        return true;
    }

    public static boolean forceUnsiege(AUnit unit) {
        unit.unsiege();
        return true;
    }
}
