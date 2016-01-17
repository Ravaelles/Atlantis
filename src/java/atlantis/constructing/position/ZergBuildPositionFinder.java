package atlantis.constructing.position;

import atlantis.Atlantis;
import atlantis.AtlantisConfig;
import static atlantis.constructing.position.AbstractBuildPositionFinder.canPhysicallyBuildHere;
import static atlantis.constructing.position.AbstractBuildPositionFinder.otherBuildingsTooClose;
import atlantis.wrappers.SelectUnits;
import jnibwapi.Position;
import jnibwapi.Unit;
import jnibwapi.types.UnitType;
import static atlantis.constructing.position.AbstractBuildPositionFinder.canPhysicallyBuildHere;
import static atlantis.constructing.position.AbstractBuildPositionFinder.canPhysicallyBuildHere;
import static atlantis.constructing.position.AbstractBuildPositionFinder.canPhysicallyBuildHere;

public class ZergBuildPositionFinder extends AbstractBuildPositionFinder {
    
    protected static String _CONDITION_THAT_FAILED = null;
    
    // =========================================================

    /**
     * Returns best position for given <b>building</b>, maximum <b>maxDistance</b> build tiles from
     * <b>nearTo</b>
     * position.<br />
     * It checks if buildings aren't too close one to another and things like that.
     *
     */
    public static Position findStandardPositionFor(Unit builder, UnitType building, Position nearTo, double maxDistance) {
        _CONDITION_THAT_FAILED = null;
        
        ConstructionBuildPositionFinder.building = building;
        ConstructionBuildPositionFinder.nearTo = nearTo;
        ConstructionBuildPositionFinder.maxDistance = maxDistance;

        // =========================================================
        int searchRadius = 3;
        if (building.isType(AtlantisConfig.BASE)) {
            searchRadius = 0;
        }
        if (building.isType(AtlantisConfig.SUPPLY)) {
            searchRadius = 8;
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
//                            System.out.println("Position for " + building + " found at: " + position);
                            return position;
                        }
                        System.out.println("    [" + position + "]  Condition failed = " + _CONDITION_THAT_FAILED);
                    }

                    yCounter++;
                }
                xCounter++;
            }

            searchRadius++;
        }
        System.out.println("## No success with searchRadius = " + searchRadius);
        System.out.println("## Last condition that failed = " + _CONDITION_THAT_FAILED);

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

        // --------------------------------------------------------------------
        // If it's not physically possible to build here (e.g. rocks, other buildings etc)
        if (!canPhysicallyBuildHere(builder, ConstructionBuildPositionFinder.building, position)) {
//            System.out.println(builder + " / " + ConstructionBuildPositionFinder.building + " / " + position);
            _CONDITION_THAT_FAILED = "CAN'T PHYSICALLY BUILD";
            return false;
        }

        // If other buildings too close
        if (otherBuildingsTooClose(builder, ConstructionBuildPositionFinder.building, position)) {
//            _CONDITION_THAT_FAILED = "BUILDINGS TOO CLOSE";
            return false;
        }

        // Can't be too close to minerals or to geyser, because would slow down production
        if (isTooCloseToMineralsOrGeyser(ConstructionBuildPositionFinder.building, position)) {
            _CONDITION_THAT_FAILED = "TOO CLOSE TO MINERALS OR GEYSER";
            return false;
        }

        // All conditions are fullfilled, return this position
        _CONDITION_THAT_FAILED = null;
        return true;
    }

    // =========================================================
    // Lo-level
    private static boolean isTooCloseToMineralsOrGeyser(UnitType building, Position position) {

        // We have problem only if building is both close to base and to minerals or to geyser
        Unit nearestBase = SelectUnits.ourBases().nearestTo(position);
        if (nearestBase != null && nearestBase.distanceTo(position) <= 7) {
            for (Unit mineral : SelectUnits.minerals().inRadius(8, position).list()) {
                if (mineral.distanceTo(position) <= 4) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isCreepConditionFulfilled(Position position) {
        return Atlantis.getBwapi().hasCreep(position)
                || ConstructionBuildPositionFinder.building.equals(UnitType.UnitTypes.Zerg_Hatchery)
                || ConstructionBuildPositionFinder.building.equals(UnitType.UnitTypes.Zerg_Extractor);
    }

}
