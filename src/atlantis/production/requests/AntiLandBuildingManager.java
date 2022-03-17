package atlantis.production.requests;

import atlantis.combat.micro.terran.TerranBunker;
import atlantis.information.decisions.OurStrategicBuildings;
import atlantis.information.enemy.EnemyInfo;
import atlantis.map.AChoke;
import atlantis.map.Bases;
import atlantis.map.Chokes;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.production.Requirements;
import atlantis.production.constructing.position.PositionModifier;
import atlantis.production.orders.build.AddToQueue;
import atlantis.production.orders.production.ProductionQueue;
import atlantis.production.requests.protoss.ProtossPhotonCannonAntiLand;
import atlantis.production.requests.zerg.ZergSunkenColony;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.util.We;

public abstract class AntiLandBuildingManager extends DynamicBuildingManager {

    private static AntiLandBuildingManager instance = null;

    // =========================================================

    public boolean handleBuildNew() {
//        System.err.println("Should build new Sunken?");

        if (shouldBuildNew()) {
            System.err.println("ENQUEUE NEW Sunken Colony");
            return requestOne(nextBuildingPosition());
        }

        return false;
    }

    // =========================================================

//    public abstract AUnitType type();

//    protected boolean shouldBuildNew() {
//        if (!Requirements.hasRequirements(type())) {
//            return false;
//        }
//
//        int defBuildingAntiLand = Count.existingOrInProductionOrInQueue(type());
////        int defBuildingAntiLand = ConstructionRequests.countExistingAndExpectedInNearFuture(
////                AtlantisConfig.DEFENSIVE_BUILDING_ANTI_LAND, 8
////        );
////        System.out.println(
////                AtlantisConfig.DEFENSIVE_BUILDING_ANTI_LAND
////                + " // "
////                + defBuildingAntiLand
////                + " < "
////                + Math.max(expectedUnits(), OurStrategicBuildings.antiLandBuildingsNeeded())
////                + " //// " +
////                + ProductionQueue.countInQueue(AtlantisConfig.DEFENSIVE_BUILDING_ANTI_LAND, 8)
////
////        );
//        return defBuildingAntiLand < Math.max(expectedUnits(), OurStrategicBuildings.antiLandBuildingsNeeded());
//    }
//
//    @Override
//    public int expectedUnits() {
//        if (We.terran()) {
//            return TerranBunker.expectedBunkers();
//        }
//
//        if (We.protoss()) {
//            if (Count.cannons() >= 1 && EnemyInfo.isDoingEarlyGamePush()) {
//                return 2;
//            }
//
//            return 0;
//        }
//
////        if (We.zerg()) {
////            if (!OurStrategy.get().isRushOrCheese()) {
////                return 3 * Select.ourBases().count();
////            }
////        }
//
//        return 0;
//    }

    @Override
    public boolean requestOne(HasPosition at) {
        if (at == null) {
            at = nextBuildingPosition();
        }

        if (at != null) {
            AUnitType required = type().whatIsRequired();
            if (
                    required != null
                            && !Requirements.hasRequirements(type())
                            && !ProductionQueue.isAtTheTopOfQueue(required, 6)
            ) {
                AddToQueue.withTopPriority(required);
                return true;
            }

            AddToQueue.withTopPriority(type(), at);
            return true;
        }

        return false;
    }

    @Override
    public HasPosition nextBuildingPosition() {
        int bases = Count.bases();

        // === Main choke ===========================================

        if (bases <= 1) {
            return PositionModifier.toPosition(
                PositionModifier.MAIN_CHOKE, type(), null, null
            );
        }

        // === At natural ===========================================

        if (bases >= 2) {
            APosition naturalPosition = Bases.natural();
            if (naturalPosition != null) {
                AUnit naturalBase = Select.ourBases().inRadius(8, naturalPosition).first();
                if (naturalBase != null) {
                    AChoke naturalChoke = Chokes.natural();
                    if (naturalChoke != null) {
                        return naturalChoke.translatePercentTowards(50, naturalBase);
                    }

                    return naturalBase;
                }
            }
        }

        // =========================================================

        AUnitType building = type();
        APosition nearTo = null;

//        System.out.println(building + " // " + AGame.hasTechAndBuildingsToProduce(building));

        AUnit previousBuilding = Select.ourBuildingsWithUnfinished().ofType(building).first();
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

    // =========================================================

    public static AntiLandBuildingManager get() {
        if (instance == null) {
            if (We.zerg()) {
                return instance = new ZergSunkenColony();
            }
            else if (We.protoss()) {
                return instance = new ProtossPhotonCannonAntiLand();
            }
//            else if (We.terran()) {
//                return instance = new AntiLandBuildingManager();
//            }
        }

        return instance;
    }

    public int existing() {
        return Count.ourOfTypeWithUnfinished(type());
    }

}
