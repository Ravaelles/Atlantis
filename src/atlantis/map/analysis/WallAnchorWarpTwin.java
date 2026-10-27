package atlantis.map.analysis;

import atlantis.map.position.APosition;
import atlantis.util.log.EventLog;
import atlantis.util.log.LogKey;

import java.util.List;

/**
 * Anchor-vs-warp geometry witness (#97 M4): when a wall-piece type warps,
 * how far did it land from the natural choke's anchor row, and from the
 * exact tile the wall planner asked for. A wall whose pieces land on
 * anchors is walled; a wall whose exact orders land elsewhere is a
 * siting gap, not a demand gap.
 *
 * <p>Pure over points: the recorder resolves the choke row and calls
 * in, tests call in with numbers. anchorDist -1 means the map offered
 * no anchor row (the 99-sentinel shape), not missing data.</p>
 */
public final class WallAnchorWarpTwin {

    private WallAnchorWarpTwin() {
    }

    /**
     * Nearest anchor slot in tiles, or -1 for no row / no tile.
     */
    public static double nearestSlotDist(List<APosition> slots, APosition tile) {
        if (slots == null || slots.isEmpty() || tile == null) return -1;

        double best = -1;
        for (APosition slot : slots) {
            if (slot == null) continue;
            double dist = slot.distTo(tile);
            if (best < 0 || dist < best) best = dist;
        }
        return best;
    }

    public static void emit(String type, APosition warpTile, APosition plannedTile,
                            boolean exact, List<APosition> slots, int frame, String time) {
        if (type == null || warpTile == null) return;
        // Decitiles, the KiteTwin convention: JSON params carry no doubles.
        int anchorDist = scaled(nearestSlotDist(slots, warpTile));
        int plannedDist = plannedTile == null ? -1 : scaled(plannedTile.distTo(warpTile));
        EventLog.key(LogKey.WALL_ANCHOR_WARP)
            .param("type", type)
            .param("tile", "[" + warpTile.tx() + "," + warpTile.ty() + "]")
            .param("anchorDist", anchorDist)
            .param("exact", exact)
            .param("plannedTile", plannedTile == null ? "-"
                : "[" + plannedTile.tx() + "," + plannedTile.ty() + "]")
            .param("plannedDist", plannedDist)
            .log(frame, time);
    }

    private static int scaled(double tiles) {
        return tiles < 0 ? -1 : (int) (tiles * 10);
    }
}
