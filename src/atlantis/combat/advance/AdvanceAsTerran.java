package atlantis.combat.advance;

import atlantis.architecture.Manager;
import atlantis.combat.advance.tank.AdvanceAsTank;
import atlantis.combat.squad.positioning.terran.TerranTooFarFromLeader;
import atlantis.units.AUnit;

public class AdvanceAsTerran extends Manager {
    public AdvanceAsTerran(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isTerran() && unit.mission().focusPoint() != null;
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            TerranTooFarFromLeader.class,
            CloserToLeader.class,
            AdvanceAsTank.class,
        };
    }
}
