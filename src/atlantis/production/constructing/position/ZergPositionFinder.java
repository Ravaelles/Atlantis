package atlantis.production.constructing.position;

import atlantis.Atlantis;
import atlantis.debug.APainter;
import atlantis.position.APosition;
import atlantis.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import bwapi.Color;
import bwapi.Position;

public class ZergPositionFinder extends AbstractPositionFinder {

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
//        building = building;
//        AtlantisPositionFinder.nearTo = nearTo;
//        AtlantisPositionFinder.maxDistance = maxDistance;

        // =========================================================

        int searchRadius = building.isCombatBuilding() ? 0 : 4;
        if (building.isBase()) {
            maxDistance = 15;
        }
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
        APainter.paintCircle(position, 10, Color.Red);

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
            _CONDITION_THAT_FAILED = "BUILDINGS TOO CLOSE";
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
    
//    private static boolean isTooCloseToMineralsOrGeyser(AUnitType building, APosition position) {
//
//        // We have problem only if building is both close to base and to minerals or to geyser
//        AUnit nearestBase = Select.ourBases().nearestTo(position);
//        if (nearestBase != null && nearestBase.distTo(position) <= 7) {
//            Collection<AUnit> mineralsInRange = Select.minerals().inRadius(8, position).listUnits();
//            for (AUnit mineral : mineralsInRange) {
//                if (mineral.distTo(position) <= 4) {
//                    return true;
//                }
//            }
//        }
//        return false;
//    }

    private static boolean isCreepConditionFulfilled(AUnitType building, Position position) {
        return Atlantis.game().hasCreep(position.toTilePosition())
                || building.equals(AUnitType.Zerg_Hatchery)
                || building.equals(AUnitType.Zerg_Extractor);
    }

}
