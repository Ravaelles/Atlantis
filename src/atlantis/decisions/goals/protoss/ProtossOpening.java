package atlantis.decisions.goals.protoss;

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
/**
 * Protoss opening vocabulary (domain language): one class per goal, this
 * file only assembles the ladder in play order. Game meaning lives here;
 * the ranking machinery in the parent package never names a game concept.
 */
public final class ProtossOpening {

    private ProtossOpening() {
    }

    public static List<Goal> goals() {
        List<Goal> goals = new ArrayList<Goal>();
        goals.add(new SupplyPylonGoal());
        goals.add(new DetectionGoal());
        goals.add(new WorkersGoal());
        goals.add(new GatewayGoal());
        goals.add(new CyberneticsCoreGoal());
        goals.add(new AssimilatorGoal());
        goals.add(new WallInGoal());
        goals.add(new CannonGoal());
        goals.add(new OpeningArmyGoal());
        goals.add(new SustainedZealotFlowGoal());
        goals.add(new SustainedDragoonFlowGoal());
        goals.add(new ExpansionGoal());
        return goals;
    }
}
