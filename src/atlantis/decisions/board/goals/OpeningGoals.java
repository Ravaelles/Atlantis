package atlantis.decisions.board.goals;

import atlantis.decisions.board.Goal;
import atlantis.decisions.board.GoalBoard;

import java.util.ArrayList;
import java.util.List;

/**
 * Phase-1 goal vocabulary (Protoss opening): one class per goal (SRP),
 * this file only assembles the ladder. Each goal re-derives its condition
 * from game meaning with zero copy-paste from the legacy producers
 * (owner ruling 1A/3A): the legacy files stay readable for reference,
 * nothing is lifted.
 */
public final class OpeningGoals {

    private OpeningGoals() {
    }

    public static List<Goal> all() {
        List<Goal> goals = new ArrayList<Goal>();
        goals.add(new SupplyPylonGoal());
        goals.add(new DetectionGoal());
        goals.add(new WorkersGoal());
        goals.add(new GatewayGoal());
        goals.add(new CyberneticsCoreGoal());
        goals.add(new AssimilatorGoal());
        goals.add(new OpeningArmyGoal());
        goals.add(new ExpansionGoal());
        return goals;
    }
}
