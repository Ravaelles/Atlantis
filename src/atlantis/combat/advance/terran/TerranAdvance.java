package atlantis.combat.advance.terran;

import atlantis.architecture.Manager;
import atlantis.architecture.ManagerFactory;
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
    protected ManagerFactory[] managers() {
        return new ManagerFactory[]{
            TerranTooFarFromLeader::new,
            TerranCloserToLeader::new,
            TerranAdvanceAsTank::new,
        };
    }
}
