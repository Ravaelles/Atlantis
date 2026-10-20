package tests.unit;

import atlantis.decisions.board.BoardState;
import atlantis.decisions.goals.protoss.ForgeGoal;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Forge timing lives on the board: DT prediction plus matchup supply
 * table, each legacy branch mirrored below. Stub states, milliseconds.
 */
@Tag("production")
public class ForgeGoalTest {

    private BoardState state(
        int supply, int combat, int strength, int strengthCB, int gatewaysUnfinished,
        int minerals, String race, boolean hidden, boolean goingHidden, boolean ranged, boolean forge
    ) {
        return BoardState.builder()
            .minerals(minerals).supply(supply, supply + 10)
            .workers(20).bases(2).gateways(2).army(combat)
            .armyStrength(strength).armyStrengthWithoutCB(strengthCB)
            .gatewaysUnfinished(gatewaysUnfinished)
            .enemyRace(race).hiddenUnits(hidden).enemyGoingHidden(goingHidden).enemyHasRanged(ranged)
            .forge(forge)
            .seconds(400)
            .build();
    }

    @Test
    public void seenHiddenUnitsWantForgeAtForty() {
        assertTrue(ForgeGoal.wanted(state(50, 5, 100, 100, 0, 200, "Protoss", true, false, false, false)));
        assertFalse(ForgeGoal.wanted(state(20, 5, 100, 100, 0, 200, "Protoss", true, false, false, false)));
    }

    @Test
    public void goingHiddenWithArmyWantsForge() {
        assertTrue(ForgeGoal.wanted(state(50, 10, 200, 200, 4, 300, "Protoss", false, true, false, false)));
        assertFalse(ForgeGoal.wanted(state(50, 5, 200, 200, 4, 300, "Protoss", false, true, false, false)));
        assertFalse(ForgeGoal.wanted(state(50, 10, 200, 100, 4, 300, "Protoss", false, true, false, false)));
    }

    @Test
    public void smallEarlyArmyWaits() {
        assertFalse(ForgeGoal.wanted(state(50, 5, 100, 100, 0, 300, "Zerg", false, false, false, false)));
        // Past 60 the supply gate opens even for a small army (legacy row).
        assertTrue(ForgeGoal.wanted(state(70, 5, 100, 100, 0, 300, "Zerg", false, false, false, false)));
    }

    @Test
    public void zergTimingAtFortySix() {
        assertTrue(ForgeGoal.wanted(state(50, 15, 200, 200, 0, 300, "Zerg", false, false, false, false)));
        assertFalse(ForgeGoal.wanted(state(40, 15, 200, 200, 0, 300, "Zerg", false, false, false, false)));
    }

    @Test
    public void terranTimingAtNinetyThree() {
        assertTrue(ForgeGoal.wanted(state(95, 15, 200, 200, 0, 300, "Terran", false, false, true, false)));
        assertFalse(ForgeGoal.wanted(state(90, 15, 200, 200, 0, 300, "Terran", false, false, true, false)));
    }

    @Test
    public void protossMirrorWithoutRangedAtSeventyThree() {
        assertTrue(ForgeGoal.wanted(state(75, 15, 200, 200, 0, 300, "Protoss", false, false, false, false)));
        assertFalse(ForgeGoal.wanted(state(70, 15, 200, 200, 0, 300, "Protoss", false, false, false, false)));
    }

    @Test
    public void activeNeedsNoForge() {
        ForgeGoal goal = new ForgeGoal();
        assertTrue(goal.active(state(75, 15, 200, 200, 0, 300, "Protoss", false, false, false, false)));
        assertFalse(goal.active(state(75, 15, 200, 200, 0, 300, "Protoss", false, false, false, true)));
    }

    @Test
    public void identityRankAndPrice() {
        ForgeGoal goal = new ForgeGoal();
        assertEquals("forge", goal.id());
        assertEquals(atlantis.decisions.goals.protoss.GoalRanks.FORGE, goal.rank());
        assertEquals(150, goal.priceMinerals());
        assertEquals(0, goal.priceGas());
        assertFalse(goal.emergency());
    }
}
