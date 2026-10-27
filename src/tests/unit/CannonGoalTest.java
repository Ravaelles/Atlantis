package tests.unit;

import atlantis.decisions.board.BoardState;
import atlantis.decisions.goals.protoss.CannonGoal;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Cannon demand lives on the board: the count matrix moved here row for
 * row from the legacy producer, so tuning verifies in milliseconds.
 * Each wanted() case mirrors one legacy branch (tiers, bonus, caps).
 */
@Tag("production")
public class CannonGoalTest {

    private final CannonGoal goal = new CannonGoal();

    private BoardState state(int supplyUsed, int supplyTotal, int minerals, String race, int mutas, int cannons) {
        return BoardState.builder()
            .minerals(minerals).supply(supplyUsed, supplyTotal)
            .workers(20).bases(2).gateways(3).army(8)
            .enemyRace(race).enemyUnits(0, 0, mutas, false).cannons(cannons)
            .seconds(500)
            .build();
    }

    @Test
    public void earlyMirrorWantsOne() {
        assertEquals(1, CannonGoal.wanted(state(20, 20, 200, "Protoss", 0, 0)));
    }

    @Test
    public void midZergRichWantsFour() {
        assertEquals(4, CannonGoal.wanted(state(140, 140, 600, "Zerg", 0, 0)));
    }

    @Test
    public void mutasPushToTheZergCap() {
        assertEquals(6, CannonGoal.wanted(state(150, 150, 1000, "Zerg", 8, 0)));
    }

    @Test
    public void terranCapsAtTwo() {
        assertEquals(2, CannonGoal.wanted(state(200, 200, 900, "Terran", 0, 0)));
    }

    @Test
    public void poorEarlyZergHoldsAtTwo() {
        assertEquals(2, CannonGoal.wanted(state(100, 100, 300, "Zerg", 0, 0)));
    }

    @Test
    public void activeWhileShort() {
        assertTrue(goal.active(state(140, 140, 600, "Zerg", 0, 2)));
        assertFalse(goal.active(state(140, 140, 600, "Zerg", 0, 4)));
    }

    @Test
    public void identityRankAndPrice() {
        assertEquals("cannon", goal.id());
        assertEquals(atlantis.decisions.goals.protoss.GoalRanks.CANNON, goal.rank());
        assertEquals(150, goal.priceMinerals());
        assertEquals(0, goal.priceGas());
        assertFalse(goal.emergency());
    }
}
