package atlantis.production.orders.build;

import atlantis.game.A;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.order.ProductionOrder;

/**
 * The memorized opening owns the game while it has due rows: a build-order
 * row with minSupply within reach (current supply + 2) that is still
 * unfinished. While owned, the dynamic producers stay off (their supply
 * pump lives elsewhere and keeps running - a late BO pylon must never
 * supply-block the script).
 *
 * <p>Two bounds keep this tight instead of total: only build-order rows
 * count (dynamic rows never own), and ownership ends at supply 18 - past
 * the double-gateway chapter the game is improvisation, and a stuck
 * far-future row must not freeze the mid-game pumps.</p>
 */
public class BuildOrderOwnsGame {

    /** Past this supply the opening is over, whatever rows linger. */
    public static final int OPENING_END_SUPPLY = 18;

    public static boolean owns() {
        // No game, no ownership: unit tests walk the commander tree without
        // a harness, and the open path is what they assert (UnitProductionPathTest).
        if (atlantis.Atlantis.game() == null) return false;
        if (A.supplyUsed() >= OPENING_END_SUPPLY) return false;

        for (ProductionOrder row : Queue.get().notFinished()
            .supplyAtMost(A.supplyUsed() + 2).list()) {
            if (!row.isDynamic()) return true;
        }
        return false;
    }
}
