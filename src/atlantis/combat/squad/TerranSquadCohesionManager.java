package atlantis.combat.squad;

import atlantis.architecture.Manager;
import atlantis.combat.squad.positioning.*;
import atlantis.combat.squad.positioning.terran.EnsureCoordinationWithTanks;
import atlantis.combat.squad.positioning.terran.TerranEnsureBall;
import atlantis.units.AUnit;
import atlantis.util.We;

public class TerranSquadCohesionManager extends Manager {
    public TerranSquadCohesionManager(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return We.terran() && unit.isGroundUnit() && !DoNotThinkOfImprovingCohesion.dontThink(unit);
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            TooFarFromLeader.class,
            EnsureCoordinationWithTanks.class,
            TooClustered.class,
            TerranEnsureBall.class,
            ComeCloser.class,
            TooLowSquadCohesion.class,
        };
    }
}
