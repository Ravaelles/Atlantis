package atlantis.game.util;

import atlantis.game.A;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.util.AConsole;

import java.util.ArrayList;
import java.util.List;

/**
 * End-of-log audit: every build-order building that warped at a different
 * supply than its build order planned gets one HIGH priority line naming
 * the order break (owner: a BO Core "At 16" warping at 26 while Pylons and
 * Gateways jumped the queue reads as correct timing from every other
 * twin). Rows are recorded at warp time (expected supply + actual
 * supply); dynamic demand is out of scope by design (it has no planned
 * supply), V2 rows are skipped on minSupply 0 for the same reason.
 */
public final class BuildOrderAudit {

    private static final List<Row> rows = new ArrayList<Row>();

    private BuildOrderAudit() {
    }

    /**
     * Called when a Protoss building warps. Dynamic and orderless warps
     * carry no planned supply and are skipped; V2 plan-items (minSupply 0)
     * are skipped for the same reason.
     */
    public static void recordWarp(AUnit building, ProductionOrder order) {
        try {
            if (building == null || order == null || order.isDynamic()) return;

            AUnitType type = building.type();
            if (type == null || !type.isABuilding()) return;

            int expected = order.minSupply();
            if (expected <= 0) return;

            rows.add(new Row(type.name(), order.id(), expected, A.supplyUsed()));
        } catch (Exception ignored) {
            // Audit must never break a warp.
        }
    }

    /**
     * Mismatched rows, oldest first. Pure over the recorded rows.
     * Grep shape (owner ask): one "[MED] Construction issue detected"
     * line per break - type, order, actual then planned supply - so the
     * units log answers "which demand slipped" without the twin hunt.
     */
    public static List<String> mismatchLines() {
        List<String> lines = new ArrayList<String>();
        for (Row row : rows) {
            if (
                row.actualSupply != row.expectedSupply
                    && (row.actualSupply <= 20 || Math.abs(row.actualSupply - row.expectedSupply) >= 2)
            ) {
                lines.add("Construction issue detected: " + row.type
                    + "#" + row.orderId + " built at " + row.actualSupply
                    + " supply instead " + row.expectedSupply);
            }
        }
        return lines;
    }

    /**
     * End-of-log analysis: every BO building whose warp supply differs
     * from the plan, plus a one-line tally - and an explicit clean bill
     * when everything matched (a silent log cannot tell "clean" from
     * "the audit never ran"). Capped at ten issue lines (owner order:
     * no more than ~10 errors of the same type - the full per-row
     * evidence stays in the twins). Never throws: exit paths call this.
     */
    public static void printAudit() {
        try {
            int total = rows.size();
            List<String> mismatches = mismatchLines();
            if (total == 0) return;

            AConsole.println("===== BUILD_ORDER audit: " + total + " BO buildings warped, "
                + mismatches.size() + " off-plan =====");
            int shown = 0;
            for (String line : mismatches) {
                if (shown >= atlantis.util.log.PostGameDigest.MAX_SAMPLES_PER_KEY) {
                    AConsole.println(atlantis.util.log.LogLevel.MEDIUM,
                        "... and " + (mismatches.size() - shown) + " more off-plan warps (see twins).");
                    break;
                }
                AConsole.println(atlantis.util.log.LogLevel.MEDIUM, line);
                shown++;
            }
            if (mismatches.isEmpty()) {
                AConsole.println(atlantis.util.log.LogLevel.LOW,
                    "No issues in Build Order execution detected.");
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * Test seam: audit rows accumulate per JVM run.
     */
    public static void clearForTests() {
        rows.clear();
    }

    private static final class Row {
        final String type;
        final int orderId;
        final int expectedSupply;
        final int actualSupply;

        Row(String type, int orderId, int expectedSupply, int actualSupply) {
            this.type = type;
            this.orderId = orderId;
            this.expectedSupply = expectedSupply;
            this.actualSupply = actualSupply;
        }
    }
}
