package atlantis.production.orders.production.queue.order;

import atlantis.battle.cluster.Mission;

import atlantis.architecture.Commander;
import atlantis.game.A;
import atlantis.information.tech.ATechRequests;
import atlantis.production.orders.production.queue.add.PreventDuplicateOrders;
import atlantis.production.requests.produce.ProduceBuilding;
import atlantis.production.requests.produce.ProduceUnit;
import atlantis.units.AUnitType;
import atlantis.util.log.ErrorLog;
import bwapi.TechType;
import bwapi.UpgradeType;

public class ProductionOrderHandler extends Commander {
    private static int TIMES_MISSION_ENFORCED = 0;

    private ProductionOrder order;

    public ProductionOrderHandler(ProductionOrder order) {
        this.order = order;
    }

    private static boolean isPreparing(ProductionOrder order) {
        return order.construction() != null
            && order.construction().status().isWaiting();
    }

    @Override
    protected boolean handle() {
        if (isAlreadyConsumed()) {
            return false;
        }

        // =========================================================

        // Produce UNIT
        if (order.unitType() != null) {
            AUnitType unitType = order.unitType();

            // A PREPARING construction is a reserved site, not work in
            // progress - the order still goes through ProduceBuilding,
            // which adopts the site instead of searching anew (owner
            // 2026-10-14).
            if (order.construction() != null && !isPreparing(order)) {
                return false;
            }

            if (unitType.isABuilding()) {
                if (ProduceBuilding.produceBuilding(unitType, order)) {
                    order.consume();
                }
            }
            else {
                if (ProduceUnit.produceUnit(unitType, order)) {
                    order.consume();
                    order.setStatus(OrderStatus.IN_PROGRESS);
                }
            }
        }

        // =========================================================
        // Produce UPGRADE

        else if (order.upgrade() != null) {
            UpgradeType upgrade = order.upgrade();
            if (ATechRequests.researchUpgrade(upgrade)) {
                order.setStatus(OrderStatus.IN_PROGRESS);
                order.consume();

                PreventDuplicateOrders.cancelPreviousNonStartedOrdersOf(upgrade, "Only one upgrade at a time");
            }
        }

        // =========================================================
        // Produce TECH

        else if (order.tech() != null) {
            TechType tech = order.tech();
            if (ATechRequests.researchTech(tech)) {
                // Phase A (__PLAN-DECISIONS): research lands here, so the
                // audit row lands here too.
                atlantis.decisions.board.DecisionLog.record(
                    atlantis.decisions.board.ADecision.decide(
                        atlantis.decisions.board.ADecision.Kind.RESEARCH, tech.name(),
                        order.toString(),
                        order.isDynamic()
                            ? atlantis.decisions.board.ADecision.Source.DYNAMIC
                            : atlantis.decisions.board.ADecision.Source.BUILD_ORDER)
                        .frame(A.now())
                        .supply(atlantis.game.A.supplyUsed(), atlantis.game.A.supplyTotal())
                        .bank(atlantis.game.A.minerals(), atlantis.game.A.gas())
                        .build());
                order.setStatus(OrderStatus.IN_PROGRESS);
                order.consume();

                PreventDuplicateOrders.cancelPreviousNonStartedOrdersOf(tech, "Only one tech at a time");
            }
        }

        // =========================================================
        // Mission CHANGE

        else if (order.mission() != null) {
            // @ToDo - disabled due to local issues
        }

        // === Nothing! ============================================

        else {
            ErrorLog.printMaxOncePerMinute(order + " was not handled at all!");
        }
        return false;
    }

    private boolean isAlreadyConsumed() {
        if (!order.isConsumed()) return false;

        if (
            order.isBuilding() && (
                order.construction() == null
                    || order.construction().buildingUnit() == null
                    || order.construction().buildingUnit().hp() <= 0
                    || order.construction().buildingUnit().isDead()
            )
        ) return false;


        return true;
    }
}
