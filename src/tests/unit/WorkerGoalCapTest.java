package tests.unit;

import atlantis.production.v2.ProductionGoal;
import atlantis.production.v2.ProductionRacePolicy;
import atlantis.production.v2.UnitProducible;
import atlantis.production.v2.goals.DynamicGoals;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The worker pump saturates the bases being built, not just the ones
 * standing (sim post-Nexus stall, NEXT #217: at 25 workers with the
 * natural still warping the pump went silent for ~1800 frames while the
 * bank climbed to 849). Pre-saturation is standard play; a cancelled
 * expansion merely leaves miners to re-balance, never a stall.
 */
@Tag("production")
public class WorkerGoalCapTest {

    @Test
    public void warpingBaseCountsTowardSaturation() {
        // 25 workers, one base standing, the natural warping: the pump
        // must keep going toward 50, not stop at 25.
        DynamicGoals.GameSnapshot warping =
            new DynamicGoals.GameSnapshot(25, 1, 49, 8, 57, 800, false, 0, 2);

        assertTrue(
            hasWorkerGoal(DynamicGoals.contribute(warping, ProductionRacePolicy.forRace("Protoss"))),
            "the warping base is saturation too - stopping at 25 starves the expansion");
    }

    @Test
    public void standingBasesStillCap() {
        // 25 workers on one standing base and nothing warping: capped.
        DynamicGoals.GameSnapshot capped =
            new DynamicGoals.GameSnapshot(25, 1, 49, 8, 57, 800, false, 0, 1);

        assertFalse(
            hasWorkerGoal(DynamicGoals.contribute(capped, ProductionRacePolicy.forRace("Protoss"))),
            "one standing base saturates at 25 - the cap still holds without a warping base");
    }

    @Test
    public void twoStandingBasesCapAtFifty() {
        DynamicGoals.GameSnapshot full =
            new DynamicGoals.GameSnapshot(50, 2, 90, 8, 98, 800, false, 0, 2);

        assertFalse(
            hasWorkerGoal(DynamicGoals.contribute(full, ProductionRacePolicy.forRace("Protoss"))),
            "50 workers on two bases is full saturation, warping or not");
    }

    private static boolean hasWorkerGoal(List<ProductionGoal> goals) {
        String workerId = UnitProducible.of(AUnitType.Protoss_Probe).id();

        return goals.stream().anyMatch(goal -> goal.item().id().equals(workerId));
    }
}
