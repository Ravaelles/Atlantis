package atlantis.production.v2.goals;

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
 * unqualified row returns by itself on a later frame. The supply gate is the
 * legacy one ({@code IsReadyToProduceOrder}): supply used plus a small
 * lookahead must reach the row's supply.
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

            goals.add(new ProductionGoal(
                    row.item(),
                    priorityForLine(line),
                    missing,
                    0,
                    TargetPlacement.namedArea(row.positionModifier())));
        }

        return goals;
    }

    /**
     * The legacy gate ({@code IsReadyToProduceOrder}): a row may start slightly
     * before its supply, so the builder walk overlaps the last worker.
     */
    static boolean supplyGateOpen(int minSupply, int supplyUsed) {
        if (minSupply <= 0) return true;
        int lookahead = supplyUsed >= 19 ? 3 : 1;
        return supplyUsed + lookahead >= minSupply;
    }

    /**
     * Earlier lines are higher priority within coarse bands; the stable sort in
     * the scheduler keeps the file order inside a band.
     */
    private static int priorityForLine(int line) {
        if (line < 4) return ProductionGoal.PRIORITY_DEPOTS;
        if (line < 12) return ProductionGoal.PRIORITY_BASEDEFENSE;
        return ProductionGoal.PRIORITY_NORMAL;
    }
}
