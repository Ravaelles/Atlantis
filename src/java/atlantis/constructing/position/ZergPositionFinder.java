package atlantis.constructing.position;

import atlantis.Atlantis;
import atlantis.AtlantisConfig;
import atlantis.wrappers.SelectUnits;
import jnibwapi.Position;
import jnibwapi.Unit;
import jnibwapi.types.UnitType;
import atlantis.debug.AtlantisPainter;

public class ZergPositionFinder extends AbstractPositionFinder {
    
    /**
     * Returns best position for given <b>building</b>, maximum <b>maxDistance</b> build tiles from
     * <b>nearTo</b>
     * position.<br />
     * It checks if buildings aren't too close one to another and things like that.
     *
     */
    public static Position findStandardPositionFor(Unit builder, UnitType building, Position nearTo, double maxDistance) {
        _CONDITION_THAT_FAILED = null;
        
        AtlantisPositionFinder.building = building;
        AtlantisPositionFinder.nearTo = nearTo;
        AtlantisPositionFinder.maxDistance = maxDistance;

        // =========================================================
        int searchRadius = 5;
        if (building.isType(AtlantisConfig.BASE)) {
            searchRadius = 0;
        }
        if (building.isType(AtlantisConfig.SUPPLY)) {
            searchRadius = 8;
        }
        if (maxDistance < searchRadius) {
            System.err.println("---------------------");
            System.err.println("Smaller maxDistance than search radius for: " + building);
            System.err.println("  maxDistance = " + maxDistance);
            System.err.println("  searchRadius = " + searchRadius);
            System.err.println("---------------------");
            maxDistance = searchRadius;
        }

        while (searchRadius < maxDistance) {
            int xCounter = 0;
            int yCounter = 0;
            int doubleRadius = searchRadius * 2;
            for (int tileX = nearTo.getBX() - searchRadius; tileX <= nearTo.getBX() + searchRadius; tileX++) {
                for (int tileY = nearTo.getBY() - searchRadius; tileY <= nearTo.getBY() + searchRadius; tileY++) {
//                    System.out.println(xCounter + ", " + yCounter);
                    if ((xCounter == 0 || xCounter == doubleRadius) || (yCounter == 0 || yCounter == doubleRadius)) {
                        Position position = new Position(tileX, tileY, Position.PosType.BUILD);
//                        System.out.println("tile [" + tileX + ", " + tileY + "]");
                        if (doesPositionFulfillAllConditions(builder, position)) {
//                            System.out.println("--------------------------------------------------------");
//                            System.out.println("--- Position for " + building + " found at: " + position);
//                            System.out.println("--------------------------------------------------------");
                            return position;
                        }
//                        System.out.println("    [" + position + "]  Condition failed = " + _CONDITION_THAT_FAILED);
                    }

                    yCounter++;
                }
                xCounter++;
            }

            searchRadius++;
        }
//        System.out.println("##### No success with searchRadius = " + searchRadius);
//        System.err.println("##### Last condition that failed = `" + _CONDITION_THAT_FAILED + "` for " + building + " with searchRadius = " + searchRadius);

        return null;
    }

    // =========================================================
    // Hi-level
    
    /**
     * Returns true if given position (treated as building position for our <b>UnitType building</b>) has all
     * necessary requirements like: doesn't collide with another building, isn't too close to minerals etc.
     */
    private static boolean doesPositionFulfillAllConditions(Unit builder, Position position) {

        // Check for CREEP
        if (!isCreepConditionFulfilled(position)) {
            _CONDITION_THAT_FAILED = "CREEP";
            return false;
        }

        // =========================================================
        // If it's not physically possible to build here (e.g. rocks, other buildings etc)
        if (!canPhysicallyBuildHere(builder, AtlantisPositionFinder.building, position)) {
//            System.out.println(builder + " / " + ConstructionBuildPositionFinder.building + " / " + position);
            _CONDITION_THAT_FAILED = "CAN'T PHYSICALLY BUILD";
            return false;
        }

        // If other buildings too close
        if (otherBuildingsTooClose(builder, AtlantisPositionFinder.building, position)) {
//            _CONDITION_THAT_FAILED = "BUILDINGS TOO CLOSE";
            return false;
        }

        // Can't be too close to minerals or to geyser, because would slow down production
        if (isTooCloseToMineralsOrGeyser(AtlantisPositionFinder.building, position)) {
            _CONDITION_THAT_FAILED = "TOO CLOSE TO MINERALS OR GEYSER";
            return false;
        }

        // All conditions are fullfilled, return this position
        _CONDITION_THAT_FAILED = null;
        return true;
    }

    // =========================================================
    // Lo-level

    private static boolean isCreepConditionFulfilled(Position position) {
        return Atlantis.getBwapi().hasCreep(position)
                || AtlantisPositionFinder.building.equals(UnitType.UnitTypes.Zerg_Hatchery)
                || AtlantisPositionFinder.building.equals(UnitType.UnitTypes.Zerg_Extractor);
    }

}
