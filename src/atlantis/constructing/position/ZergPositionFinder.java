package atlantis.constructing.position;

import java.util.Collection;

import atlantis.Atlantis;
import atlantis.AtlantisConfig;
import atlantis.units.Select;
import bwapi.Position;
import bwapi.TilePosition;

import bwapi.UnitType;
import atlantis.debug.AtlantisPainter;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.util.PositionUtil;

public class ZergPositionFinder extends AbstractPositionFinder {
    
    /**
     * Returns best position for given <b>building</b>, maximum <b>maxDistance</b> build tiles from
     * <b>nearTo</b>
     * position.<br />
     * It checks if buildings aren't too close one to another and things like that.
     *
     */
    public static Position findStandardPositionFor(AUnit builder, AUnitType building, Position nearTo, double maxDistance) {
        _CONDITION_THAT_FAILED = null;
        
        AtlantisPositionFinder.building = building;
        AtlantisPositionFinder.nearTo = nearTo;
        AtlantisPositionFinder.maxDistance = maxDistance;

        // =========================================================
        int searchRadius = 6;
        if (building.equals(AtlantisConfig.BASE)) {
            searchRadius = 0;
        }
        if (building.equals(AtlantisConfig.SUPPLY)) {
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
            TilePosition tileNearTo = nearTo.toTilePosition();	//TODO? check the validity of this conversion
            for (int tileX = tileNearTo.getX() - searchRadius; tileX <= tileNearTo.getX() + searchRadius; tileX++) {
                for (int tileY = tileNearTo.getY() - searchRadius; tileY <= tileNearTo.getY() + searchRadius; tileY++) {
//                    System.out.println(xCounter + ", " + yCounter);
                    if ((xCounter == 0 || xCounter == doubleRadius) || (yCounter == 0 || yCounter == doubleRadius)) {
                    	TilePosition tilePosition = new TilePosition(tileX, tileY);	//TODO? check the validity of this conversion 
//                        System.out.println("tile [" + tileX + ", " + tileY + "]");
                        if (doesPositionFulfillAllConditions(builder, tilePosition.toPosition())) {
//                            System.out.println("--------------------------------------------------------");
//                            System.out.println("--- Position for " + building + " found at: " + position);
//                            System.out.println("--------------------------------------------------------");
                            return tilePosition.toPosition();
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
    private static boolean doesPositionFulfillAllConditions(AUnit builder, Position position) {

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

    private static boolean isCreepConditionFulfilled(Position position) {
        return Atlantis.getBwapi().hasCreep(position.toTilePosition())
                || AtlantisPositionFinder.building.equals(AUnitType.Zerg_Hatchery)
                || AtlantisPositionFinder.building.equals(AUnitType.Zerg_Extractor);
    }

}
