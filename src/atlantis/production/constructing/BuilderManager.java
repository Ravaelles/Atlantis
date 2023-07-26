package atlantis.production.constructing;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.map.position.APosition;
import atlantis.production.constructing.position.APositionFinder;
import atlantis.production.constructing.position.AbstractPositionFinder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;
import atlantis.util.We;
import atlantis.util.log.ErrorLog;
import bwapi.TilePosition;

public class BuilderManager extends Manager {
    public BuilderManager(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isBuilder();
    }

    @Override
    public Manager handle() {
        if (update()) return usedManager(this);

        return null;
    }

    private boolean update() {

        // Don't disturb unit that are already constructing
        if (unit.isConstructing() || unit.isMorphing()) {
            return true;
        }

        if (fixTerranConstructionsWithoutBuilder()) {
            return true;
        }

        if (handleConstruction()) {
            return true;
        }

        return false;
    }

    private boolean fixTerranConstructionsWithoutBuilder() {
        if (
            We.terran() && unit.isConstructing() && unit.buildUnit() != null
            && A.everyNthGameFrame(47)
            && unit.looksIdle()
        ) {
            if (Select.ourWorkers().inRadius(1.5, unit.buildUnit()).notEmpty()) {
                unit.doRightClickAndYesIKnowIShouldAvoidUsingIt(unit.buildUnit());
                return true;
            }
        }

        return false;
    }

    private boolean handleConstruction() {
        Construction construction = ConstructionRequests.constructionFor(unit);
        if (construction != null) {

            // Construction HASN'T STARTED YET, we're probably not even at the required place
            if (construction.status() == ConstructionOrderStatus.CONSTRUCTION_NOT_STARTED) {
                return travelToConstruct(construction);
            } else if (construction.status() == ConstructionOrderStatus.CONSTRUCTION_IN_PROGRESS) {
                // Do nothing - construction is pending
            } else if (construction.status() == ConstructionOrderStatus.CONSTRUCTION_FINISHED) {
                // Do nothing - construction is finished
            }
        } else {
//            System.err.println("construction null for " + unit);
            return false;
        }
        return false;
    }

    private boolean travelToConstruct(Construction construction) {
        APosition buildPosition = construction.buildPosition();
        APosition buildPositionCenter = construction.positionToBuildCenter();
        AUnitType buildingType = construction.buildingType();

        if (unit == null) {
            throw new RuntimeException("unit empty");
        }
        if (buildPosition == null) {
            System.err.println("buildPosition is null (travelToConstruct " + buildingType + ")");
            construction.cancel();
            return false;
        }

        // =========================================================

        double minDistanceToIssueBuildOrder = buildingType.isGasBuilding() ? 3.6 : 1.1;
        double distance = unit.distTo(buildPositionCenter);
        String distString = "(" + A.digit(distance) + ")";

//        CameraCommander.centerCameraOn(unit.getPosition());

        // Move unit to the build position
        if (distance > minDistanceToIssueBuildOrder) {
            if (shouldNotTravelYet(buildingType, distance)) {
                unit.setTooltipTactical("Wait to build " + buildingType.name() + distString);
                return false;
            }

            if (!unit.isMoving()) {
//                if (A.everyNthGameFrame(20)) {
//                    construction.setPositionToBuild(newPosition);
//                }

//                    GameSpeed.changeSpeedTo(60);
                if (unit.move(
                    construction.positionToBuildCenter(),
                    Actions.MOVE_BUILD,
                    "Build " + buildingType.name() + distString,
                    true
                )) return true;
            }

            return true;
        }

        // =========================================================
        // AUnit is already at the build position

        // If we can afford to construct this building exactly right now, issue build order which should
        // be immediate as unit is standing just right there

        else {
            return issueBuildOrder(buildingType, construction);
        }
    }

    private boolean issueBuildOrder(AUnitType buildingType, Construction order) {
        if (We.protoss()) {
            AUnit newBuilding = Select.ourUnfinished()
                .ofType(order.buildingType())
                .inRadius(2, unit).nearestTo(unit);
            if (newBuilding != null) {
                order.setStatus(ConstructionOrderStatus.CONSTRUCTION_IN_PROGRESS);
                order.setBuilder(null);
                unit.stop("Finished!", true);
                return false;
            }
        }

        if (AGame.canAfford(buildingType.getMineralPrice(), buildingType.getGasPrice())) {
//            System.err.println("buildPosition PRE = " + order.buildPosition());
            APosition buildPosition = refreshBuildPosition(order);
//            APosition buildPosition = order.buildPosition();

            if (buildPosition == null) {
                return false;
            }

//            System.err.println("buildPosition POST = " + buildPosition);
//            System.err.println("buildPosition.translateByTiles(1, 1) = " + buildPosition.translateByTiles(1, 1));

            moveOtherUnitsOutOfConstructionPlace(buildPosition.translateByTiles(1, 1));

            // If place is ok, unit isn't constructing and we can afford it, issue the build command.
            buildPosition = applyGasBuildingFixIfNeeded(buildPosition, buildingType);
            TilePosition buildTilePosition = new TilePosition(
                buildPosition.tx(), buildPosition.ty()
            );

//            if (Select.ourWithUnfinishedOfType(AtlantisConfig.GAS_BUILDING).inRadius(3, buildPosition).notEmpty()) {
//                order.cancel();
//                return false;
//            }

            if (!unit.isConstructing() || unit.isIdle() || AGame.now() % 7 == 0) {
                unit.build(buildingType, buildTilePosition);
                return true;
            }
        }

        return true;
    }

