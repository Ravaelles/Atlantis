package atlantis.game.listeners;

import atlantis.combat.squad.NewUnitsToSquadsAssigner;
import atlantis.combat.squad.alpha.Alpha;
import atlantis.config.env.Env;
import atlantis.game.CameraCommander;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionOrderStatus;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Count;
import atlantis.units.select.Select;

import static atlantis.production.orders.production.queue.order.OrderStatus.FINISHED;

public class OnOurNewUnitCompleted {
    public static void ourNewUnitCompleted(AUnit unit) {
        cleanUpProductionOrderAndConstruction(unit);

        Count.clearCache();
        Select.clearCache();
        Queue.get().refresh();

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
        ProductionOrder order = unit.productionOrder();
        if (order != null) {
            order.setStatus(FINISHED);
        }

        if (unit.isABuilding()) {
            Construction construction = unit.construction();
            if (construction != null) construction.setStatus(ConstructionOrderStatus.FINISHED);

            unit.setConstruction(null);
        }
    }
}
