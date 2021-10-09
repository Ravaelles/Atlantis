package atlantis.production.requests;

import atlantis.AtlantisConfig;
import atlantis.constructing.AConstructionRequests;
import atlantis.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;

public class AAntiLandRequest {


    public static void requestDefensiveBuildingAntiLand(APosition where) {
        AUnitType building = AtlantisConfig.DEFENSIVE_BUILDING_ANTI_LAND;
        APosition nearTo = where;

        AUnit previousBuilding = Select.ourBuildingsIncludingUnfinished().ofType(building).first();
        if (where == null) {
            if (previousBuilding != null) {
//            AGame.sendMessage("New bunker near " + previousBuilding);
//            System.out.println("New bunker near " + previousBuilding);
            nearTo = previousBuilding.getPosition();
            }
            else {
    //            System.out.println("New bunker at default");
                nearTo = null;
            }
        }

        if (nearTo != null) {
            AConstructionRequests.requestConstructionOf(building, nearTo);
        }
    }
}
