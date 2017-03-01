package atlantis.constructing.position;

import atlantis.Atlantis;
import atlantis.AtlantisConfig;
import atlantis.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;
import bwapi.Position;
import java.util.Collection;

public class ZergPositionFinder extends AbstractPositionFinder {

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
//        building = building;
//        AtlantisPositionFinder.nearTo = nearTo;
//        AtlantisPositionFinder.maxDistance = maxDistance;

        // =========================================================
        int searchRadius = 6;
        if (building.equals(AtlantisConfig.BASE)) {
            searchRadius = 0;
        }
        if (building.equals(AtlantisConfig.SUPPLY)) {
            searchRadius = 8;
        }
        if (maxDistance < searchRadius) {
            maxDistance = searchRadius;
        }

        while (searchRadius < maxDistance) {
            int xCounter = 0;
            int yCounter = 0;
            int doubleRadius = searchRadius * 2;
            
            for (int tileX = nearTo.getTileX() - searchRadius; tileX <= nearTo.getTileX() + searchRadius; tileX++) {
                for (int tileY = nearTo.getTileY() - searchRadius; tileY <= nearTo.getTileY() + searchRadius; tileY++) {
                    if (xCounter == 0 || yCounter == 0 || xCounter == doubleRadius || yCounter == doubleRadius) {
                        APosition constructionPosition = APosition.create(tileX, tileY);
                        if (doesPositionFulfillAllConditions(builder,building, constructionPosition)) {
//                            AtlantisPainter.paintRectangle(constructionPosition, 32, 32, Color.Green);
                            return constructionPosition;
                        }
//                        else {
//                            AtlantisPainter.paintRectangle(constructionPosition, 32, 32, Color.Red);
//                        }
                    }

                    yCounter++;
                }
                xCounter++;
            }

            searchRadius++;
        }
//        System.out.println("##### No success with searchRadius = " + searchRadius);
//        System.err.println("##### Last condition that failed = `" + _CONDITION_THAT_FAILED + "` for " 
//              + building + " with searchRadius = " + searchRadius);

        return null;
    }

    // =========================================================
    // Hi-level
    /**
     * Returns true if given position (treated as building position for our <b>UnitType building</b>) has all
     * necessary requirements like: doesn't collide with another building, isn't too close to minerals etc.
     */
    public static boolean doesPositionFulfillAllConditions(AUnit builder, AUnitType building, APosition position) {

        // Check for CREEP
        if (!isCreepConditionFulfilled(building, position)) {
            _CONDITION_THAT_FAILED = "CREEP";
            return false;
        }

        // =========================================================
        // If it's not physically possible to build here (e.g. rocks, other buildings etc)
        if (!canPhysicallyBuildHere(builder, building, position)) {
//            System.out.println(builder + " / " + ConstructionBuildPositionFinder.building + " / " + position);
            _CONDITION_THAT_FAILED = "CAN'T PHYSICALLY BUILD";
            return false;
        }

        // If other buildings too close
        if (isOtherConstructionTooClose(builder, building, position)) {
//            _CONDITION_THAT_FAILED = "BUILDINGS TOO CLOSE";
            return false;
        }

        // Can't be too close to minerals or to geyser, because would slow down production
        if (isTooCloseToMineralsOrGeyser(building, position)) {
            _CONDITION_THAT_FAILED = "TOO CLOSE TO MINERALS OR GEYSER";
            return false;
        }

        // All conditions are fullfilled, return this position
        _CONDITION_THAT_FAILED = null;
        return true;
    }

    // =========================================================
    // Lo-level
    
    private static boolean isTooCloseToMineralsOrGeyser(AUnitType building, Position position) {

        // We have problem only if building is both close to base and to minerals or to geyser
        AUnit nearestBase = Select.ourBases().nearestTo(position);
        if (nearestBase != null && nearestBase.distanceTo(position) <= 7) {
            Collection<AUnit> mineralsInRange = (Collection<AUnit>) Select.minerals().inRadius(8, position).listUnits();
            for (AUnit mineral : mineralsInRange) {
                if (mineral.distanceTo(position) <= 4) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isCreepConditionFulfilled(AUnitType building, Position position) {
        return Atlantis.getBwapi().hasCreep(position.toTilePosition())
                || building.equals(AUnitType.Zerg_Hatchery)
                || building.equals(AUnitType.Zerg_Extractor);
    }

}
