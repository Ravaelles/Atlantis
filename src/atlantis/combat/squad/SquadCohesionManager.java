package atlantis.combat.squad;

import atlantis.architecture.Manager;
import atlantis.combat.squad.positioning.ComeCloser;
import atlantis.combat.squad.positioning.DoNotThinkOfImprovingCohesion;
import atlantis.combat.squad.positioning.TooClustered;
import atlantis.combat.squad.positioning.TooLowSquadCohesion;
import atlantis.units.AUnit;

public class SquadCohesionManager extends Manager {
    public SquadCohesionManager(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isGroundUnit() && !DoNotThinkOfImprovingCohesion.dontThink(unit);
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[] {
            TooClustered.class,
            ComeCloser.class,
            TooLowSquadCohesion.class,
        };
    }
}
