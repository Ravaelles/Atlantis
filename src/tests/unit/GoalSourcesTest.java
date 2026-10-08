package tests.unit;

import atlantis.production.v2.ProductionGoal;
import atlantis.production.v2.UnitProducible;
import atlantis.production.v2.goals.DynamicGoals;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pins the goal-source layer of production-v2 (M5 of
 * _AI/redesign/01_PRODUCTION.md): the build order and the dynamic generators
 * produce <b>declarations</b>, not orders, and the priority bands are the only
 * thing deciding who wins.
 *
 * <p>
 * This is the layer that replaces the legacy dynamic commanders, so what it
 * must not do is at least as important as what it must: it never touches the
 * game, never issues a command, and never keeps state between frames. The tests
 * below therefore only ever call it with numbers and read the goals back.
 * </p>
 */
public class GoalSourcesTest {

    // ---- dynamic goals -----------------------------------------------------

    @Test
    public void workersAreRequestedUntilBasesAreSaturated() {
        DynamicGoals.GameSnapshot unsaturated = new DynamicGoals.GameSnapshot(8, 1, 10, 6, 16, 200, true);

        List<ProductionGoal> goals = DynamicGoals.contribute(unsaturated);

        assertTrue(contains(goals, workerType()), "an unsaturated base asks for a worker");
    }

    @Test
    public void workersStopAtSaturationAndAtTheHardCap() {
        // 25 workers per base is the rule; 80 is the backstop that survives a
        // lost base report.
        DynamicGoals.GameSnapshot saturated = new DynamicGoals.GameSnapshot(25, 1, 50, 20, 70, 1000, false);
        DynamicGoals.GameSnapshot capped = new DynamicGoals.GameSnapshot(80, 4, 160, 40, 200, 1000, false);

        assertFalse(contains(DynamicGoals.contribute(saturated), workerType()),
                "a saturated single base stops asking");
        assertFalse(contains(DynamicGoals.contribute(capped), workerType()),
                "the absolute cap stops even a four-base economy");
    }

    @Test
    public void twoBasesSaturateAtTwiceTheWorkersOfOne() {
        DynamicGoals.GameSnapshot oneBaseEnough = new DynamicGoals.GameSnapshot(26, 1, 52, 20, 72, 500, false);
        DynamicGoals.GameSnapshot twoBasesNotEnough = new DynamicGoals.GameSnapshot(26, 2, 52, 20, 72, 500, false);

        assertFalse(contains(DynamicGoals.contribute(oneBaseEnough), workerType()));
        assertTrue(contains(DynamicGoals.contribute(twoBasesNotEnough), workerType()),
                "the second base raises the worker target");
    }

    @Test
    public void supplyGoalIsNormalWhenSlackIsSmallAndEmergencyWhenItIsGone() {
        DynamicGoals.GameSnapshot normal = new DynamicGoals.GameSnapshot(10, 1, 30, 3, 33, 500, false);
        DynamicGoals.GameSnapshot emergency = new DynamicGoals.GameSnapshot(10, 1, 32, 0, 32, 500, false);

        ProductionGoal normalGoal = firstOf(DynamicGoals.contribute(normal), supplyType());
        ProductionGoal emergencyGoal = firstOf(DynamicGoals.contribute(emergency), supplyType());

        assertEquals(ProductionGoal.PRIORITY_DEPOTS, normalGoal.priority());
        assertEquals(ProductionGoal.PRIORITY_EMERGENCY, emergencyGoal.priority(),
                "supply block is an emergency - it outranks everything");
    }

    @Test
    public void noSupplyGoalWhenThereIsSlack() {
        DynamicGoals.GameSnapshot plenty =
                new DynamicGoals.GameSnapshot(10, 1, 20, 20, 40, 500, false);

        assertFalse(contains(DynamicGoals.contribute(plenty), supplyType()));
    }

    @Test
    public void noArmyGoalBeforeTheEconomyCanSupportOne() {
        DynamicGoals.GameSnapshot tooEarly =
                new DynamicGoals.GameSnapshot(4, 1, 6, 6, 12, 100, true);

        assertFalse(contains(DynamicGoals.contribute(tooEarly), armyType()),
                "an army at 6 supply would starve the opening");
    }

    @Test
    public void armyGoalScalesWithTheWorkerCount() {
        DynamicGoals.GameSnapshot small =
                new DynamicGoals.GameSnapshot(12, 1, 20, 10, 30, 500, false);
        DynamicGoals.GameSnapshot large =
                new DynamicGoals.GameSnapshot(40, 2, 80, 20, 100, 500, false);

        int smallCount = firstOf(DynamicGoals.contribute(small), armyType()).count();
        int largeCount = firstOf(DynamicGoals.contribute(large), armyType()).count();

        assertTrue(largeCount > smallCount,
                "a bigger economy asks for a bigger baseline army: "
                        + smallCount + " vs " + largeCount);
    }

