package atlantis.production.dynamic.expansion.decision;

import atlantis.config.AtlantisRaceConfig;
import atlantis.game.A;
import atlantis.production.constructions.Construction;
import atlantis.production.orders.production.queue.Queue;
import atlantis.units.AUnit;
import atlantis.units.select.Count;
import atlantis.util.AConsole;

public class CancelNotStartedBases {

    /**
     * The doctrine its name describes: a base we never started is not worth the
     * queue slot, so drop it.
     *
     * <p>Used when the reason is "we have enough bases" - including
     * {@code OnOurUnitCreated}'s "New base created, remove not started ones". It
     * deliberately does <b>not</b> touch a construction with a builder on it: a base
     * half way up has cost minerals, a builder's time and a walk across the map, and
     * cancelling it is the churn the owner reported from a game (B-23: a Nexus NATURAL
     * queued at 4:13, cancelled at ~5:00 by exactly this pass, queued again at
     * 5:15).</p>
     */
    public static void cancelNotStartedBases(AUnit newBase, String reason) {
        cancelBases(newBase, reason, false);
    }

    /**
     * The aggressive policy: also drop anything under half built.
     *
     * <p>Callers are the ones giving up on an expansion to free minerals or to
     * survive - "Cancel base - much weaker", "Critical base cancel",
     * "HiddenEnemiesPressure", the expansion veto - where half a Nexus is worth
     * something back. {@link #cancelNotStartedBases} is the same pass without the
     * second half of the condition.</p>
     */
    public static void cancelNotStartedOrEarlyBases(AUnit newBase, String reason) {
        cancelBases(newBase, reason, true);
    }

    private static void cancelBases(AUnit newBase, String reason, boolean includeEarlyOnes) {
        if (A.seconds() >= 700 || Count.bases() >= 3) return;

        Queue.get().statusNotReady().ofType(AtlantisRaceConfig.BASE).forEach((order) -> {
            Construction construction = order.construction();
            if (shouldCancelBase(construction, newBase, includeEarlyOnes)) {
                AConsole.errPrintln(
                    A.now() + " Cancelling pending base "
                    + order + ", Reason: " + reason
                );
                order.cancel(reason);
            }
        });
    }

    private static boolean shouldCancelBase(Construction construction, AUnit unit, boolean includeEarlyOnes) {
        return construction != null
            && (!construction.hasStarted() || (includeEarlyOnes && construction.progressPercent() <= 49))
            && (unit == null || !construction.equals(unit.construction()));
    }
}