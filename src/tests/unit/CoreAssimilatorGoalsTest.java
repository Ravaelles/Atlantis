package tests.unit;

import atlantis.production.demand.DynamicGoals;
import atlantis.production.demand.ProductionGoal;
import atlantis.production.policy.ProductionRacePolicy;
import atlantis.production.producibles.UnitProducible;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * B1 (owner order): the engine half of the Core/Assimilator V2 home.
 * The board goal feeds the queue; these engine goals put the buildings
 * in the plan, so a V2-direct game (no build order) still techs instead
 * of banking into a stall (measured: 314 banked at f7800 with Pylon +
 * Gateway only, Core/Assim queued-but-never-issued).
 *
 * <p>Pure snapshot state, no world.</p>
 */
@Tag("production")
public class CoreAssimilatorGoalsTest {

    private static DynamicGoals.GameSnapshot state(int minerals, boolean hasCore,
                                                   int gateways, int gasBuildings) {
        DynamicGoals.GameSnapshot snapshot = new DynamicGoals.GameSnapshot(
            10, 1, 30, 5, 35, minerals, false, 8, 1,
            0, false, false, 0, 0, hasCore, 0, gasBuildings, false, 200);
        snapshot.gatewaysExisting = gateways;

        return snapshot;
    }

    private static List<ProductionGoal> contribute(DynamicGoals.GameSnapshot snapshot) {
        return DynamicGoals.contribute(snapshot, ProductionRacePolicy.forRace("Protoss"));
    }

    private static boolean hasGoal(List<ProductionGoal> goals, AUnitType type) {
        String id = UnitProducible.of(type).id();

        return goals.stream().anyMatch(goal -> goal.item().id().equals(id));
    }

    @Test
    public void gatewayPlusBankWantsCore() {
        assertTrue(hasGoal(contribute(state(400, false, 2, 0)), AUnitType.Protoss_Cybernetics_Core),
            "a Gateway and the bank want the Core building, not just the tech");
    }

    @Test
    public void standingCoreWantsNoSecond() {
        assertFalse(hasGoal(contribute(state(900, true, 2, 0)), AUnitType.Protoss_Cybernetics_Core),
            "a standing Core satisfies the building goal");
    }

    @Test
    public void noGatewayWantsNoCore() {
        assertFalse(hasGoal(contribute(state(900, false, 0, 0)), AUnitType.Protoss_Cybernetics_Core),
            "no Gateway, no Core building");
    }

    @Test
    public void standingCoreWantsFirstAssimilator() {
        assertTrue(hasGoal(contribute(state(400, true, 2, 0)), AUnitType.Protoss_Assimilator),
            "a standing Core with no gas building wants the first Assimilator");
    }

    @Test
    public void nearlyDoneCoreWantsAssimilatorEarly() {
        DynamicGoals.GameSnapshot snapshot = state(400, false, 2, 0);
        snapshot.coreRemainingSeconds = 40;

        assertTrue(hasGoal(contribute(snapshot), AUnitType.Protoss_Assimilator),
            "a Core 40 warp seconds out is close enough to queue gas (legacy <80s rule)");
    }

    @Test
    public void noCoreMeansNoAssimilator() {
        assertFalse(hasGoal(contribute(state(900, false, 2, 0)), AUnitType.Protoss_Assimilator),
            "without a Core (or one nearly done) there is nothing to refine for");
    }

    @Test
    public void existingAssimilatorWantsNoSecond() {
        DynamicGoals.GameSnapshot snapshot = state(900, true, 2, 1);

        assertFalse(hasGoal(contribute(snapshot), AUnitType.Protoss_Assimilator),
            "one gas building is enough for the first-gas goal");
    }
}
