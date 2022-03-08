package atlantis.production.requests;

import atlantis.config.AtlantisConfig;
import atlantis.information.decisions.OurStrategicBuildings;
import atlantis.information.enemy.EnemyInfo;
import atlantis.map.AChoke;
import atlantis.map.Chokes;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.production.Requirements;
import atlantis.production.constructing.position.PositionModifier;
import atlantis.production.orders.build.AddToQueue;
import atlantis.production.orders.production.ProductionQueue;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.util.cache.Cache;
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
            if (EnemyInfo.isDoingEarlyGamePush()) {
                return 2;
            }

            return 0;
        }

        if (We.protoss()) {
            if (Count.cannons() >= 1 && EnemyInfo.isDoingEarlyGamePush()) {
                return 2;
            }

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
            nearTo = positionForNext();
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

    public static APosition positionForNext() {
        if (Count.bases() <= 1) {
            return PositionModifier.toPosition(
                PositionModifier.MAIN_CHOKE, AUnitType.Protoss_Photon_Cannon, null, null
            );
        }

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
