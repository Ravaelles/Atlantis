package atlantis.units.select;

import atlantis.map.position.HasPosition;
import atlantis.production.constructing.ConstructionRequests;
import atlantis.production.orders.production.ProductionQueue;
import atlantis.units.AUnitType;

public class Have {

    protected static boolean have(AUnitType type) {
        return Count.ofType(type) > 0;
    }

    public static boolean a(AUnitType type) {
        return Count.WithPlanned(type) > 0;
    }

    public static boolean no(AUnitType type) {
        return Count.WithPlanned(type) == 0;
    }

    public static boolean free(AUnitType building) {
        return Select.ourOfType(building).free().notEmpty();
    }

    public static boolean notEvenInPlans(AUnitType type) {
        return Count.WithPlanned(type) == 0;
    }

    public static boolean existingOrPlanned(AUnitType building, HasPosition position, double inRadius) {
        assert building.isBuilding();

        if (ConstructionRequests.hasNotStartedNear(building, position, inRadius)) {
            return true;
        }

        return Select.ourWithUnfinished(building).inRadius(inRadius, position).atLeast(1);
    }

    public static boolean existingOrPlannedOrInQueue(AUnitType building, HasPosition position, double inRadius) {
        assert building.isBuilding();

        if (ProductionQueue.isAtTheTopOfQueue(building, 2)) {
            return true;
        }

        if (ConstructionRequests.hasNotStartedNear(building, position, inRadius)) {
            return true;
        }

        return Select.ourWithUnfinished(building).inRadius(inRadius, position).atLeast(1);
    }

    // =========================================================

    public static boolean armory() {
        return Count.ofType(AUnitType.Terran_Armory) > 0;
    }

    public static boolean hydraliskDen() {
        return Count.ofType(AUnitType.Zerg_Hydralisk_Den) > 0;
    }

    public static boolean base() {
        return Select.main() != null;
    }

    public static boolean engBay() {
        return Count.ofType(AUnitType.Terran_Engineering_Bay) > 0;
    }

    public static boolean dragoon() {
        return Count.ofType(AUnitType.Protoss_Dragoon) > 0;
    }

    public static boolean cannon() {
        return Count.ofType(AUnitType.Protoss_Photon_Cannon) > 0;
    }

    public static boolean barracks() {
        return Count.ofType(AUnitType.Terran_Barracks) > 0;
    }

    public static boolean academy() {
        return Count.ofType(AUnitType.Terran_Academy) > 0;
    }

    public static boolean cyberneticsCore() {
        return Count.ofType(AUnitType.Protoss_Cybernetics_Core) > 0;
    }

    public static boolean main() {
        return base();
    }

    public static boolean factory() {
        return Count.ofType(AUnitType.Terran_Factory) > 0;
    }

    public static boolean machineShop() {
        return have(AUnitType.Terran_Machine_Shop);
    }

    public static boolean roboticsFacility() {
        return have(AUnitType.Protoss_Robotics_Facility);
    }

}
