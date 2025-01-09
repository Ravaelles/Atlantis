package atlantis.protoss.reaver;

import atlantis.architecture.Manager;
import atlantis.combat.micro.avoid.special.AvoidLurkers;
import atlantis.units.AUnit;

public class ProtossReaver extends Manager {
    public ProtossReaver(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isReaver();
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            ReaverProduceScarab.class,
            ReaverIsLoaded.class,

            ReaverUseTransport.class,
            AvoidLurkers.class,

            ReaverContinueAttack.class,

            ReaverHoldToAttack.class,
            ReaverAlwaysAttack.class,

            ReaverControlEnemyDistance.class,
            ReaverAlwaysFollowAlphaLeader.class,
        };
    }
}