    @Test
    public void expansionIsWantedOnlyWhenTheCurrentBasesAreSaturated() {
        DynamicGoals.GameSnapshot notSaturated =
                new DynamicGoals.GameSnapshot(10, 1, 15, 10, 25, 600, false);
        DynamicGoals.GameSnapshot saturated =
                new DynamicGoals.GameSnapshot(25, 1, 45, 10, 55, 600, false);

        assertFalse(contains(DynamicGoals.contribute(notSaturated), baseType()),
                "an unsaturated base does not ask for another one");
        assertTrue(contains(DynamicGoals.contribute(saturated), baseType()),
                "a saturated base does ask");
    }

    @Test
    public void expansionStopsAtFourBases() {
        DynamicGoals.GameSnapshot fourBases =
                new DynamicGoals.GameSnapshot(100, 4, 180, 20, 200, 2000, false);

        assertFalse(contains(DynamicGoals.contribute(fourBases), baseType()),
                "four bases is the cap of this generator");
    }

    @Test
    public void supplyEmergencyOutranksWorkersWhenBothCompete() {
        DynamicGoals.GameSnapshot aboutToBlock = new DynamicGoals.GameSnapshot(5, 1, 15, 0, 15, 500, true);

        List<ProductionGoal> goals = DynamicGoals.contribute(aboutToBlock);
        Collections.sort(goals);

        assertEquals(supplyType(), goals.get(0).item().id(),
                "the supply provider must be scheduled before the next worker");
        assertTrue(goals.get(0).priority() < goals.get(1).priority());
    }

    @Test
    public void expansionIsNotAskedAgainWhileTheBaseIsUnderConstruction() {
        // The regression: the generator counted only finished bases, so a base
        // being built was "missing" every frame and a second one was declared.
        DynamicGoals.GameSnapshot buildingNow =
                new DynamicGoals.GameSnapshot(25, 1, 45, 10, 55, 600, false, 0, 2);
        DynamicGoals.GameSnapshot missing = new DynamicGoals.GameSnapshot(25, 1, 45, 10, 55, 600, false, 0, 1);

        assertFalse(contains(DynamicGoals.contribute(buildingNow), baseType()),
                "a base under construction satisfies the expansion goal");
        assertTrue(contains(DynamicGoals.contribute(missing), baseType()));
    }

    @Test
    public void armyGoalAsksOnlyForTheMissingUnits() {
        DynamicGoals.GameSnapshot noArmy =
                new DynamicGoals.GameSnapshot(40, 2, 80, 20, 100, 500, false, 0, 2);
        DynamicGoals.GameSnapshot halfArmy =
                new DynamicGoals.GameSnapshot(40, 2, 80, 20, 100, 500, false, 5, 2);
        DynamicGoals.GameSnapshot fullArmy =
                new DynamicGoals.GameSnapshot(40, 2, 80, 20, 100, 500, false, 10, 2);

        assertEquals(10, firstOf(DynamicGoals.contribute(noArmy), armyType()).count());
        assertEquals(5, firstOf(DynamicGoals.contribute(halfArmy), armyType()).count(),
                "existing units count towards the baseline");
        assertFalse(contains(DynamicGoals.contribute(fullArmy), armyType()),
                "a satisfied army goal asks for nothing");
    }

    @Test
    public void theSupplyRuleIsDeclaredInExactlyOnePlace() {
        // Two generators used to emit a Pylon for the same situation; the
        // scheduler honours two goals as two buildings. One module owns the rule.
        DynamicGoals.GameSnapshot low = new DynamicGoals.GameSnapshot(10, 1, 29, 1, 30, 500, false);

        ProductionGoal fromDynamic = firstOf(DynamicGoals.contribute(low), supplyType());
        ProductionGoal fromPullForward = atlantis.production.v2.goals.PullForwardGoals.supplyGoal(
                new atlantis.production.v2.goals.PullForwardGoals.Snapshot(1, 500, 10, 1, 1));

        assertNotNull(fromDynamic);
        assertEquals(fromDynamic.priority(), fromPullForward.priority(),
                "the pull-forward rule defers to the same one implementation");
        assertFalse(contains(atlantis.production.v2.goals.PullForwardGoals.contribute(
                        new atlantis.production.v2.ResourceTimeline(0, 500, 500, 0, 1, 30),
                        new atlantis.production.v2.goals.PullForwardGoals.Snapshot(1, 500, 10, 0, 1)),
                supplyType()),
                "and does not declare a second Pylon of its own");
    }

    // ---- helpers -----------------------------------------------------------

    private static boolean contains(List<ProductionGoal> goals, String typeId) {
        return firstOf(goals, typeId) != null;
    }

    private static ProductionGoal firstOf(List<ProductionGoal> goals, String typeId) {
        for (ProductionGoal goal : goals) {
            if (goal.item().id().equals(typeId))
                return goal;
        }
        return null;
    }

    private static String workerType() {
        return UnitProducible.of(probe()).id();
    }

    private static String supplyType() {
        return UnitProducible.of(AUnitType.Protoss_Pylon).id();
    }

    private static String armyType() {
        return UnitProducible.of(AUnitType.Protoss_Zealot).id();
    }

    private static String baseType() {
        return UnitProducible.of(AUnitType.Protoss_Nexus).id();
    }

    private static AUnitType probe() {
        return AUnitType.Protoss_Probe;
    }
}
