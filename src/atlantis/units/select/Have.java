package atlantis.units.select;

import atlantis.position.APosition;
import atlantis.production.constructing.AConstructionRequests;
import atlantis.units.AUnitType;

public class Have {

    public static boolean armory() {
        return Count.ofType(AUnitType.Terran_Armory) > 0;
    }

    public static boolean base() {
        return Select.main() != null;
    }

    public static boolean engBay() {
        return Count.ofType(AUnitType.Terran_Engineering_Bay) > 0;
    }

    public static boolean barracks() {
        return Count.ofType(AUnitType.Terran_Barracks) > 0;
    }

    public static boolean existingOrPlanned(AUnitType building, APosition point, double inRadius) {
        assert building.isBuilding();

        if (AConstructionRequests.hasNotStartedConstructionNear(building, point, inRadius)) {
            return true;
        }

        return Select.ourOfTypeIncludingUnfinished(building).inRadius(inRadius, point).atLeast(1);
    }
}
