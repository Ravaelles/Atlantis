package atlantis.production.constructing.position;

import atlantis.map.position.APosition;
import atlantis.production.constructing.position.conditions.*;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;

public class PositionFulfillsAllConditions {
    /**
     * Returns true if given position (treated as building position for our <b>UnitType building</b>) has all
     * necessary requirements like: doesn't collide with another building, isn't too close to minerals etc.
     */
    public static boolean doesPositionFulfillAllConditions(AUnit builder, AUnitType building, APosition position) {
//        APainter.paintCircle(position, 6, Color.Red);

        if (position == null) {
            TerranPositionFinder._CONDITION_THAT_FAILED = "POSITION ARGUMENT IS NULL";
            return false;
        }

        if (builder == null) {
            builder = Select.ourWorkers().nearestTo(position);
            if (builder == null) {
                TerranPositionFinder._CONDITION_THAT_FAILED = "NO BUILDER ASSIGNED";
                return false;
            }
        }

        // Leave entire horizontal (same tileX) and vertical (same tileY) corridors free for units to pass
        // So disallow building in e.g. 1, 5, 9, 13, 16 horizontally and 3, 7, 11, 15, 19 vertically
//        if (ForbiddenByStreetGrid.isForbiddenByStreetGrid(builder, building, position)) {
//            return false;
//        }

        if (!HasEnoughSidesFreeFromOtherBuildings.check(builder, building, position)) {
            AbstractPositionFinder._CONDITION_THAT_FAILED = "Must leave enough free space for units to pass";
            return false;
        }

        // If it's not physically possible to build here (e.g. rocks, other buildings etc)
        if (!CanPhysicallyBuildHere.canPhysicallyBuildHere(builder, building, position)) return false;

        // If other buildings too close
        if (OtherConstructionTooClose.isOtherConstructionTooClose(builder, building, position)) return false;

        if (OverlappingBaseLocation.isOverlappingBaseLocation(building, position)) return false;

        if (TerranPositionFinder.isNotEnoughPlaceLeftForAddons(builder, building, position)) return false;

        if (building.isMissileTurret()) return true;

        if (TooCloseToMainBase.isTooCloseToMainBase(building, position)) return false;

        // Can't be too close to minerals or to geyser, because would slow down production
        if (TooCloseToMineralsOrGeyser.isTooCloseToMineralsOrGeyser(building, position)) return false;

        // Overlapping a choke point can make it impossible to pass
        if (TooCloseToChoke.isTooCloseToChoke(building, position)) return false;

        // All conditions are fullfilled, return this position
        return true;
    }
}