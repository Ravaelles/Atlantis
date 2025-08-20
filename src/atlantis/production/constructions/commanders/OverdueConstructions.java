package atlantis.production.constructions.commanders;

import atlantis.game.A;
import atlantis.map.position.APosition;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.builders.RefreshConstructionPosition;
import atlantis.production.constructions.cancelling.CancelNotStarted;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnitType;

public class OverdueConstructions {
    public static void handleIfOverdue(Construction construction) {
//        if (A.supplyUsed() <= 20) return;
        if (!A.everyNthGameFrame(31)) return;
        if (A.supplyUsed() <= 27) return;
        if (construction.buildingUnit() != null && construction.hasValidBuilderAndHeIsConstructing()) return;

        if (construction.isOverdue()) {
            AUnitType building = construction.buildingType();

//            System.err.println("Construction is overdue: " + building + " / started s ago: " + construction.startedSecondsAgo());

            construction.setBuilder(null);

            if (building.isPylon() || building.isBase() || building.isGasBuilding()) {
                whenDoesNotRequirePower(construction, building);
            }
            else {
                whenRequiresPower(construction);
            }
        }
    }

    private static void whenRequiresPower(Construction construction) {
        RefreshConstructionPosition.refreshIfNeeded(construction);
        if (construction.builder() == null || construction.builder().isDead()) {
            construction.assignOptimalBuilder();
        }
    }

    private static void whenDoesNotRequirePower(Construction construction, AUnitType building) {
        A.errPrintln(building + " construction is overdue, cancel it. Supply: " + A.supplyUsed() + "/" + A.supplyTotal());

        APosition oldPosition = construction.buildPosition();
        construction.cancel(building + " is overdue");

        if (oldPosition != null && building.isPylon()) {
            oldPosition = null;
        }

        if (building.isGasBuilding() || building.isGateway() || building.isPylon()) {
            CancelNotStarted.cancel(building, "Cancelling all not started: " + building);
        }

        ProductionOrder newOrder = AddToQueue.withHighPriority(building, oldPosition);

        A.errPrintln("---\nFresh requested order: " + newOrder);

        if (newOrder == null) {
            newOrder = AddToQueue.withTopPriority(building, oldPosition);
            A.errPrintln("---\nNow with TOP priority: " + newOrder + "\n---");
        }

        if (newOrder == null && !building.isCombatBuilding()) {
            Queue.get().notStarted().buildings().cancelAll("Last resort - cancelling ALL not started");
            newOrder = AddToQueue.withTopPriority(building);
            A.errPrintln("Crazy, FLUSHED QUEUE and new TOP: " + newOrder + "\n---");
        }
    }
}
