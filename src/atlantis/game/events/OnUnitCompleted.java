package atlantis.game.events;

import atlantis.combat.squad.NewUnitsToSquadsAssigner;
import atlantis.config.env.Env;
import atlantis.game.CameraCommander;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnit;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import bwapi.Unit;

public class OnUnitCompleted {
    public static void onUnitCompleted(Unit u) {
        if (u == null) {
            System.err.println("onUnitCompleted got null");
            return;
        }

        AUnit unit = AUnit.getById(u);
        unit.refreshType();
        if (unit.isOur()) {
            ourNewUnit(unit);
        }
    }

    public static void ourNewUnit(AUnit unit) {
//        ProductionQueueRebuilder.rebuildProductionQueueToExcludeProducedOrders();
        Count.clearCache();
        Select.clearCache();
        Queue.get().refresh();

        cleanUpProductionOrderAndConstruction(unit);

        (new NewUnitsToSquadsAssigner(unit)).possibleCombatUnitCreated();

        if (Env.isLocal() && unit.isBunker() && Count.bunkers() == 1) CameraCommander.centerCameraOn(unit);
    }

    private static void cleanUpProductionOrderAndConstruction(AUnit unit) {
        if (unit.isABuilding()) unit.setConstruction(null);

        ProductionOrder order = unit.productionOrder();
        if (order != null) order.releasedReservedResources();
    }
}
