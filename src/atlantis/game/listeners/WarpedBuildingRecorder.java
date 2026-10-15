package atlantis.game.listeners;

import atlantis.game.A;
import atlantis.game.util.BuildingLedger;
import atlantis.production.orders.production.queue.ReservedResources;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnit;

/**
 * Single extraction point for the buildings ledger. The engine has two entry
 * points for "our building now exists" ({@code OnOurUnitCreated} for warps
 * and completions, {@code OnUnitMorph} for morphs) and both funnel here, so
 * the row shape cannot drift between them. The ledger dedupes by unit id.
 */
public class WarpedBuildingRecorder {

    public static void record(AUnit unit) {
        if (unit == null || !unit.isABuilding()) return;

        ProductionOrder order = unit.productionOrder();
        BuildingLedger.recordBuilding(
            unit.id(), unit.type().name(), A.now(), A.minSec(),
            A.supplyUsed(), A.supplyTotal(), A.minerals(), A.gas(),
            ReservedResources.minerals(), ReservedResources.gas(),
            order == null ? null : order.minSupply(),
            order == null ? null : order.toString()
        );
        BuildingLedger.flush();
    }

    /**
     * A combat unit the build order asked for. Orderless spawns (no demand
     * shown: starting units, Scarabs) stay out; workers never reach here.
     */
    public static void recordUnit(AUnit unit) {
        if (unit == null || unit.isABuilding() || !unit.isCombatUnit()) return;

        ProductionOrder order = unit.productionOrder();
        if (order == null) return;

        BuildingLedger.recordUnit(
            unit.id(), unit.type().name(), A.now(), A.minSec(),
            A.supplyUsed(), A.supplyTotal(), A.minerals(), A.gas(),
            ReservedResources.minerals(), ReservedResources.gas(),
            order.minSupply(), order.toString()
        );
        BuildingLedger.flush();
    }
}
