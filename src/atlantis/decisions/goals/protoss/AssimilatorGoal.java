package atlantis.decisions.goals.protoss;

import atlantis.decisions.board.BoardState;
import atlantis.decisions.board.Goal;
import atlantis.units.AUnitType;

/**
 * Gas income, Core-first.
 */
public final class AssimilatorGoal implements Goal {

    @Override
    public String id() {
        return "assimilator";
    }

    /**
     * The legacy first-Assimilator gate, row for row: queue once the
     * Core stands or is nearly done warping (under 80 warp seconds to
     * go - the legacy `CyberneticsCoreNotNearCompletion` bound), so the
     * gas starts flowing as the Core lands instead of a full warp later.
     */
    @Override
    public boolean active(BoardState state) {
        if (state.hasAssimilator) return false;

        if (state.hasCyberneticsCore) return true;

        return state.coreRemainingSeconds >= 0 && state.coreRemainingSeconds < 80;
    }

    @Override
    public int rank() {
        return GoalRanks.ASSIMILATOR;
    }

    @Override
    public int priceMinerals() {
        return AUnitType.Protoss_Assimilator.mineralPrice();
    }

    @Override
    public int priceGas() {
        return AUnitType.Protoss_Assimilator.gasPrice();
    }
}
