package atlantis.production.constructing.position;

import atlantis.debug.painter.APainter;
import atlantis.map.position.APosition;
import atlantis.production.constructing.position.conditions.*;
import atlantis.production.constructing.position.terran.TerranPositionFinder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import bwapi.Color;

public class PositionFulfillsAllConditions {
    /**
     * Returns true if given position (treated as building position for our <b>UnitType building</b>) has all
     * necessary requirements like: doesn't collide with another building, isn't too close to minerals etc.
     */
    public static boolean doesPositionFulfillAllConditions(AUnit builder, AUnitType building, APosition position) {
        APainter.paintCircle(position, 6, Color.Red);

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

        if (ForbiddenByStreetGrid.isForbiddenByStreetGrid(builder, building, position)) return false;
        if (ForbiddenForSupplyDepot.isForbidden(builder, building, position)) return false;

        if (!HasEnoughSidesFreeFromOtherBuildings.check(builder, building, position)) {
            AbstractPositionFinder._CONDITION_THAT_FAILED = "Must leave enough free space for units to pass";
            return false;
        }

        if (!CanPhysicallyBuildHere.check(builder, building, position)) return false;
        if (OtherConstructionTooClose.isOtherConstructionTooClose(builder, building, position)) return false;
        if (TooCloseToBase.isTooCloseToBase(building, position)) return false;
        if (OverlappingBaseLocation.isOverlappingBaseLocation(building, position)) return false;
        if (TooCloseToUnwalkable.isTooCloseToUnwalkable(building, position)) return false;
        if (TerranPositionFinder.isNotEnoughPlaceLeftForAddons(builder, building, position)) return false;
        if (TooCloseToChoke.isTooCloseToChoke(building, position)) return false;
        if (TooCloseToBunker.isTooCloseToBunker(building, position)) return false;
        if (LooksTooFar.looksTooFar(builder, building, position)) return false;

        if (building.isMissileTurret()) return true;

        if (TooCloseToMainBase.isTooCloseToMainBase(building, position)) return false;
        if (TooCloseToMineralsOrGeyser.isTooCloseToMineralsOrGeyser(building, position)) return false;

        // All conditions are fullfilled, return this position
        return true;
    }
}
