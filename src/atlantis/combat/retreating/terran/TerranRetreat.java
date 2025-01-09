package atlantis.combat.retreating.terran;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.util.We;

public class TerranRetreat extends Manager {
    public TerranRetreat(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return We.terran() && !unit.isLoaded();
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            TerranFullRetreat.class,
        };
    }
}
