package tests.unit;

import atlantis.production.dynamic.protoss.prioritize.ProtossCriticalStuffInQueue;
import atlantis.production.dynamic.workers.AutoProduceWorkersCommander;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.production.orders.production.queue.order.ProductionOrderPriority;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.Test;
import tests.acceptance.AbstractTestWithWorld;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * The worker pump must flow from the first minute: no gate may hold back
 * cheap workers while supply is free and the bank covers them.
 *
 * <p>
 * Measured need (owner's Wine game, last-wine.log): `CriticalStuff` from @0
 * at 50 minerals, first worker only once the bank touched 250. Cause: the
 * queue is pre-seeded with the whole build order, so a far-future not-started
 * Cybernetics Core read as "critical, save up" for the entire early game.
 * The pause now applies only when the critical item is actually up next (its
 * supply gate met) - same predicate as the PrioritizeCore rule.
 * </p>
 */
public class WorkerPumpEarlyGameTest extends AbstractTestWithWorld {

    @Override
    protected FakeUnit[] generateOur() {
        return null;
    }

    @Override
    protected FakeUnit[] generateEnemies() {
        return null;
    }

    @Test
    public void farFutureCoreDoesNotPauseWorkers() {
        currentMinerals = 100;

        world(5, fakeOurs(fake(AUnitType.Protoss_Nexus, 20, 20)), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;

            // A Core 25+ supply out is the frame-0 shape: pre-seeded by the
            // build order, not requested, not actionable.
            Queue.get().addNew(0, new ProductionOrder(AUnitType.Protoss_Cybernetics_Core, 30));

            assertTrue(
                ProtossCriticalStuffInQueue.hasEnoughMinerals(),
                "a Core that is not supply-unlocked must not pause the worker pump");
        });
    }

    @Test
    public void earlyStreamGateOpensWithFreeSupplyAndBank() {
        currentMinerals = 100;
        currentSupplyUsed = 7;
        currentSupplyTotal = 17;

        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit[] ours = {
            nexus,
            fake(AUnitType.Protoss_Probe, 21, 21),
            fake(AUnitType.Protoss_Probe, 22, 21),
            fake(AUnitType.Protoss_Probe, 21, 22),
            fake(AUnitType.Protoss_Probe, 22, 22),
        };

        world(5, fakeOurs(ours), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;

            Queue.get().addNew(0, new ProductionOrder(AUnitType.Protoss_Cybernetics_Core, 30));

            assertTrue(
                AutoProduceWorkersCommander.shouldProduceWorkers(),
                "4 probes, 100 minerals, free supply, nothing actionable queued -"
                    + " the early worker stream must not be gated, got: "
                    + AutoProduceWorkersCommander.REASON);
        });
    }

    @Test
    public void probeBuildTimeMakesTheRequeueThresholdTheLastThreePercent() {
        // queueWorker() re-queues the next worker at remainingTrainTime <= 10.
        // That is the owner's asked "<3% of the previous production time" - but
        // only if a Probe actually takes ~300 frames. Pin the engine number so
        // a data change fails loudly instead of silently moving the threshold.
        assertEquals(
            300, AUnitType.Protoss_Probe.totalTrainTime(),
            "the <=10-frame re-queue is ~3% of a 300-frame Probe");
    }

    /**
     * BO "At 8 Pylon" means BUILD it at 8 (owner ruling 2026-10-13): with no
     * Pylon at all, workers built below 150 starve the Pylon - measured as
     * bank-400-while-blocked.
     */
    @Test
    public void pylonAt8BlocksWorkersWhenPoorAndPylonless() {
        currentMinerals = 100;
        currentSupplyUsed = 8;
        currentSupplyTotal = 17;

        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 21, 21),
            fake(AUnitType.Protoss_Probe, 22, 21),
            fake(AUnitType.Protoss_Probe, 21, 22),
            fake(AUnitType.Protoss_Probe, 22, 22)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;

            assertFalse(
                AutoProduceWorkersCommander.shouldProduceWorkers(),
                "at supply 8 with no Pylon and 100 minerals the bank is saved"
                    + " for the Pylon, not spent on workers");
            assertEquals(
                "PylonAt8", AutoProduceWorkersCommander.REASON,
                "the refusing gate must name itself");
        });
    }

    @Test
    public void pylonAt8ReleasesAt150() {
        currentMinerals = 150;
        currentSupplyUsed = 8;
        currentSupplyTotal = 17;

        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 21, 21),
            fake(AUnitType.Protoss_Probe, 22, 21),
            fake(AUnitType.Protoss_Probe, 21, 22),
            fake(AUnitType.Protoss_Probe, 22, 22)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;

            assertTrue(
                AutoProduceWorkersCommander.shouldProduceWorkers(),
                "150 covers Pylon+worker - the stream flows, got: "
                    + AutoProduceWorkersCommander.REASON);
        });
    }

    @Test
    public void pylonAt8ReleasesWhenPylonWarping() {
        currentMinerals = 100;
        currentSupplyUsed = 8;
        currentSupplyTotal = 17;

        FakeUnit warpingPylon = fake(AUnitType.Protoss_Pylon, 24, 24).setCompleted(false);

        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            warpingPylon,
            fake(AUnitType.Protoss_Probe, 21, 21),
            fake(AUnitType.Protoss_Probe, 22, 21),
            fake(AUnitType.Protoss_Probe, 21, 22),
            fake(AUnitType.Protoss_Probe, 22, 22)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;

            assertTrue(
                AutoProduceWorkersCommander.shouldProduceWorkers(),
                "a started Pylon (even warping) releases the stream, got: "
                    + AutoProduceWorkersCommander.REASON);
        });
    }

    /**
     * A queued natural Nexus costs 400: without help the pump eats the bank
     * faster than it grows and the assigned builder waits at a correct site
     * forever (BuilderWaiting). Same machinery as the Core reservation.
     */
    @Test
    public void nexusQueuedPausesWorkersBelow450() {
        currentMinerals = 300;
        currentSupplyUsed = 20;
        currentSupplyTotal = 40;

        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 21, 21),
            fake(AUnitType.Protoss_Probe, 22, 21),
            fake(AUnitType.Protoss_Probe, 21, 22),
            fake(AUnitType.Protoss_Probe, 22, 22)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;

            // Direct queue insert, not AddToQueue: base orders go through
            // ShouldExpand approval there, which is global static state from
            // whatever test ran before - this test must not depend on it.
            // HIGH priority, as the expansion producer queues it (D3).
            ProductionOrder nexusOrder = new ProductionOrder(AUnitType.Protoss_Nexus, 0);
            nexusOrder.setPriority(ProductionOrderPriority.HIGH);
            Queue.get().addNew(0, nexusOrder);

            assertFalse(
                AutoProduceWorkersCommander.shouldProduceWorkers(),
                "a queued Nexus below 450 must save up, not pump workers");
            assertEquals(
                "HigherPriorityWaits", AutoProduceWorkersCommander.REASON,
                "the refusing gate must name itself");
        });
    }

    @Test
    public void nexusQueuedReleasesAt450() {
        currentMinerals = 500;
        currentSupplyUsed = 20;
        currentSupplyTotal = 40;

        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 21, 21),
            fake(AUnitType.Protoss_Probe, 22, 21),
            fake(AUnitType.Protoss_Probe, 21, 22),
            fake(AUnitType.Protoss_Probe, 22, 22)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;

            // Same direct insert: no ShouldExpand global-state dependence.
            Queue.get().addNew(0, new ProductionOrder(AUnitType.Protoss_Nexus, 0));

            assertTrue(
                AutoProduceWorkersCommander.shouldProduceWorkers(),
                "450 covers Nexus+worker - the stream flows, got: "
                    + AutoProduceWorkersCommander.REASON);
        });
    }
}
