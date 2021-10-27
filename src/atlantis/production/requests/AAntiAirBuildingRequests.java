package atlantis.production.requests;

import atlantis.AtlantisConfig;
import atlantis.constructing.AConstructionRequests;
import atlantis.position.APosition;
import atlantis.strategy.AStrategyInformations;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;

public class AAntiAirBuildingRequests {

    public static boolean handle() {
        if (shouldBuild()) {
            return requestDefensiveBuildingAntiAir(null);
        }

        return false;
    }

    // =========================================================

    private static boolean shouldBuild() {
        int defBuildingAntiLand = AConstructionRequests.countExistingAndPlannedConstructions(AtlantisConfig.DEFENSIVE_BUILDING_ANTI_AIR);
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
                        building, 8, base.getPosition()
                );

                for (int i = 0; i < expectedUnits() - numberOfAntiAirBuildingsNearBase; i++) {
                    AConstructionRequests.requestConstructionOf(building, base.getPosition());
                }
            }
        }

        if (nearTo != null) {
            AConstructionRequests.requestConstructionOf(building, nearTo);
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

        int requiredParents = AConstructionRequests.countExistingAndPlannedConstructions(building.getWhatIsRequired());
        if (requiredParents == 0) {
            AConstructionRequests.requestConstructionOf(building.getWhatIsRequired());
            return;
        }

        // === Protect every base ==========================================

        for (AUnit base : Select.ourBases().listUnits()) {
            int numberOfAntiAirBuildingsNearBase = AConstructionRequests.countExistingAndPlannedConstructionsInRadius(
                    building, 8, base.getPosition()
            );

            for (int i = 0; i < expectedUnits() - numberOfAntiAirBuildingsNearBase; i++) {
                AConstructionRequests.requestConstructionOf(building, base.getPosition());
            }
        }
    }

}
