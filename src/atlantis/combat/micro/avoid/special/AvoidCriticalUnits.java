package atlantis.combat.micro.avoid.special;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;

public class AvoidCriticalUnits extends Manager {

    public AvoidCriticalUnits(AUnit unit) {
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
            AvoidLurkers.class,
            AvoidReavers.class,
            AvoidDT.class,
            AvoidGuardian.class,
        };
    }

}
