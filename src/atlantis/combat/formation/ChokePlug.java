package atlantis.combat.formation;

import atlantis.game.A;
import atlantis.map.choke.AChoke;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.select.Selection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Choke plug (owner, PurpleWave {@code SquadDefendBase} guard port):
 * melee plugs the choke mouth on the home side, ranged shoots from
 * rows behind. PurpleWave sorts slots by weapon range ascending so
 * the shortest-ranged units take the innermost arc rows, the arc
 * faces the threat, and holders hold instead of chasing. The
 * geometry ports verbatim; the hold/fire behavior stays ours
 * (sim-pinned kite + ZealotHoldFire discipline, never touched here).
 *
 * <p>Pure geometry, no orders - the caller moves. One frame cache like
 * {@link FormationSlots}, same contract: null when the formation does
 * not apply, walkable slots only, deterministic assignment.
 */
public final class ChokePlug {

    /**
     * How far inside the choke mouth (home side) the plug sits. Nearer
     * than the 8-tile standoff rally on purpose: the plug is the wall
     * itself, and the second row still shoots over it from dragoon
     * range. Tuned against the sim funnel, not from memory.
     */
    public static final double PLUG_DEPTH_TILES = 3.0;

    /**
     * Row spacing behind the plug row, away from the foe.
     */
    public static final double ROW_SPACING_TILES = 1.5;

    /**
     * Slot spacing across a row.
     */
    public static final double SLOT_SPACING_TILES = 1.5;

    /**
     * Rows never grow past this; overflow bunches on the last row.
     */
    private static final int MAX_ROWS = 6;

    private static int cachedFrame = -1;
    private static Map<AUnit, APosition> cachedSlots = new HashMap<>();

    private ChokePlug() {
    }

    public static void clearForTests() {
        cachedFrame = -1;
        cachedSlots = new HashMap<>();
    }

    /**
     * The plug point: this far from the choke center toward home.
     * Walkable-fixed like the standoff seam; null without a choke.
     */
    public static APosition plugPointFor(AChoke choke, HasPosition homeSide) {
        if (choke == null || choke.center() == null || homeSide == null) return null;

        try {
            APosition plug = choke.center().translateTilesTowards(PLUG_DEPTH_TILES, homeSide.position());
            if (plug == null) return null;

            if (!plug.isWalkable()) plug = plug.makeWalkable(6, 3, null);

            return plug;
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    /**
     * This unit's plug slot, or null when the plug does not apply (too
     * few units, no points) or no walkable slot fits. Melee first:
     * classes sort by weapon range ascending and take rows from the
     * mouth outward, so zealots hold row zero and dragoons the rows
     * behind at shooting distance - whoever stands where does not
     * matter, the row does.
     */
    public static APosition slotFor(
        AUnit unit, Selection cluster, APosition plugPoint, HasPosition foePoint, double halfWidthTiles
    ) {
        if (unit == null || !unit.isAlive() || cluster == null || plugPoint == null || foePoint == null) {
            return null;
        }
        if (!FormationSlots.applies(cluster.count())) return null;

        if (cachedFrame != A.now() || !cachedSlots.containsKey(unit)) {
            compute(cluster, plugPoint, foePoint, halfWidthTiles);
        }
        return cachedSlots.get(unit);
    }

    // =========================================================

    private static void compute(
        Selection cluster, APosition plugPoint, HasPosition foePoint, double halfWidthTiles
    ) {
        cachedFrame = A.now();
        cachedSlots = new HashMap<>();

        List<AUnit> units = new ArrayList<>(cluster.list());
        units.removeIf(u -> u == null || !u.isAlive() || u.position() == null || !u.isGroundUnit());
        if (units.size() < FormationSlots.MIN_CLUSTER_SIZE) return;

        double foeX;
        double foeY;
        try {
            foeX = foePoint.position().x();
            foeY = foePoint.position().y();
        } catch (Exception ignored) {
            return;
        }
        double plugX = plugPoint.x();
        double plugY = plugPoint.y();
        double dx = plugX - foeX;
        double dy = plugY - foeY;
        double len = Math.hypot(dx, dy);
        if (len < 1) return;

        dx /= len;
        dy /= len;

        units.sort((a, b) -> {
            int range = Integer.compare(a.groundWeaponRange(), b.groundWeaponRange());
            if (range != 0) return range;

            return Integer.compare(a.id(), b.id());
        });

        List<APosition> slots = new ArrayList<>();
        List<Integer> slotRows = new ArrayList<>();
        for (int row = 0; row < MAX_ROWS && slots.size() < units.size(); row++) {
            double halfWidth = Math.max(1.5, halfWidthTiles + row * 1.0) * 32;
            int inRow = Math.max(1, (int) (2 * halfWidth / (SLOT_SPACING_TILES * 32)));
            for (int i = 0; i < inRow && slots.size() < units.size(); i++) {
                double across = (i - (inRow - 1) / 2.0) * SLOT_SPACING_TILES * 32;
                double back = row * ROW_SPACING_TILES * 32;
                int sx = (int) (plugX + dx * back - dy * across);
                int sy = (int) (plugY + dy * back + dx * across);
                APosition slot;
                try {
                    slot = APosition.createFromPixels(sx, sy);
                } catch (Exception ignored) {
                    continue;
                }
                if (slot != null && slot.isWalkable()) {
                    slots.add(slot);
                    slotRows.add(row);
                }
            }
        }
        if (slots.isEmpty()) return;

        // Class-ordered rows: distinct weapon ranges ascending, each
        // class takes the earliest row with free slots. A dragoon
        // standing on the mouth still takes a back row - position
        // never outranks the screen order.
        List<Integer> ranges = new ArrayList<>();
        for (AUnit u : units) {
            if (!ranges.contains(u.groundWeaponRange())) ranges.add(u.groundWeaponRange());
        }
        ranges.sort(Integer::compare);

        boolean[] taken = new boolean[slots.size()];
        for (AUnit u : units) {
            int classRow = ranges.indexOf(u.groundWeaponRange());
            int best = -1;
            double bestDist = Double.MAX_VALUE;
            // Pass 0 takes only the class row (melee holds the mouth,
            // ranged the rows behind, no matter who stands where); pass 1
            // spills over when the row is full or unusable.
            for (int pass = 0; pass < 2 && best < 0; pass++) {
                for (int i = 0; i < slots.size(); i++) {
                    if (taken[i]) continue;
                    if (pass == 0 && slotRows.get(i) != classRow) continue;
                    double dist;
                    try {
                        dist = u.distTo(slots.get(i));
                    } catch (Exception ignored) {
                        continue;
                    }
                    if (dist < bestDist) {
                        bestDist = dist;
                        best = i;
                    }
                }
            }
            if (best < 0) break;
            taken[best] = true;
            cachedSlots.put(u, slots.get(best));
        }
    }
}
