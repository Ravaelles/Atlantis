package atlantis.combat.micro.terran.tank;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;

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
            TerranTankWhenNotSieged.class,
            TerranTankWhenSieged.class,
        };
    }
}
