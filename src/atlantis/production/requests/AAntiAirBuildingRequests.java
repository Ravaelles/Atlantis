package atlantis.production.requests;

import atlantis.AtlantisConfig;
import atlantis.production.constructing.AConstructionRequests;
import atlantis.position.APosition;
import atlantis.production.orders.AddToQueue;
import atlantis.strategy.AStrategyInformations;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
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
        int defBuildingAntiLand = AConstructionRequests.countExistingAndExpectedInNearFuture(
                AtlantisConfig.DEFENSIVE_BUILDING_ANTI_AIR, 6
        );
        return defBuildingAntiLand < AStrategyInformations.antiLandBuildingsNeeded;
    }

    public static int expectedUnits() {
        return 3 * Select.ourBases().count();
    }

    public static boolean requestDefensiveBuildingAntiAir(APosition nearTo) {
        AUnitType building = AtlantisConfig.DEFENSIVE_BUILDING_ANTI_AIR;

        if (nearTo == null) {
            for (AUnit base : Select.ourBases().listUnits()) {
                int numberOfAntiAirBuildingsNearBase = AConstructionRequests.countExistingAndPlannedConstructionsInRadius(
                        building, 8, base.position()
                );

                for (int i = 0; i < expectedUnits() - numberOfAntiAirBuildingsNearBase; i++) {
                    AddToQueue.addWithTopPriority(building, base.position());
                }
            }
        }

        if (nearTo != null) {
            AddToQueue.addWithTopPriority(building, nearTo);
            return true;
        }

        return false;
    }

    /**
     * Quick air units are: Mutalisk, Wraith, Protoss Scout.
     */
    public static void requestAntiAirQuick(APosition where) {
        AUnitType building = AtlantisConfig.DEFENSIVE_BUILDING_ANTI_AIR;
//        int antiAirBuildings = AConstructionRequests.countExistingAndPlannedConstructions(building);

        // === Ensure we have required units ========================================

        int requiredParents = AConstructionRequests.countExistingAndNotFinished(building.getWhatIsRequired());
        if (requiredParents == 0) {
            AddToQueue.addWithHighPriority(building.getWhatIsRequired());
            return;
        }

        // === Protect every base ==========================================

        for (AUnit base : Select.ourBases().listUnits()) {
            int numberOfAntiAirBuildingsNearBase = AConstructionRequests.countExistingAndPlannedConstructionsInRadius(
                    building, 8, base.position()
            );

            for (int i = 0; i < expectedUnits() - numberOfAntiAirBuildingsNearBase; i++) {
                AddToQueue.addWithTopPriority(building, base.position());
            }
        }
    }

}
