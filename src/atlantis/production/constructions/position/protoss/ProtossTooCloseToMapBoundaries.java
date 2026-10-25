package atlantis.production.constructions.position.protoss;

import atlantis.game.A;
import atlantis.map.AMap;
import atlantis.map.position.APosition;
import atlantis.production.constructions.position.AbstractPositionFinder;
import atlantis.units.AUnitType;
import atlantis.util.We;

public class ProtossTooCloseToMapBoundaries {
    public static boolean isTooClose(AUnitType building, APosition position) {
//        if (true) return false;

        if (We.protoss() && !building.isGateway() && !building.isPylon()) return false;
        if (A.supplyFree() <= 0 && A.supplyTotal() >= 40) return false;

        // A build tile is 32 pixels, not 30 (owner report, 2026-10-10). Every other
        // place in the tree uses 32 - APosition.create(tx, ty) multiplies by 32,
        // AUnitType.dimensionRightTiles() divides by 32, translateByTiles multiplies
        // by 32 - so 30 here made the map smaller than it is and pulled the right and
        // bottom margins inwards. Measured on a 128-tile map the two disagree by
        // 8 tiles: the old arithmetic refuses everything at tile >= 111 while the
        // truth is 119, so the bot threw away eight columns and rows of perfectly
        // good build sites and reported "Too close to right map boundary" for
        // positions nowhere near an edge.
        int marginPx = (int) (2.4 * PX_PER_TILE);
        if (A.supplyUsed() <= 30 && building.isPylon()) marginPx = 7 * PX_PER_TILE;

        int px = position.x();
        int py = position.y();

        int mapWidthPixels = AMap.getMapWidthInTiles() * PX_PER_TILE;
        int mapHeightPixels = AMap.getMapHeightInTiles() * PX_PER_TILE;

        if (
            px >= marginPx && px <= (mapWidthPixels - marginPx)
                && py >= marginPx && py <= (mapHeightPixels - marginPx)
        ) return false;

        if ((px - building.dimensionLeftPixels()) <= marginPx) {
            return failed("Too close to left map boundary");
        }
        if ((py - building.dimensionUpPixels()) <= marginPx) {
            return failed("Too close to top map boundary");
        }

        if ((px + building.dimensionRightPixels()) >= (mapWidthPixels - marginPx)) {
            return failed("Too close to right map boundary");
        }
        if ((py + building.dimensionDownPixels()) >= (mapHeightPixels - marginPx)) {
            return failed("Too close to bottom map boundary");
        }

        return false;
    }

    /**
     * Pixels per build tile in this engine. Named because the whole point of the
     * fix this constant carries is that it is 32 and not a hand-typed 30.
     */
    public static final int PX_PER_TILE = 32;

    private static boolean failed(String reason) {
        AbstractPositionFinder._STATUS = reason;

        return true;
    }
}
