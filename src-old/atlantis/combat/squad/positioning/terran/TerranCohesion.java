package atlantis.combat.squad.positioning.terran;

import atlantis.architecture.Manager;
import atlantis.architecture.ManagerFactory;
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
    protected ManagerFactory[] managers() {
        return new ManagerFactory[]{
            TerranFormation::new,

            TerranTooFarFromLeader::new,
            TerranEnsureCoordinationWithTanks::new,
            TerranTooClustered::new,
            TerranEnsureBall::new,
            TerranComeCloser::new,
            TooLowSquadCohesion::new,
        };
    }
}
