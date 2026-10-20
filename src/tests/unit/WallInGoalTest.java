package tests.unit;

import atlantis.decisions.board.BoardState;
import atlantis.decisions.goals.protoss.WallInGoal;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Wall-in demand lives on the board: Zerg + core up + fewer than three
 * pieces at the natural choke. Pure state matrix, milliseconds - this is
 * the test shape that replaces long games for demand logic.
 */
@Tag("production")
public class WallInGoalTest {

    private final WallInGoal goal = new WallInGoal();

    private BoardState state(String race, boolean core, int pieces) {
        return state(race, core, pieces, true);
    }

    private BoardState state(String race, boolean core, int pieces, boolean forgeExpand) {
        return BoardState.builder()
            .minerals(400).supply(30, 40)
            .workers(20).bases(2).gateways(2).army(6)
            .enemyRace(race).core(core).wallPieces(pieces)
            .forgeExpand(forgeExpand)
            .seconds(400)
            .build();
    }

    @Test
    public void wantsWallAgainstZergWithCoreAndBareChoke() {
        assertTrue(goal.active(state("Zerg", true, 0)));
        assertTrue(goal.active(state("Zerg", true, 2)));
    }

    @Test
    public void quietWhenOtherRace() {
        assertFalse(goal.active(state("Protoss", true, 0)));
        assertFalse(goal.active(state("Terran", true, 0)));
        assertFalse(goal.active(state("Unknown", true, 0)));
    }

    @Test
    public void quietWithoutCore() {
        assertFalse(goal.active(state("Zerg", false, 0)));
    }

    @Test
    public void quietWhenNotForgeExpand() {
        assertFalse(goal.active(state("Zerg", true, 0, false)),
            "owner 2026-10-18: the natural wall-in only makes sense on FE - "
                + "on any other opening the goal stays quiet");
    }

    @Test
    public void quietWhenPiecesStand() {
        assertFalse(goal.active(state("Zerg", true, 3)));
        assertFalse(goal.active(state("Zerg", true, 99)));
    }

    @Test
    public void identityRankAndPrice() {
        assertEquals("wall-in", goal.id());
        assertEquals(atlantis.decisions.goals.protoss.GoalRanks.WALL_IN, goal.rank());
        assertEquals(100, goal.priceMinerals());
        assertEquals(0, goal.priceGas());
        assertFalse(goal.emergency());
    }
}
