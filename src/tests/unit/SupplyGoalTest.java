package tests.unit;

import atlantis.production.v2.ProductionGoal;
import atlantis.production.v2.ProductionRacePolicy;
import atlantis.production.v2.UnitProducible;
import atlantis.production.v2.goals.DynamicGoals;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pins the board supply-goal WHEN table (NEXT #144 input): the exact free-supply
 * boundary and the coming-provider suppression. Both are load-bearing for the
 * SupplyCommander rewrite - without the suppression the scheduler orders a
 * Pylon per frame until the first one warps (the "2800 ordered Pylons" run).
 *
 * <p>
 * Stub states only, no game, no behavior change: the legacy commander keeps
 * firing while this test pins what the board side must preserve when it takes
 * over all supply demand.
 * </p>
 */
public class SupplyGoalTest {

    @Test
    public void worryBoundaryIsBetweenFourAndFiveFreeSupply() {
        DynamicGoals.GameSnapshot atFour =
            new DynamicGoals.GameSnapshot(10, 1, 30, 4, 34, 500, false);
        DynamicGoals.GameSnapshot atFive =
            new DynamicGoals.GameSnapshot(10, 1, 30, 5, 35, 500, false);

        ProductionGoal want = DynamicGoals.supplyGoal(atFour, ProductionRacePolicy.forRace("Protoss"));

        assertNotNull(want, "free supply 4 is the last level that still wants a provider");
        assertEquals(ProductionGoal.PRIORITY_DEPOTS, want.priority());
        assertNull(
            DynamicGoals.supplyGoal(atFive, ProductionRacePolicy.forRace("Protoss")),
            "free supply 5 wants nothing - the legacy commander also stands down above its worry band");
    }

    @Test
    public void comingProviderSuppressesEvenAnEmergency() {
        DynamicGoals.GameSnapshot blocked =
            new DynamicGoals.GameSnapshot(10, 1, 32, 0, 32, 500, false);
        blocked.supplyProvidersComing = 1;

        assertNull(
            DynamicGoals.supplyGoal(blocked, ProductionRacePolicy.forRace("Protoss")),
            "a provider already on the way counts - re-emitting every frame is the 2800-Pylons defect");
    }

    @Test
    public void contributeCarriesTheSuppressionEndToEnd() {
        DynamicGoals.GameSnapshot blocked =
            new DynamicGoals.GameSnapshot(10, 1, 32, 0, 32, 500, false);
        blocked.supplyProvidersComing = 1;

        List<ProductionGoal> goals =
            DynamicGoals.contribute(blocked, ProductionRacePolicy.forRace("Protoss"));

        String supplyId = UnitProducible.of(AUnitType.Protoss_Pylon).id();
        String workerId = UnitProducible.of(AUnitType.Protoss_Probe).id();
        assertFalse(
            goals.stream().anyMatch(goal -> goal.item().id().equals(supplyId)),
            "no supply row reaches the scheduler while a provider is coming");
        assertTrue(
            goals.stream().anyMatch(goal -> goal.item().id().equals(workerId)),
            "suppression silences the supply row only - the worker row still fires");
    }

    @Test
    public void aboveSixtyTwoProvidersMayFlyAtOnce() {
        // Owner 2026-10-19: past 60 supply production outruns warp
        // completion - holding to one unstarted provider starves the
        // pump, so the second coming is allowed and only the third
        // is refused.
        DynamicGoals.GameSnapshot oneComing =
            new DynamicGoals.GameSnapshot(10, 1, 64, 4, 68, 500, false);
        oneComing.supplyProvidersComing = 1;

        assertNotNull(
            DynamicGoals.supplyGoal(oneComing, ProductionRacePolicy.forRace("Protoss")),
            "one provider coming past 60 still wants its pair");

        DynamicGoals.GameSnapshot twoComing =
            new DynamicGoals.GameSnapshot(10, 1, 64, 4, 68, 500, false);
        twoComing.supplyProvidersComing = 2;

        assertNull(
            DynamicGoals.supplyGoal(twoComing, ProductionRacePolicy.forRace("Protoss")),
            "two providers coming past 60 is enough - the third waits");
    }

    @Test
    public void atSixtyOrBelowOneProviderBlocks() {
        DynamicGoals.GameSnapshot oneComing =
            new DynamicGoals.GameSnapshot(10, 1, 30, 4, 34, 500, false);
        oneComing.supplyProvidersComing = 1;

        assertNull(
            DynamicGoals.supplyGoal(oneComing, ProductionRacePolicy.forRace("Protoss")),
            "at 60 or below a single coming provider covers the need");
    }
}
