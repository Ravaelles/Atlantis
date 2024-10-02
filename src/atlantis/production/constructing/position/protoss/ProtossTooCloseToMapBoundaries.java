package atlantis.production.constructing.position.protoss;

import atlantis.game.A;
import atlantis.map.AMap;
import atlantis.map.position.APosition;
import atlantis.map.region.ARegion;
import atlantis.map.region.ARegionBoundary;
import atlantis.production.constructing.position.AbstractPositionFinder;
import atlantis.units.AUnitType;

public class ProtossTooCloseToMapBoundaries {
    public static boolean isTooClose(AUnitType building, APosition position) {
        int threshold = 1;

        int tx = position.tx();
        int ty = position.ty();

        if (
            tx >= 4 && tx <= AMap.getMapWidthInTiles() - 4
                && ty >= 4 && ty <= AMap.getMapHeightInTiles() - 4
        ) return false;

        if ((tx - building.getTileWidth() / 2) <= threshold) {
            return failed("Too close to left map boundary");
        }
        if ((ty - building.getTileHeight() / 2) <= threshold) {
            return failed("Too close to top map boundary");
        }

        if ((tx + building.getTileWidth() / 2) >= AMap.getMapWidthInTiles() - threshold) {
            return failed("Too close to right map boundary");
        }
        if ((ty + building.getTileHeight() / 2) >= AMap.getMapHeightInTiles() - threshold) {
            return failed("Too close to bottom map boundary");
        }

        return false;
    }

    private static boolean failed(String reason) {
        AbstractPositionFinder._CONDITION_THAT_FAILED = reason;
        return true;
    }
}