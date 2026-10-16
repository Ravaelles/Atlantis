package atlantis.decisions.goals.protoss;

import atlantis.decisions.board.BoardState;
import atlantis.decisions.board.Goal;
import atlantis.units.AUnitType;

/** Continuous economy until saturation. */
public final class WorkersGoal implements Goal {

    public static final int WORKERS_PER_BASE = 22;

    @Override
    public String id() {
        return "workers";
    }

    @Override
    public boolean active(BoardState state) {
        return state.workerCount < workersWanted(state);
    }

    @Override
    public int rank() {
        return GoalRanks.WORKERS;
    }

    public static int workersWanted(BoardState state) {
        return WORKERS_PER_BASE * Math.max(1, state.baseCount);
    }

    @Override
    public int priceMinerals() {
        return AUnitType.Protoss_Probe.mineralPrice();
    }

    @Override
    public int priceGas() {
        return AUnitType.Protoss_Probe.gasPrice();
    }
}
