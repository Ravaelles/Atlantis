package atlantis.production.dynamic.protoss.buildings;

import atlantis.decisions.goals.protoss.ForgeGoal;
import atlantis.production.GoalBoardAdvisor;
import atlantis.production.dynamic.DynamicCommanderHelpers;
import atlantis.units.select.Have;

import static atlantis.units.AUnitType.Protoss_Forge;

public class ProduceForge {
    public static boolean produce() {
        if (Have.forge()) return false;

        // Timing and reasons live on the board (ForgeGoal): DT prediction
        // plus matchup supply table, pinned in stub tests. This keeps the
        // already-have check and the queue-once dedup; the BOARD twin shows
        // the want instead of the per-reason error prints.
        if (!ForgeGoal.wanted(GoalBoardAdvisor.snapshot())) return false;

        return DynamicCommanderHelpers.buildToHaveOne(Protoss_Forge);
    }
}
