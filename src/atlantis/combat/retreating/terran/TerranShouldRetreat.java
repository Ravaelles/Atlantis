package atlantis.combat.retreating.terran;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;

public class TerranShouldRetreat extends Manager {
    public TerranShouldRetreat(AUnit unit) {
        super(unit);
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            TerranFullRetreat.class,
        };
    }
}
