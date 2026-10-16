package atlantis.combat.retreating.terran;

import atlantis.architecture.Manager;
import atlantis.architecture.ManagerFactory;
import atlantis.units.AUnit;
import atlantis.util.We;

public class TerranRetreatManager extends Manager {
    public TerranRetreatManager(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return We.terran() && !unit.isLoaded();
    }

    @Override
    protected ManagerFactory[] managers() {
        return new ManagerFactory[]{
            TerranFullRetreat::new,
        };
    }
}
