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

    /**
     * Returns true if given position (treated as building position for our <b>UnitType building</b>) has all
     * necessary requirements like: doesn't collide with another building, isn't too close to minerals etc.
     */
    public static boolean doesPositionFulfillAllConditions(
        AUnit builder, AUnitType building, APosition position, HasPosition nearTo
    ) {
//        System.out.println(position + " / " + AbstractPositionFinder._CONDITION_THAT_FAILED);

//        APainter.paintCircle(position, 6, Color.Red);

//        if (position.tx() % 2 == 0 && !building.isBase() && !building.isGasBuilding()) return false;

        if (invalidPosition(position)) return false;

        // This fails:
        // - at least when pylon is not fully finished
//        if (!position.isBuildable()) {
//            AbstractPositionFinder._CONDITION_THAT_FAILED = "Not buildable";
//            return false;
//        }

        if (We.protoss()) {
            if (ProtossForbiddenByStreetGrid.isForbiddenByStreetGrid(builder, building, position)) return false;
            if (IsPositionPowered.isNotPowered(building, position)) return false;
            if (TooCloseToOtherPylons.isTooCloseToOtherPylons(builder, building, position)) return false;
            if (ProtossTooCloseToMapBoundaries.isTooClose(building, position)) return false;
            if (ProtossTooCloseToRegionBoundaries.isTooCloseToRegionBoundaries(building, position)) return false;
        }

        if (We.terran()) {
            if (TerranForbiddenByStreetGrid.isForbiddenByStreetGrid(builder, building, position)) return false;
            if (!TerranHasEnoughSidesFreeFromOtherBuildings.isOkay(builder, building, position)) return false;
            if (TerranPositionFinder.isNotEnoughPlaceLeftForAddons(builder, building, position)) return false;
            if (building.isMissileTurret()) {
                if (IsProbablyInAnotherRegion.differentRegion(builder, building, position, nearTo)) return false;
            }
            if (TooCloseToBunker.isTooCloseToBunker(building, position)) return false;
            if (TooCloseToTerranBase.isTooCloseToBase(building, position)) return false;
        }

        if (!CanPhysicallyBuildHere.check(builder, building, position)) return false;
        if (TooCloseToUnwalkable.isTooCloseToUnwalkable(building, position)) return false;
        if (OtherConstructionTooClose.isOtherConstructionTooClose(builder, building, position)) return false;

        if (!building.isBase()) {
            if (TooCloseToChoke.isTooCloseToChoke(building, position)) return false;
            if (OverlappingBaseLocation.isOverlappingBaseLocation(building, position)) return false;
            if (TooCloseToMainBase.isTooCloseToMainBase(building, position)) return false;
            if (TooCloseToMineralsOrGeyser.isTooCloseToMineralsOrGeyser(building, position)) return false;
            if (!ProtossHasEnoughSidesFreeFromOtherBuildings.isOkay(builder, building, position)) return false;
            if (IsProbablyInAnotherRegion.differentRegion(builder, building, position, nearTo)) return false;
        }

        // All conditions are fullfilled, return this position
        return true;
    }

    private static boolean invalidPosition(APosition position) {
        if (position == null) {
            ErrorLog.printMaxOncePerMinute("PositionFulfillsAllConditions: position is null");
            AbstractPositionFinder._CONDITION_THAT_FAILED = "POSITION ARGUMENT IS NULL";
            return true;
        }

        if (position.isOutOfBounds()) {
            AbstractPositionFinder._CONDITION_THAT_FAILED = "Position out of bounds";
            return true;
        }

        return false;
    }
}
