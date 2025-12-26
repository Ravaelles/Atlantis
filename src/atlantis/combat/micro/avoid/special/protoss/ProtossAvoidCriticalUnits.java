package atlantis.combat.micro.avoid.special.protoss;

import atlantis.architecture.Manager;
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
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            SuicideAgainstScarabs.class,
            AvoidTanksSieged.class,
            AvoidLurkers.class,
            AvoidReavers.class,
            AvoidDT.class,
            AvoidGuardian.class,
        };
    }

}
