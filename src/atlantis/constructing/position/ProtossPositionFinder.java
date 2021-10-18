package atlantis.constructing.position;

import atlantis.AGame;
import atlantis.Atlantis;
import atlantis.debug.APainter;
import atlantis.map.ABaseLocation;
import atlantis.map.AMap;
import atlantis.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;
import bwapi.Color;

public class ProtossPositionFinder extends AbstractPositionFinder {

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
        int initSearchRadius = building.isPylon() ? 6 : 0;

        int searchRadius = initSearchRadius;
        while (searchRadius < maxDistance) {
            int xCounter = 0;
            int yCounter = 0;

            int xMin = nearTo.getTileX() - searchRadius;
            int yMin = nearTo.getTileY() - searchRadius;
            int xMax = nearTo.getTileX() + searchRadius;
            int yMax = nearTo.getTileY() + searchRadius;
            for (int tileX = xMin; tileX <= xMax; tileX++) {
                for (int tileY = yMin; tileY <= yMax; tileY++) {
                    if (xCounter == xMin || yCounter == yMin || xCounter == 0 || yCounter == 0 || xCounter == xMax || yCounter == yMax) {
                        APosition constructionPosition = APosition.create(tileX, tileY);
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

        return null;
    }

    // =========================================================
    // Hi-level

    /**
     * Returns true if given position (treated as building position for our <b>UnitType building</b>) has all
     * necessary requirements like: doesn't collide with another building, isn't too close to minerals etc.
     */
    private static boolean doesPositionFulfillAllConditions(AUnit builder, AUnitType building, APosition position) {
        APainter.paintCircle(position, 5, Color.Red);

        // Check for POWER
        if (!isPowerConditionFulfilled(building, position)) {
            return false;
        }

        // =========================================================

        // If it's not physically possible to build here (e.g. rocks, other buildings etc)
        if (!canPhysicallyBuildHere(builder, building, position)) {
            return false;
        }

        // Leave entire horizontal (same tileX) and vertical (same tileY) corridors free for units to pass
        // So disallow building in e.g. 1, 5, 9, 13, 16 horizontally and 3, 7, 11, 15, 19 vertically
        if (isForbiddenByStreetGrid(builder, building, position)) {
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

        if (isOverlappingBaseLocation(building, position)) {
            return false;
        }

        if (building.isPylon() && isTooCloseToOtherPylons(position)) {
            return false;
        }

        // All conditions are fullfilled, return this position
        APainter.paintCircle(position, 5, Color.Green);
        return true;
    }

    // =========================================================
    // Lo-level

    private static boolean isTooCloseToOtherPylons(APosition position) {
        int pylonsNearby;

        if (AGame.getSupplyUsed() < 25) {
            pylonsNearby = Select.ourOfType(AUnitType.Protoss_Pylon).inRadius(7, position).count();
        }
        else if (AGame.getSupplyUsed() < 35) {
            pylonsNearby = Select.ourOfType(AUnitType.Protoss_Pylon).inRadius(5.5, position).count();
        }
        else if (AGame.getSupplyUsed() < 70) {
            pylonsNearby = Select.ourOfType(AUnitType.Protoss_Pylon).inRadius(4, position).count();
        }
        else if (AGame.getSupplyUsed() < 100) {
            pylonsNearby = Select.ourOfType(AUnitType.Protoss_Pylon).inRadius(2, position).count();
        } else {
            pylonsNearby = -1;
        }

        _CONDITION_THAT_FAILED = "Too close to other pylons (" + pylonsNearby + ")";
        return pylonsNearby > 0;
    }

    private static boolean isTooCloseToMineralsOrGeyser(AUnitType building, APosition position) {

        // We have problem only if building is both close to base and to minerals or to geyser
        AUnit nearestBase = Select.ourBases().nearestTo(position);
        if (nearestBase != null && nearestBase.distanceTo(position) <= 14) {
            for (AUnit mineral : Select.minerals().inRadius(8, position).listUnits()) {
                if (mineral.distanceTo(position) <= (building.isPylon() ? 5 : 4)) {
                    _CONDITION_THAT_FAILED = "Too close to mineral";
                    return true;
                }
            }

            for (AUnit geyser : Select.geysers().inRadius(8, position).listUnits()) {
                if (geyser.distanceTo(position) <= (building.isPylon() ? 5 : 4)) {
                    _CONDITION_THAT_FAILED = "Too close to geyser";
                    return true;
                }
            }
        }

        return false;
    }

    private static boolean isPowerConditionFulfilled(AUnitType building, APosition position) {
        return Atlantis.game().hasPower(position.toTilePosition())
                || building.isPylon()
                || building.equals(AUnitType.Protoss_Nexus);
    }
}
