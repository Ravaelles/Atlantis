package tests.unit;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.units.AUnitType;
import atlantis.units.special.SleepUntilFrameGate;
import atlantis.units.workers.IdleWorker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;

/**
 * The order-latency gate must stop the chain WITHOUT claiming ownership.
 *
 * <p>It used to return {@code usedManager(this, "SleepUntilFrame")}, which
 * overwrote {@code unit.manager()} and the history with whichever dispatcher
 * happened to run - UnitStateManager one frame, WorkerManager the next - while
 * the tooltip kept showing the leaf's stale text. Alternating dispatcher names
 * in a builder's history looked like the builder being hijacked and sent two
 * investigations (NEXT #48/#61) down the wrong path before the reading was
 * withdrawn. The in-flight order belongs to whoever issued it, so the gate
 * leaves the owner alone.</p>
 */
@Tag("production")
public class SleepGateOwnershipTest {

    @AfterEach
    public void clearStaticHold() {
        SleepUntilFrameGate.clear();
    }

    @Test
    public void sleepingUnitKeepsItsIssuingManager() {
        FakeUnit worker = fake(AUnitType.Protoss_Probe, 10);

        Manager before = worker.manager();
        SleepUntilFrameGate.holdAfterOrder(worker);

        Manager acted = new IdleWorker(worker).invokeFrom((Manager) null);

        assertTrue(acted != null, "the gate must still stop the chain");
        assertTrue(
            worker.manager() == before,
            "sleep must not overwrite the owner, was: " + worker.manager()
        );
        assertTrue(
            !worker.managerLogs().toString().contains("IdleWorker"),
            "sleep must not append dispatcher lines to the history"
        );

        SleepUntilFrameGate.clear();
        A.setNow(0, 0);
    }
}
