package atlantis.production.dynamic;

import atlantis.game.A;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.util.log.LogLevel;

/**
 * One loud line when the bank first crosses 800 minerals in a game
 * (owner ask): a hoarded bank with a stuck production is the shape
 * behind "zablokowaliśmy się i przegraliśmy", and the twins only show
 * it in hindsight. Fires exactly once per game - the flag resets on
 * game start (and in sim teardown, same-JVM games would otherwise
 * eat each other's line) - so it can never spam.
 *
 * <p>The row carries the demand side next to the bank, which is what
 * distinguishes "saving for a Nexus" from "nothing converts":
 * supply used/total/free, unfinished constructions and queued
 * not-started buildings.</p>
 */
public final class MineralHoardWatch {

    private static final int HOARD_MINERALS = 800;

    private static boolean announced = false;

    private MineralHoardWatch() {
    }

    public static void reset() {
        announced = false;
    }

    /**
     * True when this call emitted the row (the first bank-over-800 frame).
     */
    public static boolean check() {
        if (announced) return false;

        int minerals;
        try {
            minerals = A.minerals();
        } catch (Exception | Error ignored) {
            return false;
        }
        if (minerals <= HOARD_MINERALS) return false;

        announced = true;

        int unfinished = 0;
        try {
            unfinished = ConstructionRequests.notFinished().size();
        } catch (Exception | Error ignored) {
        }

        int queued = 0;
        try {
            if (atlantis.production.orders.production.queue.Queue.get() != null) {
                for (atlantis.production.orders.production.queue.order.ProductionOrder order
                    : atlantis.production.orders.production.queue.Queue.get().notFinished().list()) {
                    if (order != null && order.isBuilding()) queued++;
                }
            }
        } catch (Exception | Error ignored) {
        }

        atlantis.util.AConsole.errPrintln(LogLevel.HIGH,
            "BANK_HOARD first bank over " + HOARD_MINERALS + ": " + minerals
                + " minerals, supply " + safeSupplyUsed() + "/" + safeSupplyTotal()
                + " (free " + safeSupplyFree() + "), unfinished constructions "
                + unfinished + ", queued buildings " + queued);

        return true;
    }

    private static int safeSupplyUsed() {
        try {
            return A.supplyUsed();
        } catch (Exception | Error ignored) {
            return -1;
        }
    }

    private static int safeSupplyTotal() {
        try {
            return A.supplyTotal();
        } catch (Exception | Error ignored) {
            return -1;
        }
    }

    private static int safeSupplyFree() {
        try {
            return A.supplyFree();
        } catch (Exception | Error ignored) {
            return -1;
        }
    }
}
