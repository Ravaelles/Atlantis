package atlantis.units.select;

import atlantis.map.position.HasPosition;
import atlantis.production.constructing.ConstructionRequests;
import atlantis.production.orders.ProductionQueue;
import atlantis.units.AUnitType;

public class Have {

    protected static boolean have(AUnitType type) {
        return Count.ofType(type) > 0;
    }

    public static boolean a(AUnitType type) {
        return Count.includingPlanned(type) > 0;
    }

    public static boolean no(AUnitType type) {
        return Count.includingPlanned(type) == 0;
    }

    public static boolean notEvenInPlans(AUnitType type) {
        return Count.includingPlanned(type) == 0;
    }

    public static boolean existingOrPlanned(AUnitType building, HasPosition position, double inRadius) {
        assert building.isBuilding();

        if (ConstructionRequests.hasNotStartedNear(building, position, inRadius)) {
            return true;
        }

        return Select.ourOfTypeIncludingUnfinished(building).inRadius(inRadius, position).atLeast(1);
    }

    public static boolean existingOrPlannedOrInQueue(AUnitType building, HasPosition position, double inRadius) {
        assert building.isBuilding();

        if (ProductionQueue.isAtTheTopOfQueue(building, 2)) {
            return true;
        }

        if (ConstructionRequests.hasNotStartedNear(building, position, inRadius)) {
            return true;
        }

        return Select.ourOfTypeIncludingUnfinished(building).inRadius(inRadius, position).atLeast(1);
    }

    // =========================================================

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

    public static boolean main() {
        return base();
    }

    public static boolean factory() {
        return Count.ofType(AUnitType.Terran_Factory) > 0;
    }

    public static boolean machineShop() {
        return have(AUnitType.Terran_Factory);
    }
}
