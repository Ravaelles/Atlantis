package atlantis.production.constructing.position.protoss;

import atlantis.map.AMap;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.production.constructing.position.AbstractPositionFinder;
import atlantis.production.constructing.position.PositionFulfillsAllConditions;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;

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

//        int searchRadius = (building.isBase() || building.isCombatBuilding()) ? 0 : 1;
        int searchRadius = 0;

//        boolean logToFile = building.isGateway();
//        if (logToFile) LogToFile.info("------------\n");

        int xMapMax = AMap.getMapWidthInTiles() - 1;
        int yMapMax = AMap.getMapHeightInTiles() - 1;

        while (searchRadius < maxDistance) {
            int xMin = Math.max(0, nearTo.tx() - searchRadius);
            int xMax = Math.min(xMapMax, nearTo.tx() + searchRadius);
            int yMin = Math.max(0, nearTo.ty() - searchRadius);
            int yMax = Math.min(yMapMax, nearTo.ty() + searchRadius);

            for (int tileX = xMin; tileX <= xMax; tileX++) {
                for (int tileY = yMin; tileY <= yMax; tileY++) {
                    if (tileX == xMin || tileY == yMin || tileX == xMax || tileY == yMax) {
//                        if (logToFile) LogToFile.info("tx,ty: [" + tileX + "," + tileY + "]\n");

                        APosition constructionPosition = APosition.create(tileX, tileY);
                        if (PositionFulfillsAllConditions.doesPositionFulfillAllConditions(
                            builder, building, constructionPosition, nearTo
                        )) {
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
//        int supply = A.supplyTotal();
        int pylons = Count.pylons();

//        System.err.println("pylons = " + pylons);

        if (pylons >= 3) return nearTo;

        // First pylon should be close to Nexus for shorter travel dist
        if (pylons <= 0) {
            nearTo = PylonPosition.nearToPositionForFirstPylon();
//                AAdvancedPainter.paintPosition(nearTo, "PylonPosition");
        }

        // First pylon should be orientated towards the nearest choke
        else if (pylons <= 1) {
            nearTo = PylonPosition.nearToPositionForSecondPylon(nearTo.position());
        }

        return nearTo;
    }
}
