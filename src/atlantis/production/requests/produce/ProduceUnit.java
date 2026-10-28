package atlantis.production.requests.produce;

import atlantis.decisions.board.ADecision;
import atlantis.decisions.board.DecisionDispatcher;
import atlantis.decisions.goals.protoss.ArmyQuestion;
import atlantis.game.A;
import atlantis.production.GoalBoardAdvisor;
import atlantis.production.orders.build.CurrentBuildOrder;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnitType;

public class ProduceUnit {
    // See ProduceWorker: the executor registers on first wrapper load,
    // game root or test alike.
    static {
        atlantis.production.ProductionDecisionExecutor.register();
    }

    public static boolean produceUnit(AUnitType type, ProductionOrder order) {
        assert !type.isABuilding();

        // Supply: OVERLORD / PYLON / DEPOT
//        if (AGame.supplyFree() == 0 && !unitType.isSupplyUnit()) {
//            // Supply production is handled by AtlantisSupplyManager
//            return false;
//        }

        // =========================================================
        // Worker

        if (type.isWorker()) {
            return ProduceWorker.produceWorker(order);
        }

        // =========================================================
        // Non-worker units walk through the single door like everything
        // else (Phase A - the affordability gate below stays put).

        ADecision want = new ArmyQuestion().ask(GoalBoardAdvisor.snapshot());
        if (want == null) return false;

        return DecisionDispatcher.issue(want.refine(
            type.name(),
            order == null ? "army-flow" : order.toString(),
            order != null && !order.isDynamic()
                ? ADecision.Source.BUILD_ORDER : ADecision.Source.DYNAMIC,
            order).atFrame(A.now()));
    }

    /**
     * The legacy army issuance (affordability gate included) - now reachable
     * only through the domain executor, never directly. Phase A.
     */
    public static boolean orderUnit(AUnitType type, ProductionOrder order) {
        if (type.isWorker()) {
            return ProduceWorker.produceWorker(order);
        }

        // =========================================================
        // Non-worker

        else if (A.canAfford(type.mineralPrice(), type.gasPrice())) {
            return CurrentBuildOrder.get().produceUnit(type, order);
        }
//        else {
//            AConsole.errPrintln("Can't afford " + type + " (" + type.getMineralPrice() + ", " + type.getGasPrice() + ")");
//        }

        return false;
    }
}
