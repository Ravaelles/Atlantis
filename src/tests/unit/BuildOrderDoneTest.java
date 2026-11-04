package tests.unit;

import atlantis.production.engine.GameStateSnapshot;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The build-order-done seam the dynamic-tech gate reads (owner order
 * 2026-11-04): an unfinished non-dynamic row means the opening owns
 * the game; dynamic rows never own it. Pure over a row list, no game.
 */
public class BuildOrderDoneTest {

    private static ProductionOrder row(AUnitType type, boolean dynamic) {
        ProductionOrder order = new ProductionOrder(type, 10);
        if (!dynamic) order.markAsNotDynamic();

        return order;
    }

    @Test
    public void unfinishedBuildOrderRowMeansNotDone() {
        List<ProductionOrder> rows = new ArrayList<>(Arrays.asList(
            row(AUnitType.Protoss_Gateway, false),
            row(AUnitType.Protoss_Zealot, true)));

        assertFalse(GameStateSnapshot.buildOrderDone(rows),
            "one unfinished BO row keeps the opening alive");
    }

    @Test
    public void onlyDynamicRowsMeansDone() {
        List<ProductionOrder> rows = new ArrayList<>(Arrays.asList(
            row(AUnitType.Protoss_Zealot, true),
            row(AUnitType.Protoss_Dragoon, true)));

        assertTrue(GameStateSnapshot.buildOrderDone(rows),
            "dynamic demand never owns the opening");
    }

    @Test
    public void emptyOrNullMeansDone() {
        assertTrue(GameStateSnapshot.buildOrderDone(new ArrayList<ProductionOrder>()),
            "nothing unfinished, nothing to wait for");
        assertTrue(GameStateSnapshot.buildOrderDone(null),
            "no list reads as done, never as blocked");
    }
}
