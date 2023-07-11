package atlantis.combat.squad.positioning.terran;

import atlantis.combat.squad.positioning.ComeCloserToTanks;
import atlantis.units.AUnit;
import atlantis.architecture.Manager;

public class TerranInfantryComeCloser extends Manager {
    public TerranInfantryComeCloser(AUnit unit) {
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
            ComeCloserToTanks.class,
        };
    }
}
