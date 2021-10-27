package atlantis.production.requests;

import atlantis.AtlantisConfig;
import atlantis.constructing.AConstructionRequests;
import atlantis.position.APosition;
import atlantis.strategy.AStrategyInformations;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;
import atlantis.util.Us;

public class AAntiLandRequest {

    public static boolean handle() {
        if (shouldBuild()) {
            return requestDefensiveBuildingAntiLand(null);
        }

        return false;
    }

    // =========================================================

    private static boolean shouldBuild() {
        int defBuildingAntiLand = AConstructionRequests.countExistingAndPlannedConstructions(AtlantisConfig.DEFENSIVE_BUILDING_ANTI_LAND);
        return defBuildingAntiLand < AStrategyInformations.needDefBuildingAntiLand;
    }

    public static int expectedUnits() {
        if (Us.isTerran()) {
            return 1;
        }

        if (Us.isProtoss()) {
            return 2;
        }

        return 3 * Select.ourBases().count();
    }

    public static boolean requestDefensiveBuildingAntiLand(APosition where) {
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

        if (nearTo == null) {
            nearTo = Select.naturalBaseOrMain() != null ? Select.naturalBaseOrMain().getPosition() : null;
        }

        if (nearTo != null) {
            AConstructionRequests.requestConstructionOf(building, nearTo);
            return true;
        }

        return false;
    }

}
