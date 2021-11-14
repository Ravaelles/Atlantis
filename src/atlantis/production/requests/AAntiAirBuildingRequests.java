package atlantis.production.requests;

import atlantis.AtlantisConfig;
import atlantis.position.HasPosition;
import atlantis.production.Requirements;
import atlantis.production.constructing.ConstructionRequests;
import atlantis.position.APosition;
import atlantis.production.orders.AddToQueue;
import atlantis.strategy.decisions.OurStrategicBuildings;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Select;

public class AAntiAirBuildingRequests {

    public static boolean handle() {
        if (shouldBuildNew()) {
            return requestDefensiveBuildingAntiAir(null);
        }

        return false;
    }

    // =========================================================

    private static boolean shouldBuildNew() {
        if (!Requirements.hasRequirements(AtlantisConfig.DEFENSIVE_BUILDING_ANTI_LAND)) {
            return false;
        }

        int defBuildingAntiLand = Count.existingOrInProductionOrInQueue(AtlantisConfig.DEFENSIVE_BUILDING_ANTI_AIR);
        return defBuildingAntiLand < OurStrategicBuildings.antiLandBuildingsNeeded();
    }

    public static int expectedUnits() {
        return 3 * Select.ourBases().count();
    }

    public static boolean requestDefensiveBuildingAntiAir(HasPosition nearTo) {
        AUnitType building = AtlantisConfig.DEFENSIVE_BUILDING_ANTI_AIR;

        if (nearTo == null) {
            for (AUnit base : Select.ourBases().listUnits()) {
                int numberOfAntiAirBuildingsNearBase = ConstructionRequests.countExistingAndPlannedInRadius(
                        building, 8, base.position()
                );

                for (int i = 0; i < expectedUnits() - numberOfAntiAirBuildingsNearBase; i++) {
                    AddToQueue.withTopPriority(building, base.position());
                }
            }
        }

        if (nearTo != null) {
            AddToQueue.withTopPriority(building, nearTo);
            return true;
        }

        return false;
    }

    /**
     * Quick air units are: Mutalisk, Wraith, Protoss Scout.
     */
    public static void requestAntiAirQuick(APosition where) {
        AUnitType building = AtlantisConfig.DEFENSIVE_BUILDING_ANTI_AIR;
//        int antiAirBuildings = ConstructionRequests.countExistingAndPlannedConstructions(building);

        // === Ensure we have required units ========================================

        int requiredParents = ConstructionRequests.countExistingAndNotFinished(building.getWhatIsRequired());
        if (requiredParents == 0) {
            AddToQueue.withHighPriority(building.getWhatIsRequired());
            return;
        }

        // === Protect every base ==========================================

        for (AUnit base : Select.ourBases().listUnits()) {
            int numberOfAntiAirBuildingsNearBase = ConstructionRequests.countExistingAndPlannedInRadius(
                    building, 8, base.position()
            );

            for (int i = 0; i < expectedUnits() - numberOfAntiAirBuildingsNearBase; i++) {
                AddToQueue.withTopPriority(building, base.position());
            }
        }
    }

}
