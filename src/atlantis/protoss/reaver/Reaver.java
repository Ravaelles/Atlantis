package atlantis.protoss.reaver;

import atlantis.architecture.Manager;
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
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            ReaverProduceScarab.class,
            ReaverIsLoaded.class,

            ReaverUseTransport.class,
            AvoidLurkers.class,

            ReaverForceHoldToFireInRange.class,
            ReaverContinueAttack.class,
            ReaverHoldToAttack.class,

            ReaverForceFollowAnotherCombatUnit.class,

            ReaverAlwaysAttack.class,

            ReaverControlEnemyDistance.class,
            ReaverAlwaysFollowAlphaLeader.class,
        };
    }
}
