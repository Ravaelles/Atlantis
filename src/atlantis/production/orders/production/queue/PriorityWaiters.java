package atlantis.production.orders.production.queue;

import atlantis.game.A;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.production.orders.production.queue.order.ProductionOrderPriority;

/**
 * Priority arbitration for spenders (owner's direction, D3): when an
 * unfinished building, upgrade, or tech order with strictly higher priority
 * waits, lower spending pauses unless the bank covers both.
 *
 * <p>Unfinished means in queue, whatever its status: the rule keys on work
 * existing, not on whether the engine already deducted its price. A warping
 * Pylon still counts while it stands unfinished.</p>
 *
 * <p>The failure this heals: a queued Nexus flapped READY⇄IN_PROGRESS while
 * the bank sawtoothed 24-558 against the 400 price with zero requests (game
 * 20261014_121406) - the workers-only save-up gate did not stop the army
 * pump, and nothing reserved 400 for the issue frame. Same shape as the
 * Singularity Charge starvation (Dragoon pump eating gas below 150).</p>
 *
 * <p>Convergence, not deadlock: while spenders pause, income is unspent, so
 * the bank grows monotonically toward the waiter - indefinite waiting is
 * allowed by design. Supply providers are TOP and never blocked by HIGH.</p>
 */
public class PriorityWaiters {

    /**
     * Total mineral price of unfinished, non-ignored building/upgrade/tech
     * orders strictly above the given level. Pure over the queue - no game
     * reads - so it is unit-testable without a world.
     */
    public static int mineralsAbove(ProductionOrderPriority level) {
        return totalAbove(level, true);
    }

    /**
     * Total gas price of unfinished, non-ignored building/upgrade/tech
     * orders strictly above the given level.
     */
    public static int gasAbove(ProductionOrderPriority level) {
        return totalAbove(level, false);
    }

    /**
     * True when spending (myMinerals, myGas) at myLevel must wait: a higher
     * waiter exists and the bank does not cover both. Rich bank passes - that
     * is the "unless we have plenty" half of the rule.
     */
    public static boolean blocksSpending(int myMinerals, int myGas, ProductionOrderPriority myLevel) {
        int waitMinerals = mineralsAbove(myLevel);
        int waitGas = gasAbove(myLevel);
        if (waitMinerals == 0 && waitGas == 0) return false;
        return !A.hasMinerals(myMinerals + waitMinerals) || !A.hasGas(myGas + waitGas);
    }

    /**
     * Same, but the given order never blocks itself: a HIGH waiter evaluates
     * its own readiness against strictly-higher waiters only. Identity
     * comparison - same instance, not same type.
     */
    public static boolean blocksSpending(ProductionOrder order, int myMinerals, int myGas) {
        int waitMinerals = totalAboveExcept(order, true);
        int waitGas = totalAboveExcept(order, false);
        if (waitMinerals == 0 && waitGas == 0) return false;
        return !A.hasMinerals(myMinerals + waitMinerals) || !A.hasGas(myGas + waitGas);
    }

    private static int totalAbove(ProductionOrderPriority level, boolean minerals) {
        if (Queue.get() == null) return 0;
        int total = 0;
        for (ProductionOrder order : Queue.get().notFinished().list()) {
            if (order.shouldIgnore()) continue;
            if (!isWaiter(order)) continue;
            if (level.isAtLeast(order.priority())) continue;
            total += minerals ? order.mineralPrice() : order.gasPrice();
        }
        return total;
    }

    private static int totalAboveExcept(ProductionOrder except, boolean minerals) {
        if (Queue.get() == null) return 0;
        int total = 0;
        for (ProductionOrder order : Queue.get().notFinished().list()) {
            if (order == except) continue;
            if (order.shouldIgnore()) continue;
            if (!isWaiter(order)) continue;
            if (except.priority().isAtLeast(order.priority())) continue;
            total += minerals ? order.mineralPrice() : order.gasPrice();
        }
        return total;
    }

    private static boolean isWaiter(ProductionOrder order) {
        return order.isBuilding() || order.upgrade() != null || order.tech() != null;
    }

    /**
     * One-line waiter identities for the ORDER_WAITING diagnostic: type,
     * priority and price each, so a waiting game names what it waits for.
     */
    public static String describeAbove(ProductionOrderPriority level, ProductionOrder except) {
        if (Queue.get() == null) return "-";
        StringBuilder out = new StringBuilder();
        for (ProductionOrder order : Queue.get().notFinished().list()) {
            if (order == except || order.shouldIgnore() || !isWaiter(order)) continue;
            if (level.isAtLeast(order.priority())) continue;
            if (out.length() > 0) out.append("+");
            out.append(order.whatToString()).append("(").append(order.priority())
                .append(",").append(order.mineralPrice()).append("/").append(order.gasPrice()).append(")");
        }
        return out.toString();
    }
}
