package atlantis.battle.avoid.terran.fight;

import atlantis.architecture.Manager;
import atlantis.architecture.ManagerFactory;
import atlantis.battle.avoid.terran.fight.*;
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
    protected ManagerFactory[] managers() {
        return new ManagerFactory[]{
            TerranFightInsteadAvoidAsFirebat::new,
            TerranFightInsteadAvoidAsAir::new,
            TerranFightInsteadAvoidAsStandard::new,
        };
    }
}
