package atlantis.combat.squad.positioning.protoss;

import atlantis.architecture.Manager;
import atlantis.combat.squad.positioning.too_lonely.ProtossTooFarFromLeader;
import atlantis.combat.squad.positioning.too_lonely.ProtossTooLonely;
import atlantis.combat.squad.positioning.too_lonely.ProtossTooLonelyGetCloser;
import atlantis.units.AUnit;
import atlantis.util.We;

public class ProtossSquadCohesion extends Manager {
    public ProtossSquadCohesion(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return We.protoss();
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            ProtossTooLonely.class,
        };
    }
}
