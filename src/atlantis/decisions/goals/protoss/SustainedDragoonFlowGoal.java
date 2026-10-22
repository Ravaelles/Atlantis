package atlantis.decisions.goals.protoss;

import atlantis.decisions.board.BoardState;
import atlantis.decisions.board.Goal;
import atlantis.units.AUnitType;

/**
 * Sustained Dragoon flow past the opening: Gateway capacity stands idle
 * while the Dragoon posture holds (Core up, gas healthy for 50-a-unit
 * Dragoons - the same line the legacy pump draws at its gas50 gate).
 *
 * <p>Mutually exclusive with {@link SustainedZealotFlowGoal} (posture
 * splits the seat); silent while any Gateway trains and while
 * supply-blocked. Finer matchup choices (Zealots into mass lings)
 * stay with the pumps: the feeder this goal feeds only fills silence,
 * so a pump that acts always wins.</p>
 */
public final class SustainedDragoonFlowGoal implements Goal {

    @Override
    public String id() {
        return "sustained-dragoon";
    }

    @Override
    public boolean active(BoardState state) {
        if (state.gatewayCount < 1) return false;
        if (state.gatewaysTraining > 0) return false;
        if (state.supplyFree() < 2) return false;

        return SustainedZealotFlowGoal.dragoonPosture(state);
    }

    @Override
    public int rank() {
        return GoalRanks.SUSTAINED_ARMY;
    }

    @Override
    public int priceMinerals() {
        return AUnitType.Protoss_Dragoon.mineralPrice();
    }

    @Override
    public int priceGas() {
        return AUnitType.Protoss_Dragoon.gasPrice();
    }
}
