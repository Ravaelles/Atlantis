package atlantis.production.constructions.position.protoss;

import atlantis.game.A;
import atlantis.map.AMap;
import atlantis.map.position.APosition;
import atlantis.production.constructions.position.AbstractPositionFinder;
import atlantis.units.AUnitType;
import atlantis.util.We;

public class ProtossTooCloseToMapBoundaries {
    public static boolean isTooClose(AUnitType building, APosition position) {
        if (We.protoss() && !building.isGateway() && !building.isPylon()) return false;

        int marginPx = 3 * 30;

        if (A.supplyUsed() <= 30 && building.isPylon()) marginPx = 7 * 30;

        int px = position.x();
        int py = position.y();

        if (
            px > marginPx && px < (AMap.getMapWidthInTiles() - marginPx)
                && py > marginPx && py < (AMap.getMapHeightInTiles() - marginPx)
        ) return false;

        if ((px - building.dimensionLeftPixels()) <= marginPx) {
            return failed("Too close to left map boundary");
        }
        if ((py - building.dimensionUpPixels()) <= marginPx) {
            return failed("Too close to top map boundary");
        }

        if ((px + building.dimensionRightPixels()) >= (AMap.getMapWidthInTiles() * 30 - marginPx)) {
            return failed("Too close to right map boundary");
        }
        if ((py + building.dimensionDownPixels()) >= (AMap.getMapHeightInTiles() * 30 - marginPx)) {
            return failed("Too close to bottom map boundary");
        }

        return false;
    }

    private static boolean failed(String reason) {
        AbstractPositionFinder._STATUS = reason;
        return true;
    }
}