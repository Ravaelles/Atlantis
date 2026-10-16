package atlantis.combat.squad.positioning.terran;

import atlantis.architecture.Manager;
import atlantis.architecture.ManagerFactory;
import atlantis.combat.squad.positioning.GoBehindLineOfTanks;
import atlantis.combat.squad.positioning.TooFarFromTank;
import atlantis.units.AUnit;

public class TerranEnsureCoordinationWithTanks extends Manager {
    public TerranEnsureCoordinationWithTanks(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isGroundUnit() && unit.isCombatUnit() && !unit.isTank();
    }

    @Override
    protected ManagerFactory[] managers() {
        return new ManagerFactory[]{
            GoBehindLineOfTanks::new,
            TooFarFromTank::new,
        };
    }
}
