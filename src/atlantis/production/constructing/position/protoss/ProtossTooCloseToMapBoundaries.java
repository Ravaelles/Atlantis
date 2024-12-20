package atlantis.production.constructing.position.protoss;

import atlantis.game.A;
import atlantis.map.AMap;
import atlantis.map.position.APosition;
import atlantis.production.constructing.position.AbstractPositionFinder;
import atlantis.units.AUnitType;
import atlantis.util.We;

public class ProtossTooCloseToMapBoundaries {
    public static boolean isTooClose(AUnitType building, APosition position) {
        if (We.protoss() && !building.isGateway() && !building.isPylon()) return false;

        double margin = 1.3;

        if (A.supplyUsed() <= 30 && building.isPylon()) margin = 8;

        int tx = position.tx();
        int ty = position.ty();

        if (
            tx >= margin && tx <= AMap.getMapWidthInTiles() - margin
                && ty >= margin && ty <= AMap.getMapHeightInTiles() - margin
        ) return false;

        if ((tx - building.getTileWidth() / 2) <= margin) {
            return failed("Too close to left map boundary");
        }
        if ((ty - building.getTileHeight() / 2) <= margin) {
            return failed("Too close to top map boundary");
        }

        if ((tx + building.getTileWidth() / 2) >= AMap.getMapWidthInTiles() - margin) {
            return failed("Too close to right map boundary");
        }
        if ((ty + building.getTileHeight() / 2) >= AMap.getMapHeightInTiles() - margin) {
            return failed("Too close to bottom map boundary");
        }

        return false;
    }

    private static boolean failed(String reason) {
        AbstractPositionFinder._STATUS = reason;
        return true;
    }
}