package atlantis.production.requests;

import atlantis.AtlantisConfig;
import atlantis.constructing.AConstructionRequests;
import atlantis.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;

public class AAntiAirRequest {

    public static void requestDefBuildingAntiAir(APosition where) {
        AUnitType building = AtlantisConfig.DEFENSIVE_BUILDING_ANTI_AIR;
        APosition nearTo = where;

//        if (where == null) {
//
//        }

        AConstructionRequests.requestConstructionOf(building, nearTo);
    }

    /**
     * Quick air units are: Mutalisk, Wraith, Protoss Scout.
     */
    public static void requestAntiAirQuick(APosition where) {
        AUnitType building = AtlantisConfig.DEFENSIVE_BUILDING_ANTI_AIR;
        int antiAirBuildings = AConstructionRequests.countExistingAndPlannedConstructions(building);

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

            for (int i = 0; i < 2 - numberOfAntiAirBuildingsNearBase; i++) {
                AConstructionRequests.requestConstructionOf(building, base.getPosition());
            }
        }
    }
}
