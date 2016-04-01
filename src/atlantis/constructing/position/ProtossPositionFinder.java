package atlantis.constructing.position;

import java.util.Collection;

import atlantis.Atlantis;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.util.PositionUtil;
import atlantis.units.Select;
import bwapi.Position;
import bwapi.TilePosition;

import bwapi.UnitType;

public class ProtossPositionFinder extends AbstractPositionFinder {

    /**
     * Returns best position for given <b>building</b>, maximum <b>maxDistance</b> build tiles from
     * <b>nearTo</b>
     * position.<br />
     * It checks if buildings aren't too close one to another and things like that.
     *
     */
    public static Position findStandardPositionFor(AUnit builder, AUnitType building, Position nearTo, double maxDistance) {
        AtlantisPositionFinder.building = building;
        AtlantisPositionFinder.nearTo = nearTo;
        AtlantisPositionFinder.maxDistance = maxDistance;

        // =========================================================
        int searchRadius = building.equals(AUnitType.Protoss_Pylon) ? 6 : 0;

        while (searchRadius < maxDistance) {
            int xCounter = 0;
            int yCounter = 0;
            int doubleRadius = searchRadius * 2;
            TilePosition tileNearTo = nearTo.toTilePosition();	//TODO? check the validity of this conversion 
            for (int tileX = tileNearTo.getX() - searchRadius; tileX <= tileNearTo.getX() + searchRadius; tileX++) {
                for (int tileY = tileNearTo.getY() - searchRadius; tileY <= tileNearTo.getY() + searchRadius; tileY++) {
                    if (xCounter == 0 || yCounter == 0 || xCounter == doubleRadius || yCounter == doubleRadius) {
                        TilePosition tilePosition = new TilePosition(tileX, tileY);	//TODO? check the validity of this conversion 
                        if (doesPositionFulfillAllConditions(builder, tilePosition.toPosition())) {
                            return tilePosition.toPosition();
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
    private static boolean doesPositionFulfillAllConditions(AUnit builder, Position position) {

        // Check for POWER
        if (!isPowerConditionFulfilled(position)) {
            return false;
        }

        // =========================================================
        // If it's not physically possible to build here (e.g. rocks, other buildings etc)
        if (!canPhysicallyBuildHere(builder, AtlantisPositionFinder.building, position)) {
            return false;
        }

        // If other buildings too close
        if (otherBuildingsTooClose(builder, AtlantisPositionFinder.building, position)) {
            return false;
        }

        // Can't be too close to minerals or to geyser, because would slow down production
        if (isTooCloseToMineralsOrGeyser(AtlantisPositionFinder.building, position)) {
            return false;
        }

        // All conditions are fullfilled, return this position
        return true;
    }

    // =========================================================
    // Lo-level
    private static boolean isTooCloseToMineralsOrGeyser(AUnitType building, Position position) {

        // We have problem only if building is both close to base and to minerals or to geyser
        AUnit nearestBase = Select.ourBases().nearestTo(position);
        if (nearestBase != null && nearestBase.distanceTo(position) <= 8) {
        	Collection<AUnit> mineralsInRange = (Collection<AUnit>) Select.minerals().inRadius(8, position).listUnits();
            for (AUnit mineral : mineralsInRange) {
                if (mineral.distanceTo(position) <= 4) {
                    return true;
                }
            }
        	Collection<AUnit> geysersInRange = (Collection<AUnit>) Select.geysers().inRadius(8, position).listUnits();
            for (AUnit geyser : geysersInRange) {
                if (geyser.distanceTo(position) <= 4) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isPowerConditionFulfilled(Position position) {
        return Atlantis.getBwapi().hasPower(position.toTilePosition())
                || AtlantisPositionFinder.building.equals(AUnitType.Protoss_Nexus)
                || AtlantisPositionFinder.building.equals(AUnitType.Protoss_Pylon);
    }
}
