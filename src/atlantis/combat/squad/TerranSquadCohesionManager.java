package atlantis.combat.squad;

import atlantis.architecture.Manager;
import atlantis.combat.squad.positioning.ComeCloser;
import atlantis.combat.squad.positioning.DoNotThinkOfImprovingCohesion;
import atlantis.combat.squad.positioning.TooClustered;
import atlantis.combat.squad.positioning.TooLowSquadCohesion;
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
            TooClustered.class,
            TerranEnsureBall.class,
            ComeCloser.class,
            TooLowSquadCohesion.class,
        };
    }
}
