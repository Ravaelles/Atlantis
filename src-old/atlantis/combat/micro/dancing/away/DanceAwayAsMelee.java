package atlantis.combat.micro.dancing.away;

import atlantis.architecture.Manager;
import atlantis.architecture.ManagerFactory;
import atlantis.combat.micro.dancing.away.protoss.DanceAwayAsZealot;
import atlantis.units.AUnit;

public class DanceAwayAsMelee extends Manager {
    public DanceAwayAsMelee(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isMelee();
    }

    @Override
    protected ManagerFactory[] managers() {
        return new ManagerFactory[]{
            DanceAwayAsZealot::new,
        };
    }
}
