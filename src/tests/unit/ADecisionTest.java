package tests.unit;

import atlantis.decisions.board.ADecision;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * __PLAN-DECISIONS step 0: decisions are engine-free values - what, why,
 * from where, on what state, with HTN children. Pinned here so the shape
 * cannot drift silently under the producers.
 */
@Tag("infra")
public class ADecisionTest {

    private ADecision worker() {
        return ADecision.decide(
                ADecision.Kind.TRAIN_WORKER, "Probe", "BO-line-2", ADecision.Source.BUILD_ORDER)
            .frame(100).supply(9, 9).bank(60, 0)
            .build();
    }

    @Test
    public void carriesWhatWhyAndState() {
        ADecision decision = worker();

        assertEquals(ADecision.Kind.TRAIN_WORKER, decision.kind());
        assertEquals("Probe", decision.subject());
        assertEquals("BO-line-2", decision.reason());
        assertEquals(ADecision.Source.BUILD_ORDER, decision.source());
        assertEquals(100, decision.frame());
        assertEquals(9, decision.supplyUsed());
        assertEquals(9, decision.supplyTotal());
        assertEquals(60, decision.minerals());
        assertTrue(decision.children().isEmpty(), "a leaf has no children");
    }

    @Test
    public void childrenComposeWithoutMutation() {
        ADecision site = ADecision.decide(
                ADecision.Kind.BUILD, "Pylon@[16,46]", "site-picked", ADecision.Source.BUILD_ORDER)
            .frame(100).supply(8, 9).bank(100, 0)
            .build();

        ADecision building = ADecision.decide(
                ADecision.Kind.BUILD, "Pylon", "BO-line-1", ADecision.Source.BUILD_ORDER)
            .frame(100).supply(8, 9).bank(100, 0)
            .build();

        ADecision composed = building.withChild(site);

        assertEquals(0, building.children().size(), "the parent is immutable");
        assertEquals(1, composed.children().size());
        assertEquals("Pylon@[16,46]", composed.children().get(0).subject());
    }

    @Test
    public void refineAndStampKeepAudit() {
        ADecision generic = ADecision.decide(
                ADecision.Kind.BUILD, "building", "build-want", ADecision.Source.DYNAMIC)
            .frame(0).supply(12, 17).bank(200, 0)
            .build();

        ADecision precise = generic
            .refine("Gateway", "At 12 Gateway (#3)", ADecision.Source.BUILD_ORDER, null)
            .atFrame(2500);

        assertEquals("Gateway", precise.subject());
        assertEquals("At 12 Gateway (#3)", precise.reason());
        assertEquals(ADecision.Source.BUILD_ORDER, precise.source());
        assertEquals(2500, precise.frame());
        assertEquals(12, precise.supplyUsed(), "snapshot survives refinement");
        assertEquals(200, precise.minerals(), "bank survives refinement");
        assertEquals(0, generic.frame(), "the generic is immutable");
    }

    @Test
    public void onlyImportantDecisionsReachTheFile() {
        assertTrue(atlantis.decisions.board.DecisionLog.wouldLog(ADecision.decide(
            ADecision.Kind.BUILD, "Pylon", "At 8 Pylon (#1)",
            ADecision.Source.BUILD_ORDER).build()));
        assertTrue(atlantis.decisions.board.DecisionLog.wouldLog(ADecision.decide(
            ADecision.Kind.RESEARCH, "Singularity Charge", "At 0 Singularity*",
            ADecision.Source.DYNAMIC).build()));
        assertFalse(atlantis.decisions.board.DecisionLog.wouldLog(ADecision.decide(
            ADecision.Kind.TRAIN_WORKER, "Probe", "pump-flow",
            ADecision.Source.DYNAMIC).build()));
        assertFalse(atlantis.decisions.board.DecisionLog.wouldLog(ADecision.decide(
            ADecision.Kind.TRAIN_ARMY, "Zealot", "army-flow",
            ADecision.Source.DYNAMIC).build()));
    }
}
