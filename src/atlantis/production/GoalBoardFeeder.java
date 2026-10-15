package atlantis.production;

import atlantis.decisions.board.BoardState;
import atlantis.decisions.board.Goal;
import atlantis.decisions.board.GoalBoard;
import atlantis.decisions.board.goals.ProtossOpening;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnitType;

import java.util.List;

/**
 * Goal-board feeder (NEXT #91): board → queue direction. For active goals
 * nobody owns, ensure exactly one order/construction is in flight - then
 * stop. Queue rows don't spend (readiness, builders and the engine still
 * decide), so feeding is safe by construction; it only closes the
 * "wanted but never ordered" gap.
 *
 * <p>Covered goals (buildings with no good single owner): supply-pylon,
 * gateway, core, assimilator. NOT covered, with reasons: workers (the pump
 * trains directly - a Probe row would double-produce), army (Zealot vs
 * Dragoon choice is strategy, pumps own it), expansion (site approval
 * lives in ProtossExpandNow), detection (response system owns it).</p>
 */
public final class GoalBoardFeeder {

    private GoalBoardFeeder() {
    }

    /**
     * @return ids fed this call (throttled log sits with the caller).
     */
    public static String ensure(BoardState state) {
        List<Goal> ranked = GoalBoard.ranked(state, ProtossOpening.goals());
        StringBuilder fed = new StringBuilder();
        for (Goal goal : ranked) {
            AUnitType type = orderType(goal.id());
            if (type == null) {
                continue;
            }
            if (inFlight(type)) {
                continue;
            }
            ProductionOrder added = addFor(goal.id(), type);
            if (added != null && fed.length() == 0) {
                fed.append(goal.id());
            }
            else if (added != null) {
                fed.append(",").append(goal.id());
            }
        }
        return fed.toString();
    }

    private static boolean inFlight(AUnitType type) {
        if (Queue.get() == null) {
            return true;
        }
        if (!Queue.get().notFinished().ofType(type).isEmpty()) {
            return true;
        }
        return !ConstructionRequests.notFinishedOfType(type).isEmpty();
    }

    private static AUnitType orderType(String goalId) {
        if ("supply-pylon".equals(goalId)) return AUnitType.Protoss_Pylon;
        if ("gateway".equals(goalId)) return AUnitType.Protoss_Gateway;
        if ("core".equals(goalId)) return AUnitType.Protoss_Cybernetics_Core;
        if ("assimilator".equals(goalId)) return AUnitType.Protoss_Assimilator;
        return null;
    }

    private static ProductionOrder addFor(String goalId, AUnitType type) {
        if ("supply-pylon".equals(goalId)) return AddToQueue.withTopPriority(type);
        if ("core".equals(goalId)) return AddToQueue.withHighPriority(type);
        if ("assimilator".equals(goalId)) return AddToQueue.withHighPriority(type);
        return AddToQueue.withStandardPriority(type);
    }

    /** Commander entry: snapshot, feed, throttled single line. */
    public static void update() {
        BoardState state = GoalBoardAdvisor.snapshot();
        String fed = ensure(state);
        if (!fed.isEmpty()) {
            atlantis.util.log.ErrorLog.printMaxOncePerMinute(
                "BOARD_FED " + fed
                    + " min=" + state.minerals
                    + " sup=" + state.supplyUsed + "/" + state.supplyTotal);
        }
    }
}
