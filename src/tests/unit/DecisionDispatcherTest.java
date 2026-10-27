package tests.unit;

import atlantis.decisions.board.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * __PLAN-DECISIONS steps 1-2: one door for every issuance - refusals pass
 * silently, affirmatives land in the log and reach exactly one executor.
 */
@Tag("infra")
public class DecisionDispatcherTest {

    private boolean executed = false;

    @AfterEach
    public void clean() {
        DecisionLog.clearForTests();
        atlantis.production.ProductionDecisionExecutor.register();
        executed = false;
    }

    private BoardState state() {
        return BoardState.builder()
            .minerals(100).gas(0)
            .supply(9, 9)
            .workers(8).bases(1).gateways(0).army(0)
            .core(false).assimilator(false)
            .seconds(100)
            .build();
    }

    private ADecision decide() {
        return ADecision.decide(
                ADecision.Kind.TRAIN_WORKER, "Probe", "test", ADecision.Source.DYNAMIC)
            .frame(100).supply(9, 9).bank(100, 0)
            .build();
    }

    @Test
    public void refusalPassesWithoutEffect() {
        DecisionDispatcher.register(decision -> {
            executed = true;

            return true;
        });

        AQuestion nope = state -> null;

        assertFalse(DecisionDispatcher.askAndIssue(nope, state()));
        assertFalse(executed, "a refusal must not touch the executor");
    }

    @Test
    public void affirmativeReachesExactlyOneExecutor() {
        DecisionDispatcher.register(decision -> {
            executed = true;
            return "Probe".equals(decision.subject());
        });

        AQuestion yes = state -> decide();

        assertTrue(DecisionDispatcher.askAndIssue(yes, state()));
        assertTrue(executed, "one executor, exactly once");
    }

    @Test
    public void nullDecisionIsARefusal() {
        assertFalse(DecisionDispatcher.issue(null));
    }
}