    private APosition refreshBuildPosition(Construction order) {
        if (Select.ourWorkers().inRadius(1.8, order.buildPosition()).atLeast(2)) {
            return APositionFinder.findStandardPosition(
                order.builder(), order.buildingType(), order.buildPosition(), 10
            );
//            return APositionFinder.findPositionForNew(
//                order.unit(), order.buildingType(), order
//            );
        }

        return order.buildPosition();
    }

    private void moveOtherUnitsOutOfConstructionPlace(APosition buildPosition) {
        for (AUnit unit : unit.friendsNear().inRadius(2.3, buildPosition).exclude(unit).list()) {
            unit.moveAwayFrom(buildPosition, 1, "Construction!", Actions.MOVE_SPECIAL);
        }
    }

    /**
     * From reasons impossible to explain sometimes it happens that we need to build Extractor
     * one tile left from the top left position. Sometimes not. This method takes care of
     * these cases and ensures the position is valid.
     */
    private APosition applyGasBuildingFixIfNeeded(APosition position, AUnitType building) {
        if (position == null) {
            return null;
        }

        if (
            building.isGasBuilding()
            && !AbstractPositionFinder.canPhysicallyBuildHere(unit, building, position)
        ) {
            if (AbstractPositionFinder.canPhysicallyBuildHere(
                unit, building, position.translateByTiles(-1, 0))
            ) {
                System.out.println("Applied [-1,0] " + building + " position FIX");
                return position.translateByTiles(-1, 0);
            }
            if (AbstractPositionFinder.canPhysicallyBuildHere(
                unit, building, position.translateByTiles(1, 0))
            ) {
                System.out.println("Applied [1,0] " + building + " position FIX");
                return position.translateByTiles(1, 0);
            }
            if (AbstractPositionFinder.canPhysicallyBuildHere(
                unit, building, position.translateByTiles(-2, -1))
            ) {
                System.out.println("Applied [-2,-1] " + building + " position FIX");
                return position.translateByTiles(-2, -1);
            }
            if (AbstractPositionFinder.canPhysicallyBuildHere(
                unit, building, position.translateByTiles(2, 1))
            ) {
                System.out.println("Applied [2,1] " + building + " position FIX");
                return position.translateByTiles(2, 1);
            }

            ErrorLog.printMaxOncePerMinute("Gas building FIX was not applied. This can halt gas building");
        }

        return position;
    }

    // =========================================================

    private boolean shouldNotTravelYet(AUnitType building, double distance) {
//        if (AGame.timeSeconds() < 300 && !building.isBase()) {
        if (AGame.timeSeconds() < 300) {
            int baseBonus = building.isBase() ? 80 : 0;
//            return !AGame.canAfford(

//            if (building.is(AUnitType.Zerg_Spawning_Pool)) {
//                System.out.println(
//                    "mineralsRes=" + CurrentProductionQueue.resourcesReserved()[0]
//                    + ", queuePos=" + ProductionQueue.positionInQueue(building)
//                );
//            }

//            if (ProductionQueue.isAtTheTopOfQueue(building, 1)) {
                return !AGame.canAfford(
                         building.getMineralPrice() - 32 - (int) (distance * 1.3) - baseBonus,
                        building.getGasPrice() - 16 - (int) distance
                );
//            }
//
//            return !AGame.canAffordWithReserved(
//                     building.getMineralPrice() - 32 - (int) (distance * 1.3),
//                    building.getGasPrice() - 16 - (int) distance
//            );
        }

        return false;
    }

    // =========================================================

    /**
     * Returns true if given worker has been assigned to construct new building or if the constructions is
     * already in progress.
     */
    public static boolean isBuilder(AUnit worker) {
        if (worker.isConstructing() ||
            (!AGame.isPlayingAsProtoss() && ConstructionRequests.constructionFor(worker) != null)) {
            return true;
        }

        for (Construction construction : ConstructionRequests.constructions) {
            if (worker.equals(construction.builder())) {

                // Pending Protoss buildings allow unit to go away
                // Terran and Zerg need to use the worker until construction is finished
                return !AGame.isPlayingAsProtoss() || !ConstructionOrderStatus.CONSTRUCTION_IN_PROGRESS
                    .equals(construction.status());
            }
        }

        return false;
    }
}
