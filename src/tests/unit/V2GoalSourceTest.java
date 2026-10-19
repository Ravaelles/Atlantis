package tests.unit;

import atlantis.production.v2.Producible;
import atlantis.production.v2.ProductionGoal;
import atlantis.production.v2.ProductionRacePolicy;
import atlantis.production.v2.ResourceTimeline;
import atlantis.production.v2.TargetPlacement;
import atlantis.production.v2.UnitProducible;
import atlantis.production.v2.goals.BuildOrderGoals;
import atlantis.production.v2.goals.BuildOrderProgress;
import atlantis.production.v2.goals.BuildOrderRow;
import atlantis.production.v2.goals.DynamicGoals;
import atlantis.production.v2.goals.PullForwardGoals;
import atlantis.production.v2.goals.V2GoalSourceTwin;
import atlantis.units.AUnitType;
import atlantis.util.log.EventLog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * WO-144 open question: which goal re-fires a duplicate V2 Pylon pair.
 * The three emitting lists tag their goals at creation ("build-order",
 * "dynamic", "pull-forward"), and the plan-time twin carries the tag -
 * so a pair 150 frames apart reads as a BO-row re-fire, pipeline
 * overlap, or a door hole instead of three identical Pylon rows.
 *
 * <p>Pure snapshots and numbers, no game: tagging is data, the twin is
 * one EventLog call over it.</p>
 */
public class V2GoalSourceTest {

    private static final String PYLON = UnitProducible.of(AUnitType.Protoss_Pylon).id();
    private static final String GATEWAY = UnitProducible.of(AUnitType.Protoss_Gateway).id();

    @BeforeEach
    public void clearLog() {
        EventLog.clearForTests();
    }

    @Test
    public void buildOrderGoalsCarryBuildOrderSource() {
        List<BuildOrderRow> rows = Arrays.asList(
            BuildOrderRow.of(UnitProducible.of(AUnitType.Protoss_Pylon), 8),
            BuildOrderRow.of(UnitProducible.of(AUnitType.Protoss_Gateway), 10));

        List<ProductionGoal> goals = BuildOrderGoals.from(rows, new Progress(30));

        assertTrue(goals.size() >= 2, "opening rows emit: " + goals);
        for (ProductionGoal goal : goals) {
            assertEquals("build-order", goal.source(), "every BO goal names its list: " + goal);
        }
    }

    @Test
    public void dynamicGoalsCarryDynamicSource() {
        DynamicGoals.GameSnapshot low =
            new DynamicGoals.GameSnapshot(10, 1, 29, 1, 30, 500, false);

        List<ProductionGoal> goals =
            DynamicGoals.contribute(low, ProductionRacePolicy.forRace("Protoss"));

        ProductionGoal supply = firstOf(goals, PYLON);
        assertTrue(supply != null, "low supply wants a provider");
        assertEquals("dynamic", supply.source(), "the supply goal names its list");
    }

    @Test
    public void pullForwardGoalsCarryPullForwardSource() {
        List<ProductionGoal> goals = PullForwardGoals.contribute(
            new ResourceTimeline(0, 500, 500, 0, 1, 30),
            new PullForwardGoals.Snapshot(1, 500, 10, 0, 1),
            ProductionRacePolicy.forRace("Protoss"));

        assertTrue(!goals.isEmpty(), "rich mineral bank with no refinery wants gas");
        for (ProductionGoal goal : goals) {
            assertEquals("pull-forward", goal.source(), "every pull-forward goal names its list");
        }
    }

    @Test
    public void directConstructionsStayUnknown() {
        ProductionGoal goal = new ProductionGoal(
            UnitProducible.of(AUnitType.Protoss_Pylon),
            ProductionGoal.PRIORITY_DEPOTS, 1, 0, TargetPlacement.anywhere());

        assertEquals("unknown", goal.source(), "untagged constructions stay visibly untagged");
    }

    @Test
    public void twinWitnessesOnlyProvidersWithTheirSources() {
        List<ProductionGoal> goals = new ArrayList<ProductionGoal>();
        goals.add(tagged(UnitProducible.of(AUnitType.Protoss_Pylon), "build-order"));
        goals.add(tagged(UnitProducible.of(AUnitType.Protoss_Pylon), "dynamic"));
        goals.add(tagged(UnitProducible.of(AUnitType.Protoss_Gateway), "dynamic"));

        V2GoalSourceTwin.emit(100, goals, PYLON);

        String log = EventLog.toJsonLines();
        assertEquals(2, countRows(log, "V2_GOAL_SOURCE"), "only provider goals witnessed: " + log);
        assertTrue(log.contains("\"source\":\"build-order\""), "BO author named: " + log);
        assertTrue(log.contains("\"source\":\"dynamic\""), "dynamic author named: " + log);
    }

    private static ProductionGoal tagged(Producible item, String source) {
        return new ProductionGoal(
            item, ProductionGoal.PRIORITY_DEPOTS, 1, 0, TargetPlacement.anywhere())
            .withSource(source);
    }

    private static ProductionGoal firstOf(List<ProductionGoal> goals, String id) {
        for (ProductionGoal goal : goals) {
            if (goal.item().id().equals(id)) return goal;
        }
        return null;
    }

    private static int countRows(String log, String key) {
        int count = 0;
        int from = 0;
        while (true) {
            int at = log.indexOf("\"key\":\"" + key + "\"", from);
            if (at < 0) return count;
            count++;
            from = at + 1;
        }
    }

    private static final class Progress implements BuildOrderProgress {
        private final int supply;
        private final Map<String, Integer> produced = new HashMap<String, Integer>();

        Progress(int supply) {
            this.supply = supply;
        }

        @Override
        public int supplyUsed() {
            return supply;
        }

        @Override
        public int produced(Producible item) {
            return produced.getOrDefault(item.id(), 0);
        }
    }
}
