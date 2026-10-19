package tests.unit;

import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;
import atlantis.units.workers.WorkerHandlerCommander;
import atlantis.util.log.EventLog;
import bwapi.Race;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tests.acceptance.AbstractTestWithWorld;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * STUCK_WORKER names workers that stand still while NOT gathering
 * (owner 2026-10-19: 1637 of 2088 rows in one Wine game named healthy
 * GatherResources miners holding position while they work - the twin
 * drowned the real stalls). Steady-state gathering is exempt; idle,
 * walking-in-place and unclaimed workers still report.
 */
public class StuckWorkerTwinTest extends AbstractTestWithWorld {

    @Override
    protected FakeUnit[] generateOur() {
        return null;
    }

    @Override
    protected FakeUnit[] generateEnemies() {
        return null;
    }

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    @AfterEach
    public void clean() {
        EventLog.clearForTests();
        atlantis.config.env.Env.setBuilderLogForTest(false);
    }

    /**
     * The twin is ENV-gated (BUILDER_LOG); without the seam the suite
     * reads the machine-local file - green with the flag on, red with
     * it commented out (measured 2026-10-19: commenting it per owner
     * request flipped idleWorkerStillReports red on a clean tree).
     */
    @BeforeEach
    public void enableTwin() {
        atlantis.config.env.Env.setBuilderLogForTest(true);
    }

    @Test
    public void miningWorkerIsNotStuck() {
        FakeUnit miner = fake(AUnitType.Protoss_Probe, 30, 20);

        world(40, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            miner
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 30) return;

            // Set at report time: managers own the action during the frame,
            // the twin reads whatever it is when it fires.
            miner.setAction(Actions.GATHER_MINERALS);
            EventLog.clearForTests();
            WorkerHandlerCommander.reportStuckWorker(miner);

            assertFalse(EventLog.toJsonLines().contains("STUCK_WORKER"),
                "a miner holding position while gathering is working, not stuck");
        });
    }

    @Test
    public void idleWorkerStillReports() {
        FakeUnit idler = fake(AUnitType.Protoss_Probe, 30, 20);

        world(40, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            idler
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 30) return;

            idler.setAction(Actions.MOVE_IDLE);
            EventLog.clearForTests();
            WorkerHandlerCommander.reportStuckWorker(idler);

            assertTrue(EventLog.toJsonLines().contains("\"key\":\"STUCK_WORKER\""),
                "an idle worker standing still is exactly what the twin is for");
        });
    }
}
