package atlantis.combat.squad.positioning.terran;

import atlantis.architecture.Manager;
import atlantis.combat.squad.positioning.*;
import atlantis.units.AUnit;
import atlantis.util.We;

public class TerranSquadCohesion extends Manager {
    public TerranSquadCohesion(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return We.terran() && unit.isGroundUnit() && !DoNotThinkOfImprovingCohesion.dontThink(unit);
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            TerranTooFarFromLeader.class,
            EnsureCoordinationWithTanks.class,
            TooClustered.class,
            TerranEnsureBall.class,
            TerranComeCloser.class,
            TooLowSquadCohesion.class,
        };
    }
}
