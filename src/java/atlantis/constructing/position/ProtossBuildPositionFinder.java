package atlantis.constructing.position;

import atlantis.Atlantis;
import static atlantis.constructing.position.AbstractBuildPositionFinder.canPhysicallyBuildHere;
import static atlantis.constructing.position.AbstractBuildPositionFinder.otherBuildingsTooClose;
import atlantis.wrappers.SelectUnits;
import jnibwapi.Position;
import jnibwapi.Unit;
import jnibwapi.types.UnitType;

public class ProtossBuildPositionFinder extends AbstractBuildPositionFinder {

    /**
     * Returns best position for given <b>building</b>, maximum <b>maxDistance</b> build tiles from
     * <b>nearTo</b>
     * position.<br />
     * It checks if buildings aren't too close one to another and things like that.
     *
     */
    public static Position findStandardPositionFor(Unit builder, UnitType building, Position nearTo, double maxDistance) {
        ConstructionBuildPositionFinder.building = building;
        ConstructionBuildPositionFinder.nearTo = nearTo;
        ConstructionBuildPositionFinder.maxDistance = maxDistance;

        // =========================================================
        int searchRadius = building.isType(UnitType.UnitTypes.Terran_Supply_Depot) ? 8 : 0;

        while (searchRadius < maxDistance) {
            int xCounter = 0;
            int yCounter = 0;
            int doubleRadius = searchRadius * 2;
            for (int tileX = nearTo.getBX() - searchRadius; tileX <= nearTo.getBX() + searchRadius; tileX++) {
                for (int tileY = nearTo.getBY() - searchRadius; tileY <= nearTo.getBY() + searchRadius; tileY++) {
                    if (xCounter == 0 || yCounter == 0 || xCounter == doubleRadius || yCounter == doubleRadius) {
                        Position position = new Position(tileX, tileY, Position.PosType.BUILD);
                        if (doesPositionFulfillAllConditions(builder, position)) {
                            return position;
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
    private static boolean doesPositionFulfillAllConditions(Unit builder, Position position) {

        // Check for POWER
        if (!isPowerConditionFulfilled(position)) {
            return false;
        }

        // =========================================================
        // If it's not physically possible to build here (e.g. rocks, other buildings etc)
        if (!canPhysicallyBuildHere(builder, ConstructionBuildPositionFinder.building, position)) {
            return false;
        }

        // If other buildings too close
        if (otherBuildingsTooClose(builder, ConstructionBuildPositionFinder.building, position)) {
            return false;
        }

        // Can't be too close to minerals or to geyser, because would slow down production
        if (isTooCloseToMineralsOrGeyser(ConstructionBuildPositionFinder.building, position)) {
            return false;
        }

        // All conditions are fullfilled, return this position
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

    private static boolean isPowerConditionFulfilled(Position position) {
        return Atlantis.getBwapi().hasPower(position)
                || ConstructionBuildPositionFinder.building.equals(UnitType.UnitTypes.Protoss_Nexus)
                || ConstructionBuildPositionFinder.building.equals(UnitType.UnitTypes.Protoss_Pylon);
    }
}
