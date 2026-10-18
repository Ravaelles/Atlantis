package atlantis.production;

import atlantis.architecture.Commander;
import atlantis.game.A;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.dynamic.workers.AutoProduceWorkersCommander;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.ReservedResources;
import atlantis.units.AUnitType;
import atlantis.util.We;
import atlantis.util.log.EventLog;
import atlantis.util.log.LogKey;

/**
 * Grep-able CRITICAL rows for the three economy diseases (owner 2026-10-19:
 * game balance is the goal, so these stay loud). Edge-triggered: each fires
 * once when its condition turns true, not once per frame (measured 1-3
 * episodes per long game on retained runs - per-frame firing would be ~40
 * rows of the same fact).
 *
 * <ul>
 * <li>FREE SUPPLY above 14: more headroom than a Pylon warp justifies
 * (a Pylon lands +8). Names free/total, bank and pylons already coming.</li>
 * <li>3+ PYLONS in production at once: pipeline overlap is two, the third
 * is a duplicate author (WO-144 single author is the real fix).</li>
 * <li>BANK above 800 with a why-breakdown: free supply (0 = blocked),
 * reserved minerals (demand waiting on the queue), not-ready order
 * count, and the worker pump's refusing gate - so the next hoard reads
 * as a cause, not a number.</li>
 * </ul>
 *
 * <p>Read-only observability, no policy: safe in both LIVE and legacy
 * lists, and the rows it emits are exactly what the stub twins below
 * assert, so the same queries run against logs without a game.</p>
 */
public class ProductionHealthWatcher extends Commander {

    /** Free supply above this is overstock (a Pylon lands +8). */
    public static final int MAX_HEALTHY_FREE_SUPPLY = 14;

    /** Unfinished Pylons at or above this is a flood (overlap allows two). */
    public static final int MAX_HEALTHY_PYLONS_IN_FLIGHT = 2;

    /** Bank above this is a hoard (with the why-breakdown attached). */
    public static final int HOARD_BANK_MINERALS = 800;

    private static boolean overstockActive = false;
    private static boolean floodActive = false;
    private static boolean hoardActive = false;

    /** Test seam: edge memory is per-JVM, tests reset it. */
    public static void clearForTests() {
        overstockActive = false;
        floodActive = false;
        hoardActive = false;
    }

    @Override
    public boolean applies() {
        return We.protoss();
    }

    @Override
    protected boolean handle() {
        checkOverstock();
        checkFlood();
        checkHoard();
        return false;
    }

    /** Public seam: the edge logic pins in stub states without running the frame. */
    public static void checkOverstock() {
        boolean over = A.supplyTotal() - A.supplyUsed() > MAX_HEALTHY_FREE_SUPPLY;
        if (over && !overstockActive) {
            EventLog.key(LogKey.SUPPLY_OVERSTOCK)
                .param("free", A.supplyTotal() - A.supplyUsed())
                .param("used", A.supplyUsed())
                .param("total", A.supplyTotal())
                .param("minerals", A.minerals())
                .param("pylonsComing", ConstructionRequests.countNotFinishedOfType(AUnitType.Protoss_Pylon))
                .log(A.now(), A.minSec());
        }
        overstockActive = over;
    }

    /** Public seam: the edge logic pins in stub states without running the frame. */
    public static void checkFlood() {
        int coming = ConstructionRequests.countNotFinishedOfType(AUnitType.Protoss_Pylon);
        boolean flood = coming > MAX_HEALTHY_PYLONS_IN_FLIGHT;
        if (flood && !floodActive) {
            EventLog.key(LogKey.PYLON_FLOOD)
                .param("coming", coming)
                .param("minerals", A.minerals())
                .param("supplyUsed", A.supplyUsed())
                .param("supplyTotal", A.supplyTotal())
                .log(A.now(), A.minSec());
        }
        floodActive = flood;
    }

    /** Public seam: the edge logic pins in stub states without running the frame. */
    public static void checkHoard() {
        boolean hoard = A.minerals() > HOARD_BANK_MINERALS;
        if (hoard && !hoardActive) {
            int notReady = Queue.get() == null ? -1 : Queue.get().statusNotReady().size();
            EventLog.key(LogKey.BANK_HOARD)
                .param("minerals", A.minerals())
                .param("gas", A.gas())
                .param("reserved", ReservedResources.minerals())
                .param("freeSupply", A.supplyTotal() - A.supplyUsed())
                .param("notReady", notReady)
                .param("workerGate", AutoProduceWorkersCommander.REASON)
                .log(A.now(), A.minSec());
        }
        hoardActive = hoard;
    }
}
