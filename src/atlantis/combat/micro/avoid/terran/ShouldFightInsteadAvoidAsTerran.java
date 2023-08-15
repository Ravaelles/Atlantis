package atlantis.combat.micro.avoid.terran;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;

public class ShouldFightInsteadAvoidAsTerran extends Manager {
    public ShouldFightInsteadAvoidAsTerran(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isTerran();
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            ShouldFightInsteadAvoidAsWraith.class,
        };
    }
}