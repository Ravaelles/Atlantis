package atlantis.protoss.dt;

import atlantis.architecture.Manager;
import atlantis.architecture.ManagerFactory;
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
    protected ManagerFactory[] managers() {
        return new ManagerFactory[]{
            DarkTemplarRunWhenAttacked::new,
            DarkTemplarAvoidWhenUnderAttack::new,
            DarkTemplarAvoidDetectors::new,
            DarkTemplarAvoidCB::new,
            DarkTemplarAlwaysAttackWhenUndetected::new,
            DarkTemplarIdle::new,
        };
    }
}
