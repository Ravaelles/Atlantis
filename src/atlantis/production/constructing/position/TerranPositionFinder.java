package atlantis.production.constructing.position;

import atlantis.debug.painter.APainter;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import bwapi.Color;

public class TerranPositionFinder extends AbstractPositionFinder {

    /**
     * Returns best position for given <b>building</b>, maximum <b>maxDistance</b> build tiles from
     * <b>nearTo</b>
     * position.<br />
     * It checks if buildings aren't too close one to another and things like that.
     *
     */
    public static APosition findStandardPositionFor(AUnit builder, AUnitType building, HasPosition nearTo,
                                                    double maxDistance) {
        _CONDITION_THAT_FAILED = null;

        int searchRadius = (building.isBase() || building.isCombatBuilding()) ? 0 : 1;
//        int searchRadius = 0;
        maxDistance = limitMaxDistanceForImportantBuildings(maxDistance, building);

        while (searchRadius < maxDistance) {
            int xMin = nearTo.tx() - searchRadius;
            int xMax = nearTo.tx() + searchRadius;
            int yMin = nearTo.ty() - searchRadius;
            int yMax = nearTo.ty() + searchRadius;
            for (int tileX = xMin; tileX <= xMax; tileX++) {
                for (int tileY = yMin; tileY <= yMax; tileY++) {
                    if (tileX == xMin || tileY == yMin || tileX == xMax || tileY == yMax) {
                        APosition constructionPosition = APosition.create(tileX, tileY);
                        if (doesPositionFulfillAllConditions(builder, building, constructionPosition)) {

                            if (building.isCombatBuilding()) {
                                // Turret fix - make sure to build in the same region
                                if (constructionPosition.groundDistanceTo(nearTo) > 1.6 * searchRadius) {
                                    continue;
                                }
                            }

                            return constructionPosition;
                        }
                    }
                }
            }

            searchRadius++;
        }

        return null;
    }

    // =========================================================
    // Hi-level
    /**
     * Returns true if given position (treated as building position for our <b>UnitType building</b>) has all
     * necessary requirements like: doesn't collide with another building, isn't too close to minerals etc.
     */
    public static boolean doesPositionFulfillAllConditions(AUnit builder, AUnitType building, APosition position) {
//        APainter.paintCircle(position, 6, Color.Red);

        if (position == null) {
            _CONDITION_THAT_FAILED = "POSITION ARGUMENT IS NULL";
            return false;
        }

        if (builder == null) {
            builder = Select.ourWorkers().nearestTo(position);
            if (builder == null) {
                _CONDITION_THAT_FAILED = "NO BUILDER ASSIGNED";
                return false;
            }
        }

        // Leave entire horizontal (same tileX) and vertical (same tileY) corridors free for units to pass
        // So disallow building in e.g. 1, 5, 9, 13, 16 horizontally and 3, 7, 11, 15, 19 vertically
        if (isForbiddenByStreetGrid(builder, building, position)) {
            return false;
        }

        // If it's not physically possible to build here (e.g. rocks, other buildings etc)
        if (!canPhysicallyBuildHere(builder, building, position)) {
            return false;
        }

        // If other buildings too close
        if (isOtherConstructionTooClose(builder, building, position)) {
            return false;
        }

        if (isOverlappingBaseLocation(building, position)) {
            return false;
        }

        if (building.isMissileTurret()) {
            return true;
        }

        if (isTooCloseToMainBase(building, position)) {
            return false;
        }

        // Can't be too close to minerals or to geyser, because would slow down production
        if (isTooCloseToMineralsOrGeyser(building, position)) {
            return false;
        }

        // Overlapping a choke point can make it impossible to pass
        if (isTooCloseToChoke(building, position)) {
            return false;
        }

        // Can't be too close to minerals or to geyser, because would slow down production
        return !isNotEnoughPlaceLeftForAddons(builder, building, position);

        // All conditions are fullfilled, return this position
    }

    // =========================================================
    // Low-level


    private static double limitMaxDistanceForImportantBuildings(double maxDistance, AUnitType building) {
        if (building.is(AUnitType.Terran_Academy)) {
            return 8;
        }

//        if (building.isBase()) {
//            return 10;
//        }

        return maxDistance;
    }

    private static boolean isNotEnoughPlaceLeftForAddons(AUnit builder, AUnitType building, APosition position) {
        boolean canThisBuildingHaveAddon = building.canHaveAddon();
        boolean isBase = building.isBase();
        
        // === Compare against existing buildings ========================================
        
        for (AUnit otherBuilding : Select.ourBuildingsWithUnfinished().inRadius(8, position).list()) {
//            double distance = otherBuilding.distTo(position);

            // Check for this building's addon if needed
            if (canThisBuildingHaveAddon && !isBase) {
                if (!canPhysicallyBuildHere(builder, building, position.translateByTiles(2, 0))) {
                    _CONDITION_THAT_FAILED = "MY_ADDON_COULDNT_BE_BUILT_HERE";
                    return true;
                }
            }

            // Check for other buildings' addons
            if (!isBase && otherBuilding.canHaveAddon()) {
                if (!canPhysicallyBuildHere(builder, building, position.translateByTiles(-2, 0))) {
                    _CONDITION_THAT_FAILED = "WOULD_COLLIDE_WITH_ANOTHER_BUILDING_ADDON";
                    return true;
                }
            }
        }

        return false;
    }
}
