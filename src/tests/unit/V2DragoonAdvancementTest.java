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
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pins the V2 army-advancement arithmetic (NEXT #45 race-army coverage):
 * Dragoons ASAP once the Core stands and 50 gas is banked - Zealots never
 * satisfy Dragoon demand (owner 2026-10-19, ending the 202456 freeze).
 *
 * <p>Pure snapshots, no game. Legacy keeps its own thicket (it produces
 * Dragoons in games); this test pins the V2 demand side only.</p>
 */
public class V2DragoonAdvancementTest {

    private static final String DRAGOON = UnitProducible.of(AUnitType.Protoss_Dragoon).id();
    private static final String ZEALOT = UnitProducible.of(AUnitType.Protoss_Zealot).id();

    private static DynamicGoals.GameSnapshot state(int armySize, boolean hasCore) {
        return stateGasMinerals(armySize, hasCore, 200, 500);
    }

    private static DynamicGoals.GameSnapshot stateGasMinerals(
        int armySize, boolean hasCore, int gas, int minerals) {
        return new DynamicGoals.GameSnapshot(
            25, 1, 30, 10, 40, minerals, false, armySize, 1,
            0, false, false, 0,
            gas, hasCore, 0, 1, false, 0);
    }

    private static boolean contains(List<ProductionGoal> goals, String id) {
        return goals.stream().anyMatch(goal -> goal.item().id().equals(id));
    }

    @Test
    public void advancementFiresFromEmptyArmyAfterCore() {
        List<ProductionGoal> goals =
            DynamicGoals.contribute(state(0, true), ProductionRacePolicy.forRace("Protoss"));

        assertTrue(contains(goals, DRAGOON), "empty army past the Core wants Dragoons, not Zealots");
        assertFalse(contains(goals, ZEALOT), "the baseline does not double-fill behind the advancement");
        ProductionGoal dragoon = goals.stream()
            .filter(goal -> goal.item().id().equals(DRAGOON))
            .findFirst().orElseThrow(AssertionError::new);
        assertEquals(ProductionGoal.PRIORITY_MAINARMYBASE, dragoon.priority());
        assertEquals(6, dragoon.count(), "floor is workers/4 = 25/4, all of it missing");
    }

    @Test
    public void dragoonsWantedDespiteStandingZealotsWhenGasBanked() {
        // Owner 2026-10-19: the old floor counted every army unit against
        // the transition and froze it forever (202456: 7 Zealots, 0
        // Dragoons in 17.6 min). Gas banked means Dragoons are wanted
        // regardless of the standing Zealots - Zealots never satisfy
        // Dragoon demand.
        List<ProductionGoal> goals =
            DynamicGoals.contribute(state(7, true), ProductionRacePolicy.forRace("Protoss"));

        assertTrue(contains(goals, DRAGOON), "gas banked past the Core: Dragoons, not more Zealots");
        assertFalse(contains(goals, ZEALOT), "the baseline does not double-fill behind the advancement");
        ProductionGoal dragoon = goals.stream()
            .filter(goal -> goal.item().id().equals(DRAGOON))
            .findFirst().orElseThrow(AssertionError::new);
        assertEquals(6, dragoon.count(), "floor is workers/4 = 25/4 counted against Dragoons, not Zealots");
    }

    @Test
    public void noGasMeansZealotDumpOnlyWhenRich() {
        List<ProductionGoal> rich =
            DynamicGoals.contribute(stateGasMinerals(0, true, 0, 500), ProductionRacePolicy.forRace("Protoss"));
        assertTrue(contains(rich, ZEALOT), "no gas for Dragoons but 275+ minerals: Zealots as the dump");
        assertFalse(contains(rich, DRAGOON), "no gas, no Dragoons");

        List<ProductionGoal> poor =
            DynamicGoals.contribute(stateGasMinerals(0, true, 0, 200), ProductionRacePolicy.forRace("Protoss"));
        assertFalse(contains(poor, ZEALOT), "below 275 minerals post-Core the bank is saved for Dragoons");
        assertFalse(contains(poor, DRAGOON), "no gas, no Dragoons");
    }

    @Test
    public void noCoreMeansZealotBaseline() {
        List<ProductionGoal> goals =
            DynamicGoals.contribute(state(0, false), ProductionRacePolicy.forRace("Protoss"));

        assertTrue(contains(goals, ZEALOT), "pre-Core baseline is Zealots");
        assertFalse(contains(goals, DRAGOON), "no Core, no Dragoons");
    }
}
