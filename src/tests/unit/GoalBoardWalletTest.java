package tests.unit;

import atlantis.decisions.board.BoardState;
import atlantis.decisions.board.Goal;
import atlantis.decisions.board.GoalBoard;
import atlantis.decisions.board.GoalBoardWallet;
import atlantis.decisions.goals.protoss.ProtossOpening;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The wallet serves the ranked ladder top-down: the top goal and
 * emergencies always fund, the rest fund only when the remainder covers
 * them, skipped goals wait a frame. Pure like the ranking - bank arrives
 * as primitives, prices arrive on the goals.
 */
public class GoalBoardWalletTest {

    private static List<String> ids(List<Goal> goals) {
        List<String> ids = new ArrayList<String>();
        for (Goal goal : goals) ids.add(goal.id());
        return ids;
    }

    private BoardState blockedPoor() {
        return BoardState.builder()
            .minerals(61).gas(0).supply(39, 41)
            .workers(12).bases(1).gateways(1).gatewaysTraining(1).army(8)
            .seconds(400)
            .build();
    }

    @Test
    public void blockedPoorFundsOnlyTheTop() {
        BoardState state = blockedPoor();
        List<Goal> funded = funded(state, 61, 0);
        assertEquals(
            ids(GoalBoard.ranked(state, ProtossOpening.goals())).subList(0, 1),
            ids(funded),
            "a 61-mineral bank at a 39/41 block funds the supply rescue and nothing else");
        assertEquals("supply-pylon", funded.get(0).id());
    }

    private static List<Goal> funded(BoardState state, int minerals, int gas) {
        return GoalBoardWallet.funded(state, ProtossOpening.goals(), minerals, gas);
    }

    @Test
    public void richBankFundsEveryActiveGoalInLadderOrder() {
        BoardState state = BoardState.builder()
            .minerals(1000).gas(500).supply(20, 33)
            .workers(14).bases(1).gateways(1).gatewaysTraining(1).army(8)
            .core(true)
            .seconds(500)
            .build();
        assertEquals(
            ids(GoalBoard.ranked(state, ProtossOpening.goals())),
            ids(funded(state, 1000, 500)),
            "a rich bank changes nothing - every active goal funds in ladder order");
    }

    @Test
    public void emergencyFundsOnAnEmptyBank() {
        BoardState state = BoardState.builder()
            .minerals(0).gas(0).supply(14, 25)
            .workers(12).bases(1).gateways(1).gatewaysTraining(1).army(8)
            .hiddenUnits(true)
            .seconds(400)
            .build();
        List<Goal> funded = funded(state, 0, 0);
        assertTrue(
            ids(funded).contains("detection"),
            "a cloaked threat does not wait for a calm bank, funded: " + ids(funded));
        assertEquals(
            GoalBoard.ranked(state, ProtossOpening.goals()).get(0).id(),
            funded.get(0).id(),
            "the top goal still runs first, emergency rides along");
    }
}
