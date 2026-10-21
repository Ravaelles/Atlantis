package tests.unit;

import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.constructions.ConstructionOrderStatus;
import atlantis.production.dynamic.expansion.decision.ShouldExpand;
import atlantis.production.dynamic.expansion.protoss.ProtossExpandNow;
import atlantis.production.orders.production.queue.Queue;
import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.AbstractTestWithWorld;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * Bases queue at most once per 5 seconds, and only when the bank covers
 * in-flight bases plus the next one (400 each + 380). Twin symptom: two
 * "Nexus ADDED TO QUEUE" rows in the same minute while the bank cannot
 * support overlapping Warps.
 */
@Tag("production")
public class NexusQueueThrottleTest extends AbstractTestWithWorld {

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
    public void queuesBaseAtMostOncePerFiveSeconds() {
        currentMinerals = 1000;
        currentSupplyUsed = 31;
        currentSupplyTotal = 41;

        world(200, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Pylon, 24, 20),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() == 3) {
                Queue.get().allOrders().list().clear();
                Queue.get().history().clear();
                Queue.get().clearCache();
                ShouldExpand.reason = "ManyWorkers(25)";

                ProtossExpandNow.requestNewBase();
                Queue.get().clearCache();
                assertEquals(
                    1, Queue.get().notFinished().ofType(AUnitType.Protoss_Nexus).size(),
                    "first expansion request queues one Nexus");
            }

            // Frame 108 (stub-second 3, so the generic tooManyOrdersInGeneral
            // shed passes; 105 f after the first request, so the 60 f
            // just-requested guard passes too): only the 5 s base
            // throttle still holds. The reason is re-stated: live logic
            // recomputes it every frame and this test pins the queue
            // door, not the reason computer.
            if (atlantis.game.A.now() == 108) {
                ShouldExpand.reason = "ManyWorkers(25)";
                ProtossExpandNow.requestNewBase();
                Queue.get().clearCache();
                assertEquals(
                    1, Queue.get().notFinished().ofType(AUnitType.Protoss_Nexus).size(),
                    "second request inside 5 s must not queue another Nexus");
            }

            // Past the cooldown the demand flows again (cap is 2 at this bank).
            if (atlantis.game.A.now() == 180) {
                ShouldExpand.reason = "ManyWorkers(25)";
                ProtossExpandNow.requestNewBase();
                Queue.get().clearCache();
                assertEquals(
                    2, Queue.get().notFinished().ofType(AUnitType.Protoss_Nexus).size(),
                    "past 5 s the next base queues");
            }
        });
    }

    @Test
    public void refusesBaseWhenBankCannotCoverIt() {
        currentMinerals = 100;
        currentSupplyUsed = 31;
        currentSupplyTotal = 41;

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
                0, Queue.get().notFinished().ofType(AUnitType.Protoss_Nexus).size(),
                "100 bank covers no 380 floor - no Nexus queues");
        });
    }

    @Test
    public void bankMustCoverInflightBasesPlusNext() {
        currentMinerals = 700;
        currentSupplyUsed = 31;
        currentSupplyTotal = 41;

        world(200, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Pylon, 24, 20),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() == 3) {
                Queue.get().allOrders().list().clear();
                Queue.get().history().clear();
                Queue.get().clearCache();
                ShouldExpand.reason = "ManyWorkers(25)";

                // One base already warping: 700 < 1*400 + 380, hold the second.
                Construction warping = new Construction(AUnitType.Protoss_Nexus);
                warping.setStatus(ConstructionOrderStatus.NOT_STARTED);
                ConstructionRequests.constructions.add(warping);

                ProtossExpandNow.requestNewBase();
                Queue.get().clearCache();
                assertEquals(
                    0, Queue.get().notFinished().ofType(AUnitType.Protoss_Nexus).size(),
                    "700 bank cannot cover one in-flight base plus the next");
            }

            // 800 >= 780 with a fresh 5 s window: the same demand flows.
            if (atlantis.game.A.now() == 180) {
                currentMinerals = 800;
                ShouldExpand.reason = "ManyWorkers(25)";
                ProtossExpandNow.requestNewBase();
                Queue.get().clearCache();
                assertEquals(
                    1, Queue.get().notFinished().ofType(AUnitType.Protoss_Nexus).size(),
                    "800 bank covers one in-flight base plus the next");
            }
        });
    }
}
