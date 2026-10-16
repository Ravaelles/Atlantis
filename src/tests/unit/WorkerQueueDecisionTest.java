package tests.unit;

import atlantis.production.dynamic.workers.AutoProduceWorkersCommander;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Owner 2026-10-16: the Nexus queue must overlap workers - at most one
 * waits behind the running one, and only in the closing 60-frame window.
 * The engine half (reading remaining/queue off the base) is 3 lines
 * verified live via the TRAIN_ATTEMPT twin; this pins the pure decision.
 */
public class WorkerQueueDecisionTest {

    @Test
    public void closingWindowWithoutQueuedWorkerQueues() {
        assertTrue(AutoProduceWorkersCommander.shouldQueueAnotherWorker(60, false));
        assertTrue(AutoProduceWorkersCommander.shouldQueueAnotherWorker(30, false));
        assertTrue(AutoProduceWorkersCommander.shouldQueueAnotherWorker(0, false));
    }

    @Test
    public void freshCycleNeverQueues() {
        assertFalse(AutoProduceWorkersCommander.shouldQueueAnotherWorker(61, false));
        assertFalse(AutoProduceWorkersCommander.shouldQueueAnotherWorker(200, false));
        assertFalse(
            AutoProduceWorkersCommander.shouldQueueAnotherWorker(-1, false),
            "no engine object means no train cycle - a stub -1 must not read as idle-ready");
    }

    @Test
    public void oneWaiterIsEnough() {
        assertFalse(AutoProduceWorkersCommander.shouldQueueAnotherWorker(60, true));
        assertFalse(AutoProduceWorkersCommander.shouldQueueAnotherWorker(0, true));
    }
}
