package atlantis.combat.formation;

import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.map.region.ARegion;
import atlantis.units.AUnit;
import atlantis.units.select.Selection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Moon crescent slots (formation Phase A, no map lib): one shared shape
 * per cluster, units slot into it instead of each picking its own
 * compass point. Pure geometry around a shared pivot (usually the
 * leader's nearest enemy): the arc spans the summed unit widths at the
 * leader's distance to the pivot, the leader takes the nearest slot
 * first and the rest take their nearest free slot. Invalid (unwalkable)
 * slots fall back to the leader, never to standing still.
 *
 * <p>Ported from {@code src-old/.../formations/moon} minus the map-lib
 * parts: no region validation of the path (only the destination),
 * no overtime shrink (no fight clock yet - Phase C), no per-unit
 * radius tuning. Those arrive with the sim gate, not before.</p>
 *
 * <p>Slots apply to approach, never to contact: units with a target in
 * range fight per-unit (the frozen kite rules), units without one walk
 * here. Fewer than three combat units means no formation at all - the
 * 1v1 duel stays byte-identical.</p>
 */
public final class FormationSlots {

    /** Minimum cluster size: pairs and duels never form up. */
    public static final int MIN_CLUSTER_SIZE = 3;

    /** Too close for a shape: contact belongs to micro, not geometry. */
    private static final double MIN_RADIUS_TILES = 3.3;

    /** Arc never opens wider than this (Moon cap). */
    private static final double MAX_RADIUS_TILES = 11.0;

    private static int cachedFrame = -1;
    private static Map<AUnit, APosition> cachedSlots = new HashMap<>();

    private FormationSlots() {
    }

    public static boolean applies(int combatUnitCount) {
        return combatUnitCount >= MIN_CLUSTER_SIZE;
    }

    /**
     * This unit's crescent slot, or null when the formation does not
     * apply (too few units, no pivot, pivot too close) or no walkable
     * slot fits (the caller falls back to the leader/anchor).
     */
    public static APosition slotFor(AUnit unit, Selection cluster, HasPosition pivot) {
        if (unit == null || !unit.isAlive() || cluster == null || pivot == null) return null;
        if (!applies(cluster.count())) return null;

        if (cachedFrame != atlantis.game.A.now() || !cachedSlots.containsKey(unit)) {
            compute(cluster, pivot);
        }
        return cachedSlots.get(unit);
    }

    public static void clearForTests() {
        cachedFrame = -1;
        cachedSlots = new HashMap<>();
    }

    // =========================================================

    private static void compute(Selection cluster, HasPosition pivot) {
        cachedFrame = atlantis.game.A.now();
        cachedSlots = new HashMap<>();

        List<AUnit> units = new ArrayList<>(cluster.list());
        // Corpses hold no slots: a dead or positionless roster entry must
        // not crash the formation for the living (found via the sim,
        // where the roster outlives the unit).
        // Corpses hold no slots: a dead or positionless roster entry must
        // not crash the formation for the living - found via the phalanx sim,
        // where the roster outlives the unit.
        units.removeIf(u -> u == null || !u.isAlive() || u.position() == null);
        if (units.size() < MIN_CLUSTER_SIZE) return;

        AUnit leader = units.get(0);
        double radius = leader.distTo(pivot);
        if (radius < MIN_RADIUS_TILES) return;

        radius = Math.min(radius, MAX_RADIUS_TILES);

        APosition center = averageOf(units);
        if (center == null) return;

        center = nudgeTowardRegionCenter(center);

        double separation = Math.max(1.0, 20.0 / (10 + units.size()));
        double totalWidth = 0;
        for (AUnit u : units) totalWidth += u.size() + separation;
        double spanDegrees = Math.toDegrees(totalWidth / radius);

        double angleToPivot = Math.atan2(
            pivot.ty() - center.ty(), pivot.tx() - center.tx());
        double start = angleToPivot - Math.toRadians(spanDegrees / 2);
        double step = units.size() > 1
            ? Math.toRadians(spanDegrees) / (units.size() - 1) : 0;

        List<APosition> slots = new ArrayList<>();
        for (int i = 0; i < units.size(); i++) {
            double angle = start + i * step;
            APosition slot = APosition.create(
                (int) (pivot.tx() - Math.cos(angle) * radius),
                (int) (pivot.ty() - Math.sin(angle) * radius));
            if (slot != null && valid(slot, units.get(i))) slots.add(slot);
        }
        if (slots.isEmpty()) return;

        List<AUnit> ordered = new ArrayList<>(units);
        ordered.sort((a, b) -> Double.compare(a.distTo(pivot), b.distTo(pivot)));
        boolean[] taken = new boolean[slots.size()];
        for (AUnit u : ordered) {
            int best = -1;
            double bestDist = Double.MAX_VALUE;
            for (int i = 0; i < slots.size(); i++) {
                if (taken[i]) continue;
                double dist = u.distTo(slots.get(i));
                if (dist < bestDist) {
                    bestDist = dist;
                    best = i;
                }
            }
            if (best < 0) break;
            taken[best] = true;
            cachedSlots.put(u, slots.get(best));
        }
    }

    private static boolean valid(APosition slot, AUnit unit) {
        if (unit.isGroundUnit() && !slot.isWalkable()) return false;

        return true;
    }

    private static APosition nudgeTowardRegionCenter(APosition center) {
        try {
            ARegion region = center.region();
            if (region == null || region.center() == null) return center;

            return center.translatePercentTowards(33, region.center());
        }
        catch (RuntimeException ignored) {
            return center;
        }
    }

    /**
     * Mean position of the filtered roster: corpses must not drag the
     * crescent's center (or crash averaging it) - the living own the
     * formation. All callers filter first, so this never sees nulls.
     */
    private static APosition averageOf(List<AUnit> units) {
        try {
            if (units == null || units.isEmpty()) return null;

            long sx = 0;
            long sy = 0;
            for (AUnit u : units) {
                sx += u.position().x();
                sy += u.position().y();
            }
            return APosition.createFromPixels((int) (sx / units.size()), (int) (sy / units.size()));
        } catch (Exception ignored) {
            return null;
        }
    }
}
