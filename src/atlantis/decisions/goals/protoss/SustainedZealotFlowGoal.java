package atlantis.decisions.goals.protoss;

import atlantis.decisions.board.BoardState;
import atlantis.decisions.board.Goal;
import atlantis.units.AUnitType;

/**
 * Sustained Zealot flow past the opening: Gateway capacity stands idle
 * while no Dragoon posture holds (no Core, or gas too thin for 50-a-unit
 * Dragoons - the same posture line the legacy pump draws).
 *
 * <p>Mutually exclusive with {@link SustainedDragoonFlowGoal} by
 * construction (posture splits the seat); silent while any Gateway
 * trains, while supply-blocked, and while the opening army goal owns
 * the same choice through a live pump row (the feeder only fills
 * silence, never overrides - pumps choose first).</p>
 */
public final class SustainedZealotFlowGoal implements Goal {

    static boolean dragoonPosture(BoardState state) {
        return state.hasCyberneticsCore && state.gas >= 50;
    }

    @Override
    public String id() {
        return "sustained-zealot";
    }

    @Override
    public boolean active(BoardState state) {
        if (state.gatewayCount < 1) return false;
        if (state.gatewaysTraining > 0) return false;
        if (state.supplyFree() < 2) return false;

        return !dragoonPosture(state);
    }

    @Override
    public int rank() {
        return GoalRanks.SUSTAINED_ARMY;
    }

    @Override
    public int priceMinerals() {
        return AUnitType.Protoss_Zealot.mineralPrice();
    }

    @Override
    public int priceGas() {
        return AUnitType.Protoss_Zealot.gasPrice();
    }
}
