package atlantis.game.listeners;

import atlantis.combat.squad.NewUnitsToSquadsAssigner;
import atlantis.combat.squad.alpha.Alpha;
import atlantis.config.env.Env;
import atlantis.game.CameraCommander;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
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

        boolean assigned = (new NewUnitsToSquadsAssigner(unit)).possibleCombatUnitCreated();

        if (assigned) {
//            System.out.println("Unit " + unit + " assigned to squad");
            AUnit leader = Alpha.get().leader();
            if (leader != null) {
                unit.move(leader, Actions.MOVE_FOLLOW, "NewUnitToSquad");
            }
        }

        if (Env.isLocal() && unit.isBunker() && Count.bunkers() == 1) CameraCommander.centerCameraOn(unit);
    }

    private static void cleanUpProductionOrderAndConstruction(AUnit unit) {
        if (unit.isABuilding()) unit.setConstruction(null);
    }
}
