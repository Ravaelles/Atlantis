package tests.unit;

import atlantis.decisions.board.BoardState;
import atlantis.decisions.board.Goal;
import atlantis.decisions.board.GoalBoard;
import atlantis.decisions.goals.protoss.GoalRanks;
import atlantis.decisions.goals.protoss.ProtossOpening;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Owner 2026-10-15: continuous combat-unit production past the opening.
 * Two posture-split goals share one ladder seat: stalled Gateways with
 * no Dragoon posture want Zealots, stalled with Core and gas want
 * Dragoons. Producing, gateless, blocked or pre-Gateway states want
 * neither - the opening army goal and the legacy pumps own those.
 */
@Tag("production")
public class SustainedFlowGoalTest {

    private BoardState stalledNoCore() {
        return BoardState.builder()
            .minerals(200).gas(0).supply(14, 25)
            .workers(12).bases(1).gateways(1).gatewaysTraining(0).army(8)
            .seconds(400)
            .build();
    }

    private BoardState stalledDragoonPosture() {
        return BoardState.builder()
            .minerals(200).gas(120).supply(20, 33)
            .workers(14).bases(1).gateways(2).gatewaysTraining(0).army(8)
            .core(true)
            .seconds(500)
            .build();
    }

    private Goal byId(String id) {
        for (Goal goal : ProtossOpening.goals()) {
            if (goal.id().equals(id)) return goal;
        }
        throw new IllegalStateException("no such goal in the ladder: " + id);
    }

    @Test
    public void stalledWithoutPostureWantsZealotsNotDragoons() {
        BoardState state = stalledNoCore();
        assertTrue(byId("sustained-zealot").active(state));
        assertFalse(byId("sustained-dragoon").active(state));
    }

    @Test
    public void stalledWithCoreAndGasWantsDragoonsNotZealots() {
        BoardState state = stalledDragoonPosture();
        assertTrue(byId("sustained-dragoon").active(state));
        assertFalse(byId("sustained-zealot").active(state));
    }

    @Test
    public void producingGatewaysWantNothing() {
        BoardState state = BoardState.builder()
            .minerals(200).gas(120).supply(20, 33)
            .workers(14).bases(1).gateways(2).gatewaysTraining(2).army(8)
            .core(true)
            .seconds(500)
            .build();
        assertFalse(byId("sustained-zealot").active(state));
        assertFalse(byId("sustained-dragoon").active(state));
    }

    @Test
    public void noGatewayOrBlockedSupplyWantsNothing() {
        BoardState gateless = BoardState.builder()
            .minerals(400).gas(200).supply(9, 17)
            .workers(10).bases(1).gateways(0).army(4)
            .core(true)
            .seconds(300)
            .build();
        assertFalse(byId("sustained-zealot").active(gateless));
        assertFalse(byId("sustained-dragoon").active(gateless));

        BoardState blocked = BoardState.builder()
            .minerals(400).gas(200).supply(33, 33)
            .workers(16).bases(1).gateways(2).gatewaysTraining(0).army(10)
            .core(true)
            .seconds(500)
            .build();
        assertFalse(byId("sustained-zealot").active(blocked));
        assertFalse(byId("sustained-dragoon").active(blocked));
    }

    @Test
    public void sustainedSeatSitsBetweenArmyAndExpansion() {
        assertEquals(GoalRanks.SUSTAINED_ARMY, byId("sustained-zealot").rank());
        assertEquals(GoalRanks.SUSTAINED_ARMY, byId("sustained-dragoon").rank());
        assertTrue(GoalRanks.SUSTAINED_ARMY > GoalRanks.ARMY);
        assertTrue(GoalRanks.SUSTAINED_ARMY < GoalRanks.EXPANSION);
    }

    @Test
    public void rankingKeepsBothPosturesInLadderOrder() {
        List<Goal> ranked = GoalBoard.ranked(stalledDragoonPosture(), ProtossOpening.goals());
        boolean seenDragoon = false;
        boolean seenZealot = false;
        for (Goal goal : ranked) {
            if (goal.id().equals("sustained-dragoon")) seenDragoon = true;
            if (goal.id().equals("sustained-zealot")) seenZealot = true;
        }
        assertTrue(seenDragoon, "active posture ranks");
        assertFalse(seenZealot, "inactive posture does not rank");
    }
}
