package atlantis.production.orders.build;

import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnitType;

import java.util.ArrayList;

public class CurrentBuildOrder {

    /**
     * Build order currently in use.
     * switchToBuildOrder(ABuildOrder buildOrder)
     */
    private static ABuildOrder currentBuildOrder = null;

    /**
     * Returns currently active build order.
     */
    public static ABuildOrder get() {
        return currentBuildOrder;
    }

    public static void set(ABuildOrder buildOrder) {
        currentBuildOrder = buildOrder;

        if (currentBuildOrder.productionOrders().isEmpty()) {
            System.err.println("Invalid Build Order: no production orders. Fix the file.");
        }
    }

    /**
     * Test seam: forget the loaded build order (never null-derefs the
     * way {@code set(null)} would).
     */
    public static void clear() {
        currentBuildOrder = null;
    }

    /**
     * Does this build ever train a Dragoon? Read by the game root at
     * game start only: combat needs the build's intent for the
     * first-Dragoon gate (owner order 2026-10-26) and must not depend
     * on production code, so {@code OnGameStarted} hands the answer over
     * as a flag. Null build or no rows answers false - an unknown build
     * is never gated.
     */
    public static boolean plansDragoon() {
        ABuildOrder buildOrder = currentBuildOrder;
        if (buildOrder == null) return false;

        ArrayList<ProductionOrder> rows = buildOrder.productionOrders();
        if (rows == null) return false;

        for (ProductionOrder row : rows) {
            AUnitType unit = row == null ? null : row.unitType();
            if (unit != null && unit.isDragoon()) return true;
        }

        return false;
    }
}
