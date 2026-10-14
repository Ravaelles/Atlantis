package tests.unit;

import atlantis.production.dynamic.expansion.decision.ShouldExpand;
import atlantis.production.dynamic.expansion.protoss.ProtossExpandNow;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.order.OrderStatus;
import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.Test;
import tests.acceptance.AbstractTestWithWorld;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * An expansion order being worked on must count toward the expansion cap.
 *
 * <p>NEXT #58: {@code AddToQueue.maxAtATime} counts {@code nextOrders()},
 * which skips IN_PROGRESS - so the expansion producer added a second Nexus
 * 9 s after the first (game 20261014_121406: ADDED 6:36 + 6:45, the first
 * IN_PROGRESS throughout, zero cancels). Bases take minutes; an unfinished
 * one always counts. Fails pre-fix with 2 queued Nexuses.</p>
 *
 * <p>The queue and the request history are cleared first: live commanders
 * fill the stub-world queue during setup frames, and the just-requested
 * throttle would otherwise decide the outcome instead of the cap. The
 * expansion reason is set explicitly - an unset reason refuses every Nexus
 * before any counting happens.</p>
 */
public class ExpansionNoSecondBaseWhileOnePendingTest extends AbstractTestWithWorld {

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

    @Test
    public void inProgressNexusCountsTowardExpansionCap() {
        currentMinerals = 556;
        currentSupplyUsed = 53;
        currentSupplyTotal = 65;

        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Pylon, 24, 20),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;

            Queue.get().allOrders().list().clear();
            Queue.get().history().clear();
            Queue.get().clearCache();
            ShouldExpand.reason = "ManyWorkers(25)";

            ProtossExpandNow.requestNewBase();
            Queue.get().clearCache();
            assertEquals(
                1, Queue.get().notFinished().ofType(AUnitType.Protoss_Nexus).size(),
                "first expansion request must queue one Nexus");

            Queue.get().notFinished().ofType(AUnitType.Protoss_Nexus).first()
                .setStatus(OrderStatus.IN_PROGRESS);
            Queue.get().history().clear();
            Queue.get().clearCache();

            ProtossExpandNow.requestNewBase();
            Queue.get().clearCache();
            assertEquals(
                1, Queue.get().notFinished().ofType(AUnitType.Protoss_Nexus).size(),
                "an IN_PROGRESS Nexus must satisfy the cap - no sibling order");
        });
    }
}
