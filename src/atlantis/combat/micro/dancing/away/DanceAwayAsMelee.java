package atlantis.combat.micro.dancing.away;

import atlantis.architecture.Manager;
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
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            DanceAwayAsZealot.class,
        };
    }
}
