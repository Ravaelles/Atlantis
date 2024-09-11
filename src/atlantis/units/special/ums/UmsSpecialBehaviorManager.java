package atlantis.units.special.ums;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.units.AUnit;

public class UmsSpecialBehaviorManager extends Manager {
    public UmsSpecialBehaviorManager(AUnit unit) {
        super(unit);
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            GoToNeutralNewCompanions.class,
            GoToBeacons.class,
        };
    }

    @Override
    public boolean applies() {
        return A.isUms();
    }
}
