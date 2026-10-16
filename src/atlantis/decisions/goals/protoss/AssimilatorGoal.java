package atlantis.decisions.goals.protoss;

import atlantis.decisions.board.BoardState;
import atlantis.decisions.board.Goal;
import atlantis.units.AUnitType;

/** Gas income, Core-first. */
public final class AssimilatorGoal implements Goal {

    @Override
    public String id() {
        return "assimilator";
    }

    @Override
    public boolean active(BoardState state) {
        return !state.hasAssimilator && state.hasCyberneticsCore;
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
