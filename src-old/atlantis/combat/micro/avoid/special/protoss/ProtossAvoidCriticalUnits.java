package atlantis.combat.micro.avoid.special.protoss;

import atlantis.architecture.Manager;
import atlantis.architecture.ManagerFactory;
import atlantis.combat.micro.avoid.special.*;
import atlantis.units.AUnit;

public class ProtossAvoidCriticalUnits extends Manager {

    public ProtossAvoidCriticalUnits(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.enemiesNear().notEmpty();
    }

    @Override
    protected ManagerFactory[] managers() {
        return new ManagerFactory[]{
            SuicideAgainstScarabs::new,
            AvoidTanksSieged::new,
            AvoidLurkers::new,
            AvoidReavers::new,
            AvoidDT::new,
            AvoidGuardian::new,
        };
    }

}
