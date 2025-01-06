package atlantis.production.constructing.position;

import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.production.constructing.position.conditions.*;
import atlantis.production.constructing.position.protoss.*;
import atlantis.production.constructing.position.terran.TerranForbiddenByStreetGrid;
import atlantis.production.constructing.position.terran.TerranPositionFinder;
import atlantis.production.constructing.position.terran.TooCloseToBunker;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.util.We;
import atlantis.util.log.ErrorLog;

public class PositionFulfillsAllConditions {
    public static int currentSearchRadius = -1;
    private static AUnitType building;

    /**
     * Returns true if given position (treated as building position for our <b>UnitType building</b>) has all
     * necessary requirements like: doesn't collide with another building, isn't too close to minerals etc.
     */
    public static boolean doesPositionFulfillAllConditions(
        AUnit builder, AUnitType building, APosition position, HasPosition nearTo
    ) {
        PositionFulfillsAllConditions.building = building;

//        System.out.println("Conditions: " + position);
//        if (building.isForge())
//        System.out.println(position + " / " + building + " / Status: " + AbstractPositionFinder._STATUS);

//        APainter.paintCircle(position, 6, Color.Red);
//        if (position.tx() % 2 == 0 && !building.isBase() && !building.isGasBuilding()) return false;

        if (!validPosition(position)) return failed();

        // This fails:
        // - at least when pylon is not fully finished
//        if (!position.isBuildable()) {
//            AbstractPositionFinder._CONDITION_THAT_FAILED = "Not buildable";
//            return false;
//        }

        if (We.protoss()) {
            if (ProtossForbiddenByStreetGrid.isForbiddenByStreetGrid(builder, building, position)) return failed();
            if (IsPositionPowered.isNotPowered(building, position)) return failed();
            if (TooCloseToOtherPylons.isTooCloseToOtherPylons(builder, building, position)) return failed();
            if (ProtossTooCloseToMapBoundaries.isTooClose(building, position)) return failed();
            if (ProtossTooCloseToRegionBoundaries.isTooCloseToRegionBoundaries(building, position)) return failed();
        }

        if (We.terran()) {
            if (TerranForbiddenByStreetGrid.isForbiddenByStreetGrid(builder, building, position)) return failed();
            if (!TerranHasEnoughSidesFreeFromOtherBuildings.isOkay(builder, building, position)) return failed();
            if (TerranPositionFinder.isNotEnoughPlaceLeftForAddons(builder, building, position)) return failed();
            if (building.isMissileTurret()) {
                if (IsProbablyInAnotherRegion.differentRegion(builder, building, position, nearTo)) return failed();
            }
            if (TooCloseToBunker.isTooCloseToBunker(building, position)) return failed();
            if (TooCloseToTerranBase.isTooCloseToBase(building, position)) return failed();
        }

        if (!CanPhysicallyBuildHere.check(builder, building, position)) return failed();
        if (TooCloseToUnwalkable.isTooCloseToUnwalkable(building, position)) return failed();
        if (OtherConstructionTooClose.isOtherConstructionTooClose(builder, building, position)) return failed();

        if (!building.isBase()) {
            if (TooCloseToChoke.isTooCloseToChoke(building, position)) return failed();
            if (TooCloseToBaseLocation.isOverlappingBaseLocation(building, position)) return failed();
            if (TooCloseToMainBase.isTooCloseToMainBase(building, position)) return failed();
            if (TooCloseToMineralsOrGeyser.isTooCloseToMineralsOrGeyser(building, position)) return failed();
            if (!ProtossHasEnoughSidesFreeFromOtherBuildings.isOkay(builder, building, position)) return failed();
            if (IsProbablyInAnotherRegion.differentRegion(builder, building, position, nearTo)) return failed();
        }

        // All conditions are fullfilled, return this position
        return success();
    }

    private static boolean success() {
        AbstractPositionFinder._STATUS = "OK";
        return true;
    }

    private static boolean failed() {
        if (AbstractPositionFinder._STATUS == null) {
            ErrorLog.printMaxOncePerMinutePlusPrintStackTrace(
                "failed() called for " + PositionFulfillsAllConditions.building + ", but no _STATUS was set"
            );
        }

        return false;
    }

    private static boolean validPosition(APosition position) {
        if (position == null) {
            ErrorLog.printMaxOncePerMinute("PositionFulfillsAllConditions: position is null");
            AbstractPositionFinder._STATUS = "POSITION ARGUMENT IS NULL";
            return false;
        }

        if (position.isOutOfBounds()) {
            AbstractPositionFinder._STATUS = "Position out of bounds";
            return false;
        }

        return true;
    }
}
