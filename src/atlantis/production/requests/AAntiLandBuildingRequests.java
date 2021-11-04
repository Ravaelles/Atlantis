package atlantis.production.requests;

import atlantis.AtlantisConfig;
import atlantis.map.AChoke;
import atlantis.map.MapChokes;
import atlantis.position.APosition;
import atlantis.production.Requirements;
import atlantis.production.orders.AddToQueue;
import atlantis.strategy.AStrategyInformations;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.util.Cache;
import atlantis.util.Us;

public class AAntiLandBuildingRequests {

    private Cache<APosition> cache = new Cache<>();

    public static boolean handle() {
        if (shouldBuildNew()) {
            System.out.println("ENQUEUE NEW CANNON");
            return requestDefensiveBuildingAntiLand(null);
        }

        return false;
    }

    // =========================================================

    private static boolean shouldBuildNew() {
        if (!Requirements.hasRequirements(AtlantisConfig.DEFENSIVE_BUILDING_ANTI_LAND)) {
            return false;
        }

        int defBuildingAntiLand = Count.existingOrInProductionOrInQueue(AtlantisConfig.DEFENSIVE_BUILDING_ANTI_LAND);
//        int defBuildingAntiLand = AConstructionRequests.countExistingAndExpectedInNearFuture(
//                AtlantisConfig.DEFENSIVE_BUILDING_ANTI_LAND, 8
//        );
//        System.out.println(
//                AtlantisConfig.DEFENSIVE_BUILDING_ANTI_LAND
//                + " // "
//                + defBuildingAntiLand
//                + " < "
//                + Math.max(expectedUnits(), AStrategyInformations.antiLandBuildingsNeeded())
//                + " //// " +
//                + ProductionQueue.countInQueue(AtlantisConfig.DEFENSIVE_BUILDING_ANTI_LAND, 8)
//
//        );
        return defBuildingAntiLand < Math.max(expectedUnits(), AStrategyInformations.antiLandBuildingsNeeded());
    }

    public static int expectedUnits() {
        if (Us.isTerran()) {
            return 1;
        }

        if (Us.isProtoss()) {
            return 1;
        }

        return 3 * Select.ourBases().count();
    }

    public static boolean requestDefensiveBuildingAntiLand(APosition nearTo) {
        if (nearTo == null) {
            nearTo = positionForNextBuilding();
        }

        if (nearTo != null) {
            AddToQueue.addWithTopPriority(building(), nearTo);
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
            nearTo = Select.naturalOrMain() != null ? Select.naturalOrMain().position() : null;
//            nearTo = Select.mainBase();
        }

        // Move towards nearest choke
        if (nearTo != null) {
            AChoke choke = MapChokes.nearestChoke(nearTo);
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
