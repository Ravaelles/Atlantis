package atlantis.units.special.ums;

import atlantis.architecture.Manager;
import atlantis.architecture.ManagerFactory;
import atlantis.game.A;
import atlantis.units.AUnit;

public class UmsSpecialBehaviorManager extends Manager {
    public UmsSpecialBehaviorManager(AUnit unit) {
        super(unit);
    }

    @Override
    protected ManagerFactory[] managers() {
        return new ManagerFactory[]{
            GoToNeutralNewCompanions::new,
            GoToBeacons::new,
        };
    }

    @Override
    public boolean applies() {
        return A.isUms();
    }
}
