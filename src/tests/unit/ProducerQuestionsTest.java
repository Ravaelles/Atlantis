package tests.unit;

import atlantis.decisions.board.ADecision;
import atlantis.decisions.board.BoardState;
import atlantis.decisions.goals.protoss.ArmyQuestion;
import atlantis.decisions.goals.protoss.BuildingQuestion;
import atlantis.decisions.goals.protoss.WorkerQuestion;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * __PLAN-DECISIONS steps 5-8 (Phase A): questions answer want on a pure
 * snapshot - supply, bank, headroom. Inner gates still live in the
 * executors; these outer envelopes only ever refuse what the legacy
 * path would refuse too (unaffordable, blocked, capped).
 */
@Tag("production")
public class ProducerQuestionsTest {

    private BoardState state(int minerals, int used, int total, int workers) {
        return BoardState.builder()
            .minerals(minerals).gas(0)
            .supply(used, total)
            .workers(workers).bases(1).gateways(1).army(2)
            .core(false).assimilator(false)
            .seconds(180)
            .build();
    }

    @Test
    public void workerWantsFlowRefusesBlocks() {
        assertNotNull(new WorkerQuestion().ask(state(50, 8, 17, 8)));
        assertNull(new WorkerQuestion().ask(state(50, 9, 9, 8)),
            "supply-blocked: the engine would refuse");
        assertNull(new WorkerQuestion().ask(state(49, 8, 17, 8)),
            "unaffordable: the pump would refuse");
        assertNull(new WorkerQuestion().ask(state(500, 8, 17, 25)),
            "capped: the TooMany gate would refuse");
        assertNull(new WorkerQuestion().ask(null));
    }

    @Test
    public void armyWantsGatewayAndBank() {
        ADecision want = new ArmyQuestion().ask(state(200, 20, 25, 12));
        assertNotNull(want);
        assertEquals(ADecision.Kind.TRAIN_ARMY, want.kind());

        assertNull(new ArmyQuestion().ask(state(200, 25, 25, 12)),
            "supply-blocked");
        assertNull(new ArmyQuestion().ask(state(50, 20, 25, 12)),
            "a zealot costs 100");
    }

    @Test
    public void armyRefusesGateless() {
        BoardState gateless = BoardState.builder()
            .minerals(400).gas(0)
            .supply(15, 17)
            .workers(12).bases(1).gateways(0).army(0)
            .core(false).assimilator(false)
            .seconds(200)
            .build();

        assertNull(new ArmyQuestion().ask(gateless),
            "no gateway, no army - the producers would spin");
    }

    @Test
    public void buildingWantsBank() {
        assertNotNull(new BuildingQuestion().ask(state(400, 12, 17, 10)));
        assertNotNull(new BuildingQuestion().ask(state(50, 12, 17, 10)),
            "50 is the headroom floor: the builder walks while the last minerals are mined");
        assertNull(new BuildingQuestion().ask(state(40, 12, 17, 10)),
            "nothing starts under the headroom floor");
        assertNull(new BuildingQuestion().ask(null));
    }
}
