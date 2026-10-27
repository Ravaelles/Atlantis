package atlantis.production.v2.goals;

import atlantis.production.v2.Producible;
import atlantis.production.v2.ProductionGoal;
import atlantis.production.v2.TargetPlacement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Turns a text build order into v2 goals (M5 of
 * _AI/redesign/01_PRODUCTION.md): the existing {@code .txt} files stay the
 * source of the opening, they simply stop being a queue.
 *
 * <p>
 * Stateless and idempotent: row K of item T is the N-th occurrence of T in the
 * file, and it is a goal only while {@link BuildOrderProgress#produced} is
 * below N. A finished, queued or pending row therefore never comes back, and an
 * unqualified row returns by itself on a later frame. The supply gate is
 * exact: a row with gate N is a goal only at supply N or later, no
 * lookahead. The early first Pylon measured in the sim (warp at supply 6
 * for gate 8) was never this gate firing early - it was the supply-block
 * prevention goal preempting the row - and a lookahead here would hide
 * that second author instead of exposing it.
 * </p>
 */
public final class BuildOrderGoals {

    private BuildOrderGoals() {
    }

    public static List<ProductionGoal> from(List<BuildOrderRow> rows, BuildOrderProgress progress) {
        List<ProductionGoal> goals = new ArrayList<>();
        if (rows == null || progress == null) return goals;

        int supplyUsed = progress.supplyUsed();
        Map<String, Integer> occurrences = new HashMap<>();
        Map<String, Integer> producedCache = new HashMap<>();
        java.util.Set<String> emittedTypes = new java.util.HashSet<>();

        for (int line = 0; line < rows.size(); line++) {
            BuildOrderRow row = rows.get(line);
            String id = row.item().id();

            Integer produced = producedCache.get(id);
            if (produced == null) {
                produced = Math.max(0, progress.produced(row.item()));
                producedCache.put(id, produced);
            }

            int before = occurrences.getOrDefault(id, 0);
            int after = before + row.multiplicity();
            occurrences.put(id, after);

            int missing = after - Math.max(before, produced);
            if (missing <= 0) continue;
            if (!supplyGateOpen(row.minSupply(), supplyUsed)) continue;

            // Build-order rows are sequential instructions, not independent
            // requests. Several future Pylon rows may have open supply gates in
            // the same frame; emitting every one gives the stateless scheduler
            // duplicate goals for one construction (measured in OpenBW, NEXT #48).
            // After the earliest outstanding row for this item is emitted, later
            // occurrences wait for the game snapshot to count it as produced.
            if (!emittedTypes.add(id)) continue;

            goals.add(new ProductionGoal(
                    row.item(),
                    priorityFor(row, line, supplyUsed),
                    missing,
                    0,
                    TargetPlacement.namedArea(row.positionModifier())));
        }

        goals.replaceAll(goal -> goal.withSource("build-order"));

        return goals;
    }

    /**
     * Exact supply gate: a row with gate N is a goal only at supply N or
     * later. No lookahead and no early-start bonus: the builder walk
     * overlaps nothing, it starts when the gate opens.
     */
    static boolean supplyGateOpen(int minSupply, int supplyUsed) {
        if (minSupply <= 0) return true;

        return supplyUsed >= minSupply;
    }

    /**
     * True while the rows still hold an unproduced row of this item: with
     * total demand D over all rows of T, some row is outstanding while
     * fewer than D exist. The supply goal reads this so the opening
     * provider has exactly one author (the row), never two.
     */
    public static boolean hasUnproducedRowOf(
            List<BuildOrderRow> rows, BuildOrderProgress progress, Producible item) {
        if (rows == null || progress == null || item == null) return false;

        int demanded = 0;
        for (BuildOrderRow row : rows) {
            if (row == null || !row.item().id().equals(item.id())) continue;
            demanded += row.multiplicity();
        }
        if (demanded <= 0) return false;

        return Math.max(0, progress.produced(item)) < demanded;
    }

    /**
     * Priority for one build-order row.
     *
     * <p>
     * <b>The opening supply provider is asked for before the workers are</b> (see
     * {@link ProductionGoal#PRIORITY_OPENING_SUPPLY}). The default opening is "Pylon at
     * supply 8", and there the Pylon is not a depot among others: nothing can be produced
     * without it, so it must not queue behind a Probe. This was a measured livelock - at
     * {@code PRIORITY_DEPOTS} the Pylon's start moved +1 per frame and it was never
     * dispatched, because the worker goal (scheduled first every pass) kept the bank below
     * its cost (`NEXT.md` #52).
     * </p>
     *
     * <p>
     * <b>Only the opening</b>, and that is the point of the threshold: a Pylon at supply
     * 40 is a depot like any other and keeps {@link ProductionGoal#PRIORITY_DEPOTS}. The
     * rule is about the building that unlocks production, not about the building type.
     * </p>
     *
     * <p>
     * The rest is still "earlier lines matter more", which is why the line number
     * survives as a fallback - but the opening case above is decided by what the row
     * <b>is</b>, not by where it sits in the file. Line-number-only priority meant a supply
     * Pylon on line 5 would silently outrank one on line 2 by band, which is not a rule
     * anybody chose.
     * </p>
     */
    private static int priorityFor(BuildOrderRow row, int line, int supplyUsed) {
        if (isOpeningSupplyProvider(row, supplyUsed)) {
            return ProductionGoal.PRIORITY_OPENING_SUPPLY;
        }

        if (line < 4) return ProductionGoal.PRIORITY_DEPOTS;
        if (line < 12) return ProductionGoal.PRIORITY_BASEDEFENSE;

        return ProductionGoal.PRIORITY_NORMAL;
    }

    /**
     * The supply gate up to which a supply provider counts as the OPENING one - the "Pylon
     * at 8" that every Protoss build order in this repository starts with.
     *
     * <p>
     * <b>Chosen from the build orders, not guessed.</b> Checked against
     * {@code bwapi-data/AI/build_orders/Protoss/*.txt}: the first Pylon is at supply <b>8</b>
     * in every file, and the second one is at <b>15</b> or <b>16</b>. So the opening is a
     * narrow band, and 12 sits between it and the second provider - it covers the real
     * opening with headroom for a build order that asks a few supply earlier or later,
     * and excludes the first top-up. A threshold of 16 was tried first and the test caught
     * it: it silently promoted the supply-16 Pylon too, which is exactly the depot the
     * rule must not touch.
     * </p>
     */
    static final int OPENING_SUPPLY_THRESHOLD = 12;

    /**
     * Is this row the supply provider that unlocks production, rather than a later top-up?
     *
     * <p>
     * Answered from the row's own supply gate, not from its position in the file, so a build
     * order may list its Pylon anywhere and still be understood. A row with no gate ({@code
     * minSupply <= 0}) does not count - it is not being asked for at a specific supply, so
     * there is no opening to protect.
     * </p>
     */
    private static boolean isOpeningSupplyProvider(BuildOrderRow row, int supplyUsed) {
        if (row.minSupply() <= 0) return false;
        if (row.minSupply() > OPENING_SUPPLY_THRESHOLD) return false;

        return suppliesPopulation(row.item());
    }

    /**
     * Does this item provide supply? Asked of the producible's own contract rather than by
     * comparing against {@code AUnitType.Protoss_Pylon} here: the goal layer serves all
     * three races, and a rule written as "is a Pylon" would be a Protoss assumption in
     * shared code (the same reason {@code PlacementPlanner} takes a strategy).
     */
    private static boolean suppliesPopulation(Producible item) {
        return item.supplyProvided() > 0;
    }
}
