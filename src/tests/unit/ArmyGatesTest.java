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
 * Late army gates at the V2 goal layer (legacy
 * AllowProduceDragoon/AllowProduceZealot): basics stop at 188 supply,
 * Dragoons wait at 184+ with no room or no gas behind them unless the
 * bank is rich, Zealots brake at 180+/175+, and both yield the bank
 * while Dark Templar tech warps.
 *
 * <p>Pure snapshot state, no world.</p>
 */
@Tag("production")
public class ArmyGatesTest {

    private static DynamicGoals.GameSnapshot state(
        int supplyUsed, int supplyFree, int minerals, int gas,
        int strength, boolean saving) {
        return state(supplyUsed, supplyFree, minerals, gas, strength, saving, 0, 0, 0);
    }

    private static DynamicGoals.GameSnapshot state(
        int supplyUsed, int supplyFree, int minerals, int gas,
        int strength, boolean saving, int zealots, int dragoons, int lings) {
        DynamicGoals.GameSnapshot snapshot = new DynamicGoals.GameSnapshot(
            24, 1, supplyUsed, supplyFree, supplyUsed + supplyFree, minerals,
            false, 10, 1, 0, true, false, 0, gas, true, dragoons, 1, false, strength);
        snapshot.savingForDarkTemplar = saving;
        snapshot.gatewaysFree = 1;
        snapshot.zealots = zealots;
        snapshot.zerglings = lings;

        return snapshot.atBase(100, 60);
    }

    private static List<ProductionGoal> contribute(DynamicGoals.GameSnapshot snapshot) {
        return DynamicGoals.contribute(snapshot, ProductionRacePolicy.forRace("Protoss"));
    }

    private static boolean hasArmyGoal(List<ProductionGoal> goals) {
        String zealot = UnitProducible.of(AUnitType.Protoss_Zealot).id();
        String dragoon = UnitProducible.of(AUnitType.Protoss_Dragoon).id();

        return goals.stream().anyMatch(goal ->
            goal.item().id().equals(zealot) || goal.item().id().equals(dragoon));
    }

    @Test
    public void maxSupplyCapStopsBasics() {
        assertFalse(hasArmyGoal(contribute(state(188, 12, 800, 400, 500, false))),
            "at 188 the basics stop, tech and eyes flow on");
    }

    @Test
    public void tightSupplyWaitsForDragoon() {
        assertFalse(hasArmyGoal(contribute(state(184, 4, 300, 150, 300, false))),
            "184 with 4 free waits for room");
    }

    @Test
    public void richBankBuysThroughTheVeto() {
        assertTrue(hasArmyGoal(contribute(state(184, 4, 600, 150, 300, false))),
            "500+ minerals buy through any veto");
    }

    @Test
    public void savingForTemplarVetoesDragoon() {
        assertFalse(hasArmyGoal(contribute(state(60, 20, 300, 150, 200, true))),
            "warping Archives eat the 300 bank, not the next Dragoon");
    }

    @Test
    public void fundedTemplarTechReleasesDragoon() {
        assertTrue(hasArmyGoal(contribute(state(60, 20, 400, 150, 200, true))),
            "at 400 the bank covers both");
    }

    @Test
    public void gasRichLateGameBrakesZealot() {
        DynamicGoals.GameSnapshot snapshot = state(182, 10, 300, 50, 140, false);
        List<ProductionGoal> goals = contribute(snapshot);
        String zealot = UnitProducible.of(AUnitType.Protoss_Zealot).id();

        assertFalse(goals.stream().anyMatch(goal -> goal.item().id().equals(zealot)),
            "at 182 with gas banked the Zealot stops");
    }

    @Test
    public void midGameFlows() {
        assertTrue(hasArmyGoal(contribute(state(60, 20, 500, 150, 200, false))),
            "the mid game still trains");
    }

    @Test
    public void lingFloodWantsZealotsInstead() {
        List<ProductionGoal> goals = contribute(state(60, 20, 500, 150, 200, false, 2, 4, 6));
        String zealot = UnitProducible.of(AUnitType.Protoss_Zealot).id();
        String dragoon = UnitProducible.of(AUnitType.Protoss_Dragoon).id();

        assertTrue(goals.stream().anyMatch(goal -> goal.item().id().equals(zealot)),
            "4 lings vs 4 goons and 2 zealots want Zealots: " + goals);
        assertFalse(goals.stream().anyMatch(goal -> goal.item().id().equals(dragoon)),
            "not another Dragoon into the flood: " + goals);
    }

    @Test
    public void healthyRatioKeepsDragoons() {
        List<ProductionGoal> goals = contribute(state(60, 20, 500, 150, 200, false, 10, 4, 6));
        String dragoon = UnitProducible.of(AUnitType.Protoss_Dragoon).id();

        assertTrue(goals.stream().anyMatch(goal -> goal.item().id().equals(dragoon)),
            "10 zealots at ratio 2.4 keep the Dragoons coming: " + goals);
    }
}
