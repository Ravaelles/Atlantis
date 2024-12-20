package atlantis.combat.advance.terran;

import atlantis.architecture.Manager;
import atlantis.combat.advance.tank.TerranAdvanceAsTank;
import atlantis.combat.squad.positioning.terran.TerranTooFarFromLeader;
import atlantis.units.AUnit;
import atlantis.util.We;

public class TerranAdvance extends Manager {
    public TerranAdvance(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return We.terran() && unit.mission().focusPoint() != null;
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            TerranTooFarFromLeader.class,
            TerranCloserToLeader.class,
            TerranAdvanceAsTank.class,
        };
    }
}
