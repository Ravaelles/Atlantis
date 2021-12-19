package atlantis.production.requests;

import atlantis.AtlantisConfig;
import atlantis.map.AChoke;
import atlantis.map.Chokes;
import atlantis.position.APosition;
import atlantis.position.HasPosition;
import atlantis.production.Requirements;
import atlantis.production.orders.AddToQueue;
import atlantis.production.orders.ProductionQueue;
import atlantis.strategy.decisions.OurStrategicBuildings;
import atlantis.strategy.OurStrategy;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.util.Cache;
import atlantis.util.We;

public class AAntiLandBuildingRequests {

    private Cache<APosition> cache = new Cache<>();

    public static boolean handle() {
        if (shouldBuildNew()) {
//            System.out.println("ENQUEUE NEW ANTI LAND");
            return requestCombatBuildingAntiLand(null);
        }

        return false;
    }

    // =========================================================

    private static boolean shouldBuildNew() {
        if (!Requirements.hasRequirements(AtlantisConfig.DEFENSIVE_BUILDING_ANTI_LAND)) {
            return false;
        }

        int defBuildingAntiLand = Count.existingOrInProductionOrInQueue(AtlantisConfig.DEFENSIVE_BUILDING_ANTI_LAND);
//        int defBuildingAntiLand = ConstructionRequests.countExistingAndExpectedInNearFuture(
//                AtlantisConfig.DEFENSIVE_BUILDING_ANTI_LAND, 8
//        );
//        System.out.println(
//                AtlantisConfig.DEFENSIVE_BUILDING_ANTI_LAND
//                + " // "
//                + defBuildingAntiLand
//                + " < "
//                + Math.max(expectedUnits(), OurStrategicBuildings.antiLandBuildingsNeeded())
//                + " //// " +
//                + ProductionQueue.countInQueue(AtlantisConfig.DEFENSIVE_BUILDING_ANTI_LAND, 8)
//
//        );
        return defBuildingAntiLand < Math.max(expectedUnits(), OurStrategicBuildings.antiLandBuildingsNeeded());
    }

    public static int expectedUnits() {
        if (We.terran()) {
            return 0;
        }

        if (We.protoss()) {
            return 0;
        }

//        if (We.zerg()) {
//            if (!OurStrategy.get().isRushOrCheese()) {
//                return 3 * Select.ourBases().count();
//            }
//        }

        return 0;
    }

    public static boolean requestCombatBuildingAntiLand(HasPosition nearTo) {
        if (nearTo == null) {
            nearTo = positionForNextBuilding();
        }

        if (nearTo != null) {
            AUnitType required = building().getWhatIsRequired();
            if (
                    required != null
                            && !Requirements.hasRequirements(building())
                            && !ProductionQueue.isAtTheTopOfQueue(required, 6)
            ) {
                AddToQueue.withTopPriority(required);
                return true;
            }

            AddToQueue.withTopPriority(building(), nearTo);
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
            AChoke choke = Chokes.nearestChoke(nearTo);
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
