package atlantis.production;

import atlantis.decisions.board.BoardState;
import atlantis.decisions.board.Goal;
import atlantis.decisions.board.GoalBoard;
import atlantis.decisions.board.GoalBoardWallet;
import atlantis.decisions.goals.protoss.ProtossOpening;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.ReservedResources;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnitType;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Goal-board feeder (NEXT #91, wallet-gated since #93): board → queue
 * direction. For FUNDED active goals nobody owns, ensure exactly one
 * order/construction is in flight - then stop. Queue rows don't spend
 * (readiness, builders and the engine still decide), so feeding is safe
 * by construction; it only closes the "wanted but never ordered" gap.
 *
 * <p>Covered goals (buildings with no good single owner): supply-pylon,
 * gateway, core, assimilator. Army flow rows (Zealot/Dragoon) are covered
 * too, with one deliberate difference from the rest: the TYPE is the
 * strategy (posture picks it in the domain goal), the feeder only ensures
 * a row exists when the pumps are silent. A pump that acts always wins
 * (its row counts as in-flight), so finer matchup choices (Zealots into
 * mass lings) stay with the pumps untouched. NOT covered, with reasons:
 * workers (the pump trains directly - a Probe row would double-produce),
 * expansion (site approval lives in ProtossExpandNow), detection
 * (response system owns it).</p>
 */
public final class GoalBoardFeeder {

    private GoalBoardFeeder() {
    }

    /**
     * @return ids fed this call (throttled log sits with the caller).
     */
    public static String ensure(BoardState state) {
        List<Goal> ranked = GoalBoard.ranked(state, ProtossOpening.goals());
        Set<String> funded = fundedIds(state, ranked);
        publishFunding(ranked, funded);
        StringBuilder fed = new StringBuilder();
        for (Goal goal : ranked) {
            if (!funded.contains(goal.id())) {
                continue;
            }
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

    /**
     * The wallet pass: only funded goals get rows. The bank is the free
     * bank (snapshot minerals minus live reservations); the top goal and
     * emergencies always fund, the rest fit-or-wait. Rows stay spend-free,
     * so an unfunded goal loses a queue row for a frame, never a building.
     */
    private static Set<String> fundedIds(BoardState state, List<Goal> ranked) {
        int minerals = Math.max(0, state.minerals - ReservedResources.minerals());
        int gas = Math.max(0, state.gas - ReservedResources.gas());
        Set<String> funded = new HashSet<String>();
        for (Goal goal : GoalBoardWallet.funded(state, ranked, minerals, gas)) {
            funded.add(goal.id());
        }
        return funded;
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
        if ("sustained-zealot".equals(goalId)) return AUnitType.Protoss_Zealot;
        if ("sustained-dragoon".equals(goalId)) return AUnitType.Protoss_Dragoon;
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

    private static Set<String> lastActiveIds = new HashSet<String>();
    private static Set<String> lastFundedIds = new HashSet<String>();
    private static int lastFundedFrame = -1;

    private static void publishFunding(List<Goal> ranked, Set<String> funded) {
        lastActiveIds = new HashSet<String>();
        for (Goal goal : ranked) lastActiveIds.add(goal.id());
        lastFundedIds = funded;
        lastFundedFrame = atlantis.game.A.now();
    }

    /**
     * The execution door reads this (NEXT #93): a fresh builder claim for a
     * board-owned type proceeds only when its goal is funded - or when the
     * board does not want it at all (legacy owns inactive demand, like a
     * third Gateway past the goal, and everything without a goal). Stale
     * funding (older than the feeder cadence) fails open: refusing on last
     * frame's bank would be worse than allowing.
     *
     * <p>Public as the wallet seam: the single construction door lives in
     * another package and reads funding here.</p>
     */
    public static boolean adoptionAllowed(AUnitType type) {
        String goal = goalFor(type);
        if (goal == null) return true;
        if (atlantis.game.A.now() - lastFundedFrame > 30) return true;
        if (!lastActiveIds.contains(goal)) return true;
        return lastFundedIds.contains(goal);
    }

    private static String goalFor(AUnitType type) {
        if (type == null) return null;
        if (type.equals(AUnitType.Protoss_Pylon)) return "supply-pylon";
        if (type.equals(AUnitType.Protoss_Gateway)) return "gateway";
        if (type.equals(AUnitType.Protoss_Cybernetics_Core)) return "core";
        if (type.equals(AUnitType.Protoss_Assimilator)) return "assimilator";
        if (type.equals(AUnitType.Protoss_Zealot)) return "sustained-zealot";
        if (type.equals(AUnitType.Protoss_Dragoon)) return "sustained-dragoon";
        return null;
    }
}
