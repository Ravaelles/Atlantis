package atlantis.decisions.goals.protoss;

import atlantis.decisions.board.BoardState;
import atlantis.decisions.board.Goal;
import atlantis.units.AUnitType;

/**
 * Production capacity for the opening.
 */
public final class GatewayGoal implements Goal {

    public static final int WANTED_GATEWAYS = 2;

    @Override
    public String id() {
        return "gateway";
    }

    @Override
    public boolean active(BoardState state) {
        return state.gatewayCount < WANTED_GATEWAYS;
    }

    @Override
    public int rank() {
        return GoalRanks.GATEWAY;
    }

    @Override
    public int priceMinerals() {
        return AUnitType.Protoss_Gateway.mineralPrice();
    }

    @Override
    public int priceGas() {
        return AUnitType.Protoss_Gateway.gasPrice();
    }
}
