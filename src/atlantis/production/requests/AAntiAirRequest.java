package atlantis.production.requests;

import atlantis.AGame;
import atlantis.AtlantisConfig;
import atlantis.combat.missions.MissionDefend;
import atlantis.constructing.AConstructionManager;
import atlantis.map.AMap;
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

        AConstructionManager.requestConstructionOf(building, nearTo);
    }

    /**
     * Quick air units are: Mutalisk, Wraith, Protoss Scout.
     */
    public static void requestAntiAirQuick(APosition where) {
        AUnitType building = AtlantisConfig.DEFENSIVE_BUILDING_ANTI_AIR;
        int antiAirBuildings = AConstructionManager.countExistingAndPlannedConstructions(building);

        // === Ensure we have required units ========================================

        int requiredParents = AConstructionManager.countExistingAndPlannedConstructions(building.getWhatIsRequired());
        if (requiredParents == 0) {
            AConstructionManager.requestConstructionOf(building.getWhatIsRequired());
            return;
        }

        // === Protect every base ==========================================

        for (AUnit base : Select.ourBases().listUnits()) {
            int numberOfAntiAirBuildingsNearBase = AConstructionManager.countExistingAndPlannedConstructionsInRadius(
                    building, 8, base.getPosition()
            );

            for (int i = 0; i < 2 - numberOfAntiAirBuildingsNearBase; i++) {
                AConstructionManager.requestConstructionOf(building, base.getPosition());
            }
        }
    }
}
