package atlantis.constructing.position;

import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;
import atlantis.wrappers.APosition;
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
//        building = building;
//        AtlantisPositionFinder.nearTo = nearTo;
//        AtlantisPositionFinder.maxDistance = maxDistance;

        // =========================================================
        int searchRadius = building.equals(AUnitType.Terran_Supply_Depot) ? 8 : 0;

        while (searchRadius < maxDistance) {
            int xCounter = 0;
            int yCounter = 0;
            int doubleRadius = searchRadius * 2;
            
            for (int tileX = nearTo.getTileX() - searchRadius; tileX <= nearTo.getTileX() + searchRadius; tileX++) {
                for (int tileY = nearTo.getTileY() - searchRadius; tileY <= nearTo.getTileY() + searchRadius; tileY++) {
                    if (xCounter == 0 || yCounter == 0 || xCounter == doubleRadius || yCounter == doubleRadius) {
                        APosition constructionPosition = new APosition(tileX * 32, tileY * 32);
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
    public static boolean doesPositionFulfillAllConditions(AUnit builder, AUnitType building, APosition position) {
        if (builder == null) {
            return false;
        }
        if (position == null) {
            return false;
        }

        // If it's not physically possible to build here (e.g. rocks, other buildings etc)
        if (!canPhysicallyBuildHere(builder, building, position)) {
            return false;
        }

        // If other buildings too close
        if (otherBuildingsTooClose(builder, building, position)) {
            return false;
        }

        // Can't be too close to minerals or to geyser, because would slow down production
        if (isTooCloseToMineralsOrGeyser(building, position)) {
            return false;
        }

        // All conditions are fullfilled, return this position
        return true;
    }

    // =========================================================
    // Lo-level
    private static boolean isTooCloseToMineralsOrGeyser(AUnitType building, APosition position) {

        // We have problem only if building is both close to base and to minerals or to geyser
        AUnit nearestBase = Select.ourBases().nearestTo(position);
        if (nearestBase != null && nearestBase.distanceTo(position) <= 7) {
        	Collection<AUnit> mineralsInRange = 
                  (Collection<AUnit>) Select.minerals().inRadius(8, position).listUnits();
            for (AUnit mineral : mineralsInRange) {
                if (mineral.distanceTo(position) <= 4) {
                    return true;
                }
            }
        }
        return false;
    }
}
