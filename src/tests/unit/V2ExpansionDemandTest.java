package tests.unit;

import atlantis.production.v2.ProductionGoal;
import atlantis.production.v2.ProductionRacePolicy;
import atlantis.production.v2.UnitProducible;
import atlantis.production.v2.goals.DynamicGoals;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pins the V2 expansion demand (NEXT #45 race-army/economy coverage):
 * a second Nexus is wanted once workers hit 25 per standing base.
 * OpenBW `202456` (V2 LIVE) shows demand is not the gap - the goal
 * fired while the bank held 8000+, yet the builder committed only at
 * 17:29, too late to warp. Demand side locked here; issue timing stays
 * open (needs a longer game, not a different threshold).
 *
 * <p>Pure snapshots, no game, no behavior change.</p>
 */
public class V2ExpansionDemandTest {

    private static final String NEXUS = UnitProducible.of(AUnitType.Protoss_Nexus).id();

    private static DynamicGoals.GameSnapshot state(int workers, int bases, int basesExisting) {
        return new DynamicGoals.GameSnapshot(
            workers, bases, 30, 10, 40, 500, false, 7, basesExisting,
            0, false, false, 0,
            200, true, 0, 1, false, 0);
    }

    private static boolean wantsNexus(DynamicGoals.GameSnapshot snapshot) {
        List<ProductionGoal> goals =
            DynamicGoals.contribute(snapshot, ProductionRacePolicy.forRace("Protoss"));
        return goals.stream().anyMatch(goal -> goal.item().id().equals(NEXUS));
    }

    @Test
    public void saturatedBaseWantsExpansion() {
        assertTrue(wantsNexus(state(25, 1, 1)),
            "25 workers on 1 base hit 25-per-base - expansion is wanted");
    }

    @Test
    public void oneWorkerShortStaysHome() {
        assertFalse(wantsNexus(state(24, 1, 1)),
            "24 workers are below the threshold - no expansion yet");
    }

    @Test
    public void baseUnderConstructionSatisfiesDemand() {
        assertFalse(wantsNexus(state(30, 1, 2)),
            "a base being built already answers the want - never double-order");
    }

    @Test
    public void fourBasesAreEnough() {
        assertFalse(wantsNexus(state(100, 4, 4)),
            "the cap holds even with the workers for more");
    }
}
