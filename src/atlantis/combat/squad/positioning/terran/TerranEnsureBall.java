package atlantis.combat.squad.positioning.terran;

import atlantis.architecture.Manager;
import atlantis.combat.squad.positioning.HugTanks;
import atlantis.units.AUnit;

public class TerranEnsureBall extends Manager {
    public TerranEnsureBall(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isTerranInfantry() && unit.friendsNear().groundUnits().inRadius(2, unit).atMost(7);
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            TooFarFromMedic.class,
            HugTanks.class,
        };
    }
}
