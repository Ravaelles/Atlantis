package tests.unit;

import atlantis.decisions.board.BoardState;
import atlantis.decisions.board.Goal;
import atlantis.decisions.board.GoalBoard;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * NEXT #91: arbitration proves in milliseconds against stub states, never
 * in OpenBW games. Each case is one game situation and its expected board
 * top - the exact questions full games used to cost us.
 */
public class GoalBoardTest {

    private static BoardState base() {
        return BoardState.builder()
            .minerals(200).gas(0)
            .supply(10, 17)
            .workers(8).bases(1).gateways(1).army(2)
            .core(false).assimilator(false)
            .seconds(180)
            .build();
    }

    @Test
    public void supplyBlockTops() {
        BoardState state = BoardState.builder()
            .minerals(150).supply(9, 9)
            .workers(6).bases(1).gateways(1).army(2)
            .build();

        assertEquals("supply-pylon", GoalBoard.top(state, GoalBoard.openingGoals(), null).id());
    }

    @Test
    public void earlyEconomyTopsWorkers() {
        assertEquals("workers", GoalBoard.top(base(), GoalBoard.openingGoals(), null).id());
    }

    @Test
    public void detectionPreemptsEconomy() {
        BoardState state = BoardState.builder()
            .minerals(100).supply(10, 17)
            .workers(4).bases(1).gateways(1).army(2)
            .hiddenUnits(true)
            .build();

        assertEquals("detection", GoalBoard.top(state, GoalBoard.openingGoals(), null).id());
    }

    @Test
    public void richLadderOrderHolds() {
        BoardState state = BoardState.builder()
            .minerals(400).gas(100).supply(20, 25)
            .workers(12).bases(1).gateways(1).army(3)
            .core(true).assimilator(false)
            .seconds(300)
            .build();

        List<Goal> ranked = GoalBoard.ranked(state, GoalBoard.openingGoals());
        StringBuilder ids = new StringBuilder();
        for (Goal goal : ranked) {
            if (ids.length() > 0) ids.append(",");
            ids.append(goal.id());
        }
        assertEquals(
            "workers,gateway,assimilator,army,expansion", ids.toString(),
            "supply satisfied, core standing, no threats: economy, capacity, gas, army, base");
    }

    @Test
    public void commitmentHoldsInFlightGoal() {
        assertEquals(
            "gateway",
            GoalBoard.top(base(), GoalBoard.openingGoals(), Collections.singleton("gateway")).id(),
            "started gateway keeps its place while workers also want work");
    }

    @Test
    public void emergencyBreaksCommitment() {
        BoardState state = BoardState.builder()
            .minerals(100).supply(10, 17)
            .workers(4).bases(1).gateways(1).army(2)
            .hiddenUnits(true)
            .build();

        assertEquals(
            "detection",
            GoalBoard.top(state, GoalBoard.openingGoals(), Collections.singleton("workers")).id(),
            "a cloaked wraith does not wait for a calm plan");
    }

    @Test
    public void nothingActiveTopsNull() {
        BoardState state = BoardState.builder()
            .minerals(2000).gas(1000).supply(100, 200)
            .workers(70).bases(3).gateways(5).army(30)
            .core(true).assimilator(true).forge(true).cannons(2).observers(1)
            .seconds(900)
            .build();

        assertTrue(GoalBoard.ranked(state, GoalBoard.openingGoals()).isEmpty());
        assertNull(GoalBoard.top(state, GoalBoard.openingGoals(), null));
    }
}
