package atlantis.combat.squad.positioning.terran;

import atlantis.architecture.Manager;
import atlantis.combat.squad.positioning.GoBehindLineOfTanks;
import atlantis.combat.squad.positioning.TooFarFromTank;
import atlantis.units.AUnit;

public class EnsureCoordinationWithTanks extends Manager {
    public EnsureCoordinationWithTanks(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isGroundUnit() && unit.isCombatUnit() && !unit.isTank();
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            GoBehindLineOfTanks.class,
            TooFarFromTank.class,
        };
    }
}
