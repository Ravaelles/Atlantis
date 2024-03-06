package atlantis.production.dynamic.reinforce.protoss;

import atlantis.units.AUnit;
import atlantis.units.select.Count;

import static atlantis.units.AUnitType.Protoss_Photon_Cannon;

public class IsProtossBaseSecured {
    private final AUnit base;

    public IsProtossBaseSecured(AUnit base) {
        this.base = base;
    }

    public boolean needsSecuring() {
//        HasPosition nearTo = ABaseLocation.mineralsCenter(base);
        Count.clearCache();
        int cannonsNearby = Count.existingOrPlannedBuildingsNear(Protoss_Photon_Cannon, 10, base);

//        System.err.println(base + " cannons = " + cannonsNearby);

        return cannonsNearby == 0;
    }
}
