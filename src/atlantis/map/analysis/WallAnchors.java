package atlantis.map.analysis;

import atlantis.map.position.APosition;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Wall anchors for one choke: the tile row pieces stand on, in segment
 * order, plus the door suggestion.
 *
 * <p>
 * Pure: choke ends in, ordered anchor tiles + middle door out. No pieces,
 * no order, no game - which piece plugs where (Pylon + Gateway + cannons
 * at the natural, per the 04_MAPLIB charter) is the wall-in goal's job in
 * production, where bank, supply and race live. The goal reads these
 * anchors as exact-tile constraints; the door tile stays recorded so
 * holders keep it clear.
 * </p>
 */
public final class WallAnchors {

    /** Anchors in segment order with the door suggestion. */
    public static final class AnchorRow {
        private final List<APosition> slots;
        private final APosition door;

        AnchorRow(List<APosition> slots, APosition door) {
            this.slots = slots;
            this.door = door;
        }

        public List<APosition> slots() {
            return slots;
        }

        public APosition door() {
            return door;
        }
    }

    private WallAnchors() {
    }

    /**
     * Tiles within half a tile of the choke segment, deduplicated in walk
     * order; the door is the middle slot. A degenerate (point) choke yields
     * its single tile as both row and door.
     */
    public static AnchorRow slots(APosition endA, APosition endB) {
        List<APosition> slots = new ArrayList<>();
        if (endA == null || endB == null) return new AnchorRow(slots, null);

        double ax = endA.getX();
        double ay = endA.getY();
        double bx = endB.getX();
        double by = endB.getY();
        double len = Math.hypot(bx - ax, by - ay);

        Set<String> seen = new LinkedHashSet<>();
        if (len == 0) {
            APosition tile = APosition.create(endA.tx(), endA.ty());
            slots.add(tile);

            return new AnchorRow(slots, tile);
        }

        int steps = Math.max(1, (int) (len / 16));
        for (int i = 0; i <= steps; i++) {
            double t = (double) i / steps;
            int tx = (int) ((ax + t * (bx - ax)) / 32);
            int ty = (int) ((ay + t * (by - ay)) / 32);
            if (seen.add(tx + ":" + ty)) {
                slots.add(APosition.create(tx, ty));
            }
        }
        return new AnchorRow(slots, slots.get(slots.size() / 2));
    }
}
