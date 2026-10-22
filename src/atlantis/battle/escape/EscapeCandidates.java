package atlantis.battle.escape;

import atlantis.map.position.APosition;

import java.util.ArrayList;
import java.util.List;

/**
 * Where a fleeing unit could go: the 8 compass directions at 4/8/12 tiles,
 * unwalkable destinations filtered. The 8-tile want (NEXT #172: run at
 * least 8 tiles when possible) is the middle ring, not a promise - a
 * boxed unit still gets its 4-tile steps, and 12 tiles when the map is
 * open. Scoring (obstacles, corners, enemies) ranks them later; this
 * only lists what exists.
 */
public final class EscapeCandidates {

    private EscapeCandidates() {
    }

    /** Rings in tiles: 4 (boxed fallback), 8 (the want), 12 (open ground). */
    public static final int[] RADII_TILES = { 4, 8, 12 };

    public static List<APosition> generate(
        APosition from, EscapeRays.Walkability walk
    ) {
        List<APosition> out = new ArrayList<>();
        if (from == null || walk == null) return out;

        for (int[] dir : EscapeRays.DIRS) {
            for (int radius : RADII_TILES) {
                int tx = from.tx() + dir[0] * radius;
                int ty = from.ty() + dir[1] * radius;
                if (tx < 0 || ty < 0) continue;
                if (!walk.isWalkable(tx, ty)) continue;
                out.add(APosition.create(tx, ty));
            }
        }
        return out;
    }
}
