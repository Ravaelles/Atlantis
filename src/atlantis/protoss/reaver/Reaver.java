package atlantis.protoss.reaver;

import atlantis.architecture.Manager;
import atlantis.architecture.ManagerFactory;
import atlantis.combat.micro.avoid.special.AvoidLurkers;
import atlantis.units.AUnit;

public class Reaver extends Manager {
    public Reaver(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isReaver() && !unit.isLoaded();
    }

    @Override
    protected ManagerFactory[] managers() {
        return new ManagerFactory[]{
            ReaverProduceScarab::new,
            ReaverIsLoaded::new,

            ReaverUseTransport::new,
            AvoidLurkers::new,

            ReaverForceHoldToFireInRange::new,
            ReaverContinueAttack::new,
            ReaverHoldToAttack::new,

            ReaverForceFollowAnotherCombatUnit::new,

            ReaverAlwaysAttack::new,

            ReaverControlEnemyDistance::new,
            ReaverAlwaysFollowAlphaLeader::new,
        };
    }
}
