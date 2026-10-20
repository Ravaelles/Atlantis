package tests.unit;

import atlantis.production.constructions.ConstructionOrderStatus;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static atlantis.production.constructions.ConstructionOrderStatus.FINISHED;
import static atlantis.production.constructions.ConstructionOrderStatus.IN_PROGRESS;
import static atlantis.production.constructions.ConstructionOrderStatus.NOT_STARTED;
import static atlantis.production.constructions.ConstructionOrderStatus.PREPARING;
import static atlantis.production.constructions.ConstructionOrderStatus.STARTING;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pins the semantic queries on {@link ConstructionOrderStatus} (NEXT #157):
 * waiting vs live demand vs terminal is a property of the status itself,
 * not a disjunction each reader re-spells. Additive only - no reader was
 * touched, so every assertion here must hold against the pre-existing
 * call sites (the {@code isNotStarted} family, the overdue/cancel skips,
 * the pylon door).
 */
@Tag("production")
public class ConstructionOrderStatusTest {

    @Test
    public void preparingWaitsByDesignAndCountsAsNoDemand() {
        assertTrue(PREPARING.isWaiting(),
                "PREPARING is the aimed future: site picked, no builder, waiting by design");
        assertFalse(PREPARING.countsAsDemand(),
                "a waiting site must not suppress other orders (pylon door parks otherwise)");
        assertFalse(PREPARING.isTerminal(), "waiting sites stay on the list");
    }

    @Test
    public void notStartedAndStartingAreLiveDemand() {
        assertFalse(NOT_STARTED.isWaiting(), "accepted demand is not waiting");
        assertTrue(NOT_STARTED.countsAsDemand(), "accepted demand suppresses re-requests");
        assertFalse(NOT_STARTED.isTerminal(), "accepted demand stays on the list");

        assertFalse(STARTING.isWaiting(), "a travelling builder is not waiting");
        assertTrue(STARTING.countsAsDemand(),
                "a travelling builder is committed demand, never mistaken for none");
        assertFalse(STARTING.isTerminal(), "travelling builders stay on the list");
    }

    @Test
    public void inProgressIsWorkingNeitherWaitingNorDemandNorTerminal() {
        assertFalse(IN_PROGRESS.isWaiting(), "work in progress is not waiting");
        assertFalse(IN_PROGRESS.countsAsDemand(),
                "a warping building is arriving supply; the echo-without-unit case stays a door-level rule");
        assertFalse(IN_PROGRESS.isTerminal(), "work in progress stays on the list");
    }

    @Test
    public void finishedIsTheOnlyTerminalState() {
        assertFalse(FINISHED.isWaiting(), "terminal states wait for nothing");
        assertFalse(FINISHED.countsAsDemand(), "terminal states demand nothing");
        assertTrue(FINISHED.isTerminal(), "FINISHED is removed from the list on entry");

        for (ConstructionOrderStatus status : ConstructionOrderStatus.values()) {
            if (status != FINISHED) {
                assertFalse(status.isTerminal(),
                        status + " must stay on the list; no tombstone states exist");
            }
        }
    }
}
