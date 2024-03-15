package atlantis.combat.retreating.protoss;

import atlantis.architecture.Manager;
import atlantis.combat.retreating.protoss.should.ProtossShouldNotRetreat;
import atlantis.combat.retreating.protoss.should.ProtossShouldRetreat;
import atlantis.units.AUnit;
import atlantis.util.We;

public class ProtossRetreat extends Manager {
    public ProtossRetreat(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return We.protoss();
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            ProtossShouldNotRetreat.class,
            ProtossShouldRetreat.class,
        };
    }
}
