package atlantis.combat.retreating.protoss;

import atlantis.architecture.Manager;
import atlantis.combat.retreating.protoss.should.ProtossDontRetreat;
import atlantis.combat.retreating.protoss.should.ProtossShouldRetreat;
import atlantis.units.AUnit;
import atlantis.util.We;

public class ProtossRetreat extends Manager {
    public ProtossRetreat(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return We.protoss()
            && !(new ProtossDontRetreat(unit)).applies();
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            ProtossShouldRetreat.class,
        };
    }
}
