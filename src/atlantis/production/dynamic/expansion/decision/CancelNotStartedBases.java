package atlantis.production.dynamic.expansion.decision;

import atlantis.config.AtlantisRaceConfig;
import atlantis.game.A;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnit;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.util.AConsole;

import java.util.ArrayList;
import java.util.List;

public class CancelNotStartedBases {

    /**
     * "We have enough bases, so drop the ones we never started" - but only when that
     * is actually true.
     *
     * <p>The owner's rule (B-23), after a game where this pass cancelled the natural
     * the bot was building: look at everything unfinished of the base type - planned
     * constructions, rising buildings, units caught mid-build - and cancel only when
     * there are <b>two or more</b>. With one unfinished base there is no "the rest",
     * and cancelling the only one is the churn that was reported (a Nexus NATURAL
     * queued at 4:13, cancelled at ~5:00 with this exact reason, queued again at
     * 5:15).</p>
     *
     * <p>Which one goes: only bases nobody has started on are prunable - a base with a
     * builder on it has cost minerals, a worker and a walk, and no number of spare
     * bases is worth that. Of the prunable ones, if something is already going up we
     * drop them all (the bot is already expanding); if nothing has started we keep the
     * oldest, because cancelling every pending expansion only makes the bot queue them
     * again a minute later - which is the churn this whole pass used to cause.</p>
     */
    public static void cancelNotStartedBases(AUnit newBase, String reason) {
        if (!worthPruningBases()) return;

        for (ProductionOrder order : redundantNotStartedBases(newBase)) {
            cancel(order, reason);
        }
    }

    /**
     * The aggressive policy: also drop anything under half built, and do not ask
     * whether there is more than one.
     *
     * <p>Callers are the ones giving up on an expansion to free minerals or to
     * survive - "Cancel base - much weaker", "Critical base cancel",
     * "HiddenEnemiesPressure", the expansion veto - where the base itself is the
     * thing they are unhappy about, not the number of them.</p>
     */
    public static void cancelNotStartedOrEarlyBases(AUnit newBase, String reason) {
        if (!worthPruningBases()) return;

        Queue.get().statusNotReady().ofType(AtlantisRaceConfig.BASE).forEach((order) -> {
            Construction construction = order.construction();
            if (shouldCancelBase(construction, newBase, true)) {
                cancel(order, reason);
            }
        });
    }

    // =========================================================

    private static boolean worthPruningBases() {
        return A.seconds() < 700 && Count.bases() < 3;
    }

    /**
     * Everything of the base type that is not finished yet: rising buildings and units
     * caught mid-construction, constructions nobody has started but has already
     * ordered, and constructions that are still only a request.
     */
    private static int unfinishedBases(int notStartedInQueue) {
        return Select.ourUnfinished().ofType(AtlantisRaceConfig.BASE).count()
            + ConstructionRequests.countNotStartedOfType(AtlantisRaceConfig.BASE)
            + notStartedInQueue;
    }

    /**
     * The orders to drop: everything pending that nobody has started on and that is not
     * the base whose completion triggered this pass.
     *
     * <p>Empty means there is nothing redundant - a single pending base - and then
     * nothing is cancelled.</p>
     */
    private static List<ProductionOrder> redundantNotStartedBases(AUnit newBase) {
        List<ProductionOrder> notStarted = new ArrayList<>();

        for (ProductionOrder order : Queue.get().statusNotReady().ofType(AtlantisRaceConfig.BASE).list()) {
            Construction construction = order.construction();
            if (shouldCancelBase(construction, newBase, false)) {
                notStarted.add(order);
            }
        }

        // The owner's "only if >= 2": one unfinished base is not a redundancy, and
        // cancelling it is the churn this pass used to cause.
        if (unfinishedBases(notStarted.size()) < 2) {
            return new ArrayList<>();
        }

        // Nothing is under construction: keep the oldest pending base (queue order is
        // oldest first) and drop the rest, so the bot keeps expanding towards one place.
        if (!somethingIsAlreadyBeingBuilt(newBase)) {
            return new ArrayList<>(notStarted.subList(1, notStarted.size()));
        }

        return notStarted;
    }

    private static boolean somethingIsAlreadyBeingBuilt(AUnit newBase) {
        Construction triggerConstruction = newBase == null ? null : newBase.construction();

        for (ProductionOrder order : Queue.get().statusNotReady().ofType(AtlantisRaceConfig.BASE).list()) {
            Construction construction = order.construction();
            if (construction != null
                && construction.hasStarted()
                && !construction.equals(triggerConstruction)) {
                return true;
            }
        }

        return false;
    }

    private static boolean shouldCancelBase(Construction construction, AUnit unit, boolean includeEarlyOnes) {
        return construction != null
            && (!construction.hasStarted() || (includeEarlyOnes && construction.progressPercent() <= 49))
            && (unit == null || !construction.equals(unit.construction()));
    }

    private static void cancel(ProductionOrder order, String reason) {
        AConsole.errPrintln(
            A.now() + " Cancelling pending base "
            + order + ", Reason: " + reason
        );
        order.cancel(reason);
    }
}