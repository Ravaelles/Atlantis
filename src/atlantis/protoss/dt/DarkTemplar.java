package atlantis.protoss.dt;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;

public class DarkTemplar extends Manager {
    public DarkTemplar(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isDarkTemplar();
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            DarkTemplarRunWhenAttacked.class,
            DarkTemplarAvoidDetectors.class,
            DarkTemplarAvoidCB.class,
            DarkTemplarAlwaysAttackWhenUndetected.class,
            DarkTemplarIdle.class,
        };
    }
}
