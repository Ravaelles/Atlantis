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

import java.util.ArrayList;
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
     *
     * <p>Package-visible: the advisor reads the same funded set for the
     * BOARD line, so games show what the wallet did.</p>
     *
     * <p>Servable goals only (ones with a feeder row): wants without a row
     * (workers, detection, army posture, expansion) are owned elsewhere,
     * and funding them here would only block the servable ones below
     * (measured: workers-50 always funded first, pushing the Gateway to
     * a 200 bank). Rank and opinion still see every goal.</p>
     */
    static Set<String> fundedIds(BoardState state, List<Goal> ranked) {
        int minerals = Math.max(0, state.minerals - ReservedResources.minerals());
        int gas = Math.max(0, state.gas - ReservedResources.gas());
        List<Goal> servable = new ArrayList<Goal>();
        for (Goal goal : ranked) {
            if (orderType(goal.id()) != null) servable.add(goal);
        }
        Set<String> funded = new HashSet<String>();
        for (Goal goal : GoalBoardWallet.funded(state, servable, minerals, gas)) {
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

    /**
     * The single board-to-type dictionary both directions read: the
     * feeder (which row a goal needs), the funding door (which goal a
     * row serves) and the commitment set below. One map, never three
     * if-chains to keep in sync.
     */
    private static final java.util.Map<String, AUnitType> GOAL_TYPES =
        new java.util.LinkedHashMap<String, AUnitType>();

    static {
        GOAL_TYPES.put("supply-pylon", AUnitType.Protoss_Pylon);
        GOAL_TYPES.put("gateway", AUnitType.Protoss_Gateway);
        GOAL_TYPES.put("core", AUnitType.Protoss_Cybernetics_Core);
        GOAL_TYPES.put("assimilator", AUnitType.Protoss_Assimilator);
        GOAL_TYPES.put("sustained-zealot", AUnitType.Protoss_Zealot);
        GOAL_TYPES.put("sustained-dragoon", AUnitType.Protoss_Dragoon);
    }

    private static AUnitType orderType(String goalId) {
        return GOAL_TYPES.get(goalId);
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

    /**
     * Board-owned goals with work already in flight (a queue row or a
     * live construction of their type). The commitment reader
     * ({@code GoalBoard.top}) takes these so started work keeps its
     * place instead of flapping between frames.
     *
     * <p>Public as a seam: driver, advisor and tests read it.</p>
     */
    public static Set<String> inFlightIds() {
        Set<String> inFlight = new HashSet<String>();
        for (String goal : GOAL_TYPES.keySet()) {
            if (inFlight(GOAL_TYPES.get(goal))) inFlight.add(goal);
        }
        return inFlight;
    }

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
        for (java.util.Map.Entry<String, AUnitType> entry : GOAL_TYPES.entrySet()) {
            if (type.equals(entry.getValue())) return entry.getKey();
        }
        return null;
    }

    /**
     * The board owns the WHEN for demand the feeder cannot serve with rows
     * (exact-tile wall pieces, detection answers): true when the last
     * ranking (fresh) wanted this goal id. Stale rankings fail open -
     * legacy triggers decide, exactly like {@code adoptionAllowed}.
     * Public as the demand-gate seam.
     */
    public static boolean wantedGoal(String id) {
        if (!boardFresh()) return true;
        return lastActiveIds.contains(id);
    }

    /** A ranking younger than the feeder cadence owns decisions; older, legacy triggers do. */
    public static boolean boardFresh() {
        return atlantis.game.A.now() - lastFundedFrame <= 30;
    }
}
