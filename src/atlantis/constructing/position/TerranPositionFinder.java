package atlantis.constructing.position;

import atlantis.information.AMap;
import atlantis.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;
import java.util.Collection;

public class TerranPositionFinder extends AbstractPositionFinder {

    /**
     * Returns best position for given <b>building</b>, maximum <b>maxDistance</b> build tiles from
     * <b>nearTo</b>
     * position.<br />
     * It checks if buildings aren't too close one to another and things like that.
     *
     */
    public static APosition findStandardPositionFor(AUnit builder, AUnitType building, APosition nearTo,
            double maxDistance) {
        _CONDITION_THAT_FAILED = null;

        // =========================================================
//        int searchRadius = building.equals(AUnitType.Terran_Supply_Depot) ? 0 : 0;
        int searchRadius = 0;

        while (searchRadius < maxDistance) {
            int xCounter = 0;
            int yCounter = 0;
            int doubleRadius = searchRadius * 2;

            // Search horizontally
            int minTileX = Math.max(0, nearTo.getTileX() - searchRadius);
            int maxTileX = Math.min(nearTo.getTileX() + searchRadius, AMap.getMapWidthInTiles() - 1);
            for (int tileX = minTileX; tileX <= maxTileX; tileX++) {
                
                // Search veritcally
                int minTileY = Math.max(0, nearTo.getTileY() - searchRadius);
                int maxTileY = Math.min(nearTo.getTileY() + searchRadius, AMap.getMapHeightInTiles() - 1);
                for (int tileY = minTileY; tileY <= maxTileY; tileY++) {
                    if (xCounter == 0 || yCounter == 0 || xCounter == doubleRadius || yCounter == doubleRadius) {
//                        System.out.println(tileX + " / " + tileY);
                        APosition constructionPosition = APosition.create(tileX, tileY);
                        
                        // Check if position is buildable etc
                        if (doesPositionFulfillAllConditions(builder, building, constructionPosition)) {
                            return constructionPosition;
                        }
                    }

                    yCounter++;
                }
                xCounter++;
            }

            searchRadius++;
        }
        
        System.err.println("Finished at search radius: " + searchRadius + " from " + nearTo);
        System.err.println("   (Last error: " + AbstractPositionFinder._CONDITION_THAT_FAILED + ")");

        return null;
    }

    // =========================================================
    // Hi-level
    /**
     * Returns true if given position (treated as building position for our <b>UnitType building</b>) has all
     * necessary requirements like: doesn't collide with another building, isn't too close to minerals etc.
     */
    public static boolean doesPositionFulfillAllConditions(AUnit builder, AUnitType building, APosition position) {
        if (builder == null) {
            _CONDITION_THAT_FAILED = "NO BUILDER ASSIGNED";
            return false;
        }
        if (position == null) {
            _CONDITION_THAT_FAILED = "POSITION ARGUMENT IS NULL";
            return false;
        }

        // Leave entire horizontal (same tileX) and vertical (same tileY) corridors free for units to pass
        // So disallow building in e.g. 1, 5, 9, 13, 16 horizontally and 3, 7, 11, 15, 19 vertically
        if (isForbiddenByStreetBlock(builder, building, position)) {
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

        // Can't be too close to minerals or to geyser, because would slow down production
        if (isTooCloseToMineralsOrGeyser(building, position)) {
            return false;
        }

        // Can't be too close to minerals or to geyser, because would slow down production
        if (isNotEnoughPlaceLeftForAddons(builder, building, position)) {
            return false;
        }

        // All conditions are fullfilled, return this position
        return true;
    }

    // =========================================================
    // Low-level
    
    private static boolean isTooCloseToMineralsOrGeyser(AUnitType building, APosition position) {

        // We have problem only if building is both close to base and to minerals or to geyser
        AUnit nearestBase = Select.ourBases().nearestTo(position);
        if (nearestBase != null && nearestBase.distanceTo(position) <= 3) {
            Collection<AUnit> mineralsInRange
                    = (Collection<AUnit>) Select.minerals().inRadius(3, position).listUnits();
            for (AUnit mineral : mineralsInRange) {
                if (mineral.distanceTo(position) <= 2) {
                    _CONDITION_THAT_FAILED = "MINERAL TOO CLOSE";
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isNotEnoughPlaceLeftForAddons(AUnit builder, AUnitType building, APosition position) {
        boolean canThisBuildingHaveAddon = building.canHaveAddon();
        boolean isBase = building.isBase();
        
        // === Compare against existing buildings ========================================
        
        for (AUnit otherBuilding : Select.ourBuildingsIncludingUnfinished().inRadius(8, position).listUnits()) {
            double distance = otherBuilding.getPosition().distanceTo(position);

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
