package atlantis.units.select;

import atlantis.map.position.HasPosition;
import atlantis.production.constructing.ConstructionRequests;

import atlantis.production.orders.production.queue.Queue;
import atlantis.units.AUnitType;

public class Have {

    protected static boolean have(AUnitType type) {
        return Count.ofType(type) > 0;
    }

    public static boolean a(AUnitType type) {
//        return Count.withPlanned(type) > 0;
        return Count.ofType(type) > 0;
    }

    public static boolean no(AUnitType type) {
//        return Count.withPlanned(type) > 0;
        return Count.ofType(type) == 0;
    }

    public static boolean unfinishedOrPlanned(AUnitType type) {
        return Count.inProductionOrInQueue(type) > 0;
    }

    public static boolean notEvenPlanned(AUnitType type) {
        return Count.withPlanned(type) == 0;
    }

    public static boolean free(AUnitType building) {
        return Select.ourOfType(building).free().notEmpty();
    }

    public static boolean dontHaveEvenInPlans(AUnitType type) {
        return Count.withPlanned(type) == 0;
    }

    public static boolean haveExistingOrInPlans(AUnitType type) {
        return Count.withPlanned(type) == 0;
    }

    public static boolean existingOrPlanned(AUnitType building, HasPosition position, double inRadius) {
        assert building.isABuilding();

        if (ConstructionRequests.hasNotStartedNear(building, position, inRadius)) return true;

        return Select.ourWithUnfinished(building).inRadius(inRadius, position).atLeast(1);
    }

    public static boolean existingOrUnfinished(AUnitType building) {
        assert building.isABuilding();

        return Select.ourWithUnfinished(building).atLeast(1);
    }

    public static boolean existingOrPlannedOrInQueue(AUnitType building, HasPosition position, double inRadius) {
        assert building.isABuilding();

        if (Queue.get().haveAmongNextOrders(building, 2)) return true;
        if (ConstructionRequests.hasNotStartedNear(building, position, inRadius)) return true;

        return Select.ourWithUnfinished(building).inRadius(inRadius, position).atLeast(1);
    }

    // =========================================================

    public static boolean armory() {
        return Count.ofType(AUnitType.Terran_Armory) > 0;
    }

    public static boolean assimilator() {
        return Count.withPlanned(AUnitType.Protoss_Assimilator) > 0;
    }

    public static boolean observer() {
        return Count.ofType(AUnitType.Protoss_Observer) > 0;
    }

    public static boolean observatory() {
        return Count.ofType(AUnitType.Protoss_Observatory) > 0;
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

    public static boolean starport() {
        return Count.ofType(AUnitType.Terran_Starport) > 0;
    }

    public static boolean spawningPool() {
        return Count.ofType(AUnitType.Zerg_Spawning_Pool) > 0;
    }

    public static boolean controlTower() {
        return Count.ofType(AUnitType.Terran_Control_Tower) > 0;
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

    public static boolean gateway() {
        return Count.ofType(AUnitType.Protoss_Gateway) > 0;
    }

    public static boolean academy() {
        return Count.ofType(AUnitType.Terran_Academy) > 0;
    }

    public static boolean citadel() {
        return Count.ofType(AUnitType.Protoss_Citadel_of_Adun) > 0;
    }

    public static boolean cyberneticsCore() {
        return Count.ofType(AUnitType.Protoss_Cybernetics_Core) > 0;
    }

    public static boolean cyberneticsCoreWithUnfinished() {
        return Count.ourWithUnfinished(AUnitType.Protoss_Cybernetics_Core) > 0;
    }

    public static boolean main() {
        return base();
    }

    public static boolean factory() {
        return Count.ofType(AUnitType.Terran_Factory) > 0;
    }

    public static boolean scienceFacility() {
        return Count.ofType(AUnitType.Terran_Science_Facility) > 0;
    }

    public static boolean machineShop() {
        return have(AUnitType.Terran_Machine_Shop);
    }

    public static boolean roboticsFacility() {
        return have(AUnitType.Protoss_Robotics_Facility);
    }

    public static boolean roboticsSupportBay() {
        return have(AUnitType.Protoss_Robotics_Support_Bay);
    }

    public static boolean forge() {
        return have(AUnitType.Protoss_Forge);
    }

    public static boolean larvas(int minLarvas) {
        return Count.larvas() >= minLarvas;
    }

    public static boolean scienceVessel() {
        return Count.ofType(AUnitType.Terran_Science_Vessel) > 0;
    }
}
