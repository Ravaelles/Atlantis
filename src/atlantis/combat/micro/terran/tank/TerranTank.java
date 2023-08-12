package atlantis.combat.micro.terran.tank;

import atlantis.architecture.Manager;
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
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            TerranTankWhenUnsieged.class,
            TerranTankWhenSieged.class,
        };
    }

    public static boolean wantsToUnsiege(AUnit unit) {
        if (unit.lastActionLessThanAgo(30 * (6 + unit.id() % 3), Actions.SIEGE) || unit.hasCooldown()) return false;
        if (unit.lastActionLessThanAgo(30 * (12 + unit.id() % 4), Actions.UNSIEGE)) return false;

        unit.unsiege();
        return true;
    }
}
