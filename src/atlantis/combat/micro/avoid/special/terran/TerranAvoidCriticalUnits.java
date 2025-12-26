package atlantis.combat.micro.avoid.special.terran;

import atlantis.architecture.Manager;
import atlantis.combat.micro.avoid.special.*;
import atlantis.units.AUnit;

public class TerranAvoidCriticalUnits extends Manager {

    public TerranAvoidCriticalUnits(AUnit unit) {
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
