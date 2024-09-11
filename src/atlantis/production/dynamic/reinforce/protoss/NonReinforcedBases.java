package atlantis.production.dynamic.reinforce.protoss;

import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Select;

public class NonReinforcedBases {
    public static HasPosition getNonReinforcedBase() {
        if (Count.basesWithUnfinished() <= minCannons()) return null;

        for (AUnit unit : Select.ourBuildingsWithUnfinished().list()) {
            if (!isReinforced(unit)) return unit;
        }

        return null;
    }

    private static boolean isReinforced(AUnit unit) {
        return Select.ourBuildings().ofType(AUnitType.Protoss_Photon_Cannon).inRadius(8, unit).count() >= minCannons();
    }

    private static int minCannons() {
        return 1;
    }
}
