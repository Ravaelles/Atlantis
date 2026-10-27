package atlantis.production.constructions.position.protoss;

import atlantis.game.A;
import atlantis.map.position.APosition;
import atlantis.production.constructions.position.AbstractPositionFinder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;

/**
 * A Pylon warps power, not just a building: a site whose surroundings
 * cannot host anything wastes the power field (measured on UMS 7th_rav:
 * a Pylon warped tight at the region edge, lighting no usable ground).
 * When supply is comfortable (2+ free), the four diagonal tiles around
 * the site (4 tiles out each way) must all be walkable, so the Pylon
 * sits inside usable ground instead of on its edge. Poor supply keeps
 * the old behaviour: any powered tile warms the pump. Pure geometry
 * on live map data - no builders, no reservations.
 */
public class PylonNeedsRoomAround {

    /**
     * How far out the four diagonal probes reach (tiles).
     */
    public static final int ROOM_TILES = 4;

    public static boolean isTooTight(AUnit builder, AUnitType building, APosition position) {
        if (!building.isPylon()) return false;
        if (A.supplyFree() <= 1) return false;

        int[][] corners = {{ROOM_TILES, ROOM_TILES}, {-ROOM_TILES, -ROOM_TILES}, {ROOM_TILES, -ROOM_TILES}, {-ROOM_TILES, ROOM_TILES}};
        for (int[] corner : corners) {
            APosition probe = position.translateByTiles(corner[0], corner[1]);
            if (probe == null || !probe.isWalkable()) {
                AbstractPositionFinder._STATUS = "Pylon needs room around (blocked at "
                    + corner[0] + "/" + corner[1] + ")";

                return true;
            }
        }
        return false;
    }
}
