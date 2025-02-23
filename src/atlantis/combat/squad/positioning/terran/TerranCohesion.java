package atlantis.combat.squad.positioning.terran;

import atlantis.architecture.Manager;
import atlantis.combat.squad.positioning.*;
import atlantis.combat.squad.positioning.terran.formation.TerranFormation;
import atlantis.units.AUnit;
import atlantis.util.We;

public class TerranCohesion extends Manager {
    public TerranCohesion(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return We.terran()
            && unit.isGroundUnit();
//            && !DoNotThinkOfImprovingCohesion.dontThink(unit);
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            TerranFormation.class,

            TerranTooFarFromLeader.class,
            TerranEnsureCoordinationWithTanks.class,
            TerranTooClustered.class,
            TerranEnsureBall.class,
            TerranComeCloser.class,
            TooLowSquadCohesion.class,
        };
    }
}
