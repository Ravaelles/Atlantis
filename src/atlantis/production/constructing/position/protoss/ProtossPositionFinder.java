package atlantis.production.constructing.position.protoss;

import atlantis.Atlantis;
import atlantis.debug.painter.APainter;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.map.AMap;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.production.constructing.position.AbstractPositionFinder;
import atlantis.production.constructing.position.PositionFulfillsAllConditions;
import atlantis.production.constructing.position.TooCloseToRegionBoundaries;
import atlantis.production.constructing.position.conditions.*;
import atlantis.production.constructing.position.terran.TerranForbiddenByStreetGrid;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import bwapi.Color;

public class ProtossPositionFinder extends AbstractPositionFinder {

    /**
     * Returns best position for given <b>building</b>, maximum <b>maxDistance</b> build tiles from
     * <b>nearTo</b>
     * position.<br />
     * It checks if buildings aren't too close one to another and things like that.
     */
    public static APosition findStandardPositionFor(AUnit builder, AUnitType building, HasPosition nearTo, double maxDistance) {
        _CONDITION_THAT_FAILED = null;

        // =========================================================

        if (building.isPylon()) nearTo = defineNearToForPylon(nearTo);

        int searchRadius = (building.isBase() || building.isCombatBuilding()) ? 0 : 1;

        while (searchRadius < maxDistance) {
            int xMin = Math.max(0, nearTo.tx() - searchRadius);
            int xMax = Math.min(AMap.getMapWidthInTiles() - 1, nearTo.tx() + searchRadius);
            int yMin = Math.max(0, nearTo.ty() - searchRadius);
            int yMax = Math.min(AMap.getMapHeightInTiles() - 1, nearTo.ty() + searchRadius);
            for (int tileX = xMin; tileX <= xMax; tileX++) {
                for (int tileY = yMin; tileY <= yMax; tileY++) {
                    if (tileX == xMin || tileY == yMin || tileX == xMax || tileY == yMax) {
                        APosition constructionPosition = APosition.create(tileX, tileY);
                        if (PositionFulfillsAllConditions.doesPositionFulfillAllConditions(
                            builder, building, constructionPosition,
                            nearTo)) {

                            if (building.isCombatBuilding()) {
                                // Turret fix - make sure to build in the same region
                                if (constructionPosition.groundDistanceTo(nearTo) > 1.6 * searchRadius) {
                                    continue;
                                }
                            }

                            return constructionPosition;
                        }
                    }
                }
            }

            searchRadius++;
        }

        return null;
    }

    private static HasPosition defineNearToForPylon(HasPosition nearTo) {
        // First pylon should be close to Nexus for shorter travel dist
        if (AGame.supplyTotal() <= 10) {
            nearTo = PylonPosition.positionForFirstPylon();
//                AAdvancedPainter.paintPosition(nearTo, "PylonPosition");
        }

        // First pylon should be orientated towards the nearest choke
        else if (AGame.supplyTotal() <= 18) {
            nearTo = PylonPosition.positionForSecondPylon(nearTo.position());
        }

        return nearTo;
    }

    // =========================================================
    // Hi-level

    /**
     * Returns true if given position (treated as building position for our <b>UnitType building</b>) has all
     * necessary requirements like: doesn't collide with another building, isn't too close to minerals etc.
     */
//    private static boolean doesPositionFulfillAllConditions(AUnit builder, AUnitType building, APosition position) {
//        _CONDITION_THAT_FAILED = null;
//        if (DEBUG) APainter.paintCircle(position, 5, Color.Red);
//
//        // Check for POWER
//        if (!isPowerConditionFulfilled(building, position)) {
//            _CONDITION_THAT_FAILED = "No power";
//
//            if (A.supplyTotal() >= 20 && Count.inQueueOrUnfinished(AUnitType.Protoss_Pylon, 2) == 0) {
//                AddToQueue.withTopPriority(AUnitType.Protoss_Pylon);
////                System.out.println("Requested Pylon for more powered up surface.");
//            }
//
//            return false;
//        }
//
//        // =========================================================
//
//        // If it's not physically possible to build here (e.g. rocks, other buildings etc)
//        if (!CanPhysicallyBuildHere.check(builder, building, position)) {
//            _CONDITION_THAT_FAILED = "Can't physically build here";
//            return false;
//        }
//
//        // Leave entire horizontal (same tileX) and vertical (same tileY) corridors free for units to pass
//        // So disallow building in e.g. 1, 5, 9, 13, 16 horizontally and 3, 7, 11, 15, 19 vertically
//        if (TerranForbiddenByStreetGrid.isForbiddenByStreetGrid(builder, building, position)) return false;
//
//        // If other buildings too close
//        if (OtherConstructionTooClose.isOtherConstructionTooClose(builder, building, position)) return false;
//
//        // Can't be too close to minerals or to geyser, because would slow down production
//        if (TooCloseToMineralsOrGeyser.isTooCloseToMineralsOrGeyser(building, position)) return false;
//
//        if (OverlappingBaseLocation.isOverlappingBaseLocation(building, position)) return false;
//
//        // Overlapping a choke point can make it impossible to pass
//        if (TooCloseToChoke.isTooCloseToChoke(building, position)) return false;
//
//        // Let's spread pylons a bit initially so they power more space
//        if (building.isPylon() && isTooCloseToOtherPylons(position)) return false;
//
//        if (TooCloseToRegionBoundaries.isTooCloseToRegionBoundaries(position)) return false;
//
//        // All conditions are fullfilled, return this position
//        if (DEBUG) APainter.paintCircle(position, 5, Color.Green);
//        if (DEBUG) {
////            A.centerAndPause(position);
////            A.centerAndChangeSpeed(position, 1);
//        }
//        return true;
//    }
}
