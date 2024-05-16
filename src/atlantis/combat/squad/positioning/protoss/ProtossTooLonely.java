package atlantis.combat.squad.positioning.protoss;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.util.We;

public class ProtossTooLonely extends Manager {
    public ProtossTooLonely(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return We.protoss()
            && unit.isCombatUnit();
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            ProtossZealotTooFarFromDragoon.class,
            ProtossTooFarFromLeader.class,
        };
    }
}
