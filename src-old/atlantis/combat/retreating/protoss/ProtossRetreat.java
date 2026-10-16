package atlantis.combat.retreating.protoss;

import atlantis.architecture.Manager;
import atlantis.architecture.ManagerFactory;
import atlantis.combat.micro.early.protoss.ZealotAvoidLingsWhenWounded;
import atlantis.combat.retreating.protoss.should.ProtossDontRetreat;
import atlantis.combat.retreating.protoss.should.ProtossRetreatWrapper;
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
    protected ManagerFactory[] managers() {
        return new ManagerFactory[]{
            ProtossRetreatWrapper::new,

            ZealotAvoidLingsWhenWounded::new,
        };
    }
}
