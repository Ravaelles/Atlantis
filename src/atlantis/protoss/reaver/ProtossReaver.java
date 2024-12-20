package atlantis.protoss.reaver;

import atlantis.architecture.Manager;
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

            ReaverContinueAttack.class,

            ReaverUseTransport.class,

            ReaverHoldToAttack.class,
            ReaverAlwaysAttack.class,

            ReaverControlEnemyDistance.class,
            ReaverAlwaysFollowAlphaLeader.class,
        };
    }
}
