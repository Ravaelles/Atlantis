package atlantis.production.requests;

import atlantis.AtlantisConfig;
import atlantis.constructing.AConstructionRequests;
import atlantis.map.AChoke;
import atlantis.map.AMap;
import atlantis.position.APosition;
import atlantis.strategy.AStrategyInformations;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.Us;

public class AAntiLandBuildingRequests {

    public static boolean handle() {
        if (shouldBuild()) {
            return requestDefensiveBuildingAntiLand(null);
        }

        return false;
    }

    // =========================================================

    private static boolean shouldBuild() {
        int defBuildingAntiLand = AConstructionRequests.countExistingAndPlannedConstructions(AtlantisConfig.DEFENSIVE_BUILDING_ANTI_LAND);
        return defBuildingAntiLand < Math.max(expectedUnits(), AStrategyInformations.antiLandBuildingsNeeded);
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

    public static boolean requestDefensiveBuildingAntiLand(APosition nearTo) {
        if (nearTo == null) {
            nearTo = positionForNextBuilding();
        }

        if (nearTo != null) {
            AConstructionRequests.requestConstructionOf(building(), nearTo);
            return true;
        }

        return false;
    }

    public static APosition positionForNextBuilding() {
        AUnitType building = building();
        APosition nearTo = null;

//        System.out.println(building + " // " + AGame.hasTechAndBuildingsToProduce(building));


        AUnit previousBuilding = Select.ourBuildingsIncludingUnfinished().ofType(building).first();
        if (previousBuilding != null) {
            nearTo = previousBuilding.position();
        }

        if (nearTo == null) {

            // Place near the base
            nearTo = Select.naturalBaseOrMain() != null ? Select.naturalBaseOrMain().position() : null;
//            nearTo = Select.mainBase();
        }

        // Move towards nearest choke
        if (nearTo != null) {
            AChoke choke = AMap.nearestChoke(nearTo);
            if (choke != null) {
                nearTo = nearTo.translateTilesTowards(choke, 7);
            }
        }

        return nearTo;
    }

    public static AUnitType building() {
        return AtlantisConfig.DEFENSIVE_BUILDING_ANTI_LAND;
    }

}
