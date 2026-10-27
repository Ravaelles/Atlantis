package atlantis.decisions.goals.protoss;

import atlantis.decisions.board.BoardState;
import atlantis.decisions.board.Goal;
import atlantis.units.AUnitType;

/**
 * Tech unlock for Dragoons, needs a Gateway first.
 */
public final class CyberneticsCoreGoal implements Goal {

    @Override
    public String id() {
        return "core";
    }

    @Override
    public boolean active(BoardState state) {
        return !state.hasCyberneticsCore && state.gatewayCount >= 1;
    }

    @Override
    public int rank() {
        return GoalRanks.CORE;
    }

    @Override
    public int priceMinerals() {
        return AUnitType.Protoss_Cybernetics_Core.mineralPrice();
    }

    @Override
    public int priceGas() {
        return AUnitType.Protoss_Cybernetics_Core.gasPrice();
    }
}
