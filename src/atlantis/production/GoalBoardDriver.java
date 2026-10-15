package atlantis.production;

import atlantis.decisions.board.BoardState;
import atlantis.decisions.board.Goal;
import atlantis.decisions.board.GoalBoard;
import atlantis.decisions.board.GoalRanks;
import atlantis.units.AUnitType;

import java.util.Collections;

/**
 * Goal-board driver, first consumer (NEXT #91, owner order 2A): emergency
 * preemption only. When the board top is an emergency-ranked goal, pumps
 * pause unless the bank covers both - a cloaked wraith does not wait for
 * a calm worker cycle. Everything else keeps legacy brakes (a top-only
 * driver would also stop Nexus saving, which lives at rank 80 by design).
 */
public final class GoalBoardDriver {

    private GoalBoardDriver() {
    }

    public static boolean pauseWorkers(BoardState state) {
        Goal top = GoalBoard.top(state, GoalBoard.openingGoals(), Collections.<String>emptySet());
        if (top == null || top.rank() > GoalRanks.EMERGENCY_BREAK) {
            return false;
        }
        int priceMinerals = mineralPrice(top.id());
        int priceGas = gasPrice(top.id());
        return state.minerals < priceMinerals + 50 || state.gas < priceGas;
    }

    /**
     * Engine-truth prices (AUnitType), never typed from memory - the board
     * itself stays priceless on purpose.
     */
    static int mineralPrice(String goalId) {
        if ("supply-pylon".equals(goalId)) return AUnitType.Protoss_Pylon.mineralPrice();
        if ("detection".equals(goalId)) return AUnitType.Protoss_Forge.mineralPrice();
        if ("gateway".equals(goalId)) return AUnitType.Protoss_Gateway.mineralPrice();
        if ("core".equals(goalId)) return AUnitType.Protoss_Cybernetics_Core.mineralPrice();
        if ("assimilator".equals(goalId)) return AUnitType.Protoss_Assimilator.mineralPrice();
        if ("expansion".equals(goalId)) return AUnitType.Protoss_Nexus.mineralPrice();
        return 100;
    }

    static int gasPrice(String goalId) {
        if ("assimilator".equals(goalId)) return AUnitType.Protoss_Assimilator.gasPrice();
        return 0;
    }
}
