package tests.unit;

import atlantis.production.constructions.commanders.CancelTooLongConstructions;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.order.ProductionOrder;
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
 * Game 201239: after a legitimate 48 s cancel the requeue vetoed on four
 * Pylon rows (`notStartedInQueue=4 >= maxAllowedAtOnce=2`) - but rows at
 * 13/20/27 can never trigger while supply is frozen at 9/9, so counting
 * them deadlocks the one rebuild that could unblock supply. Only rows
 * that could convert now count.
 */
@Tag("production")
public class RequeueVetoCountsTriggerableTest extends AbstractTestWithWorld {

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
    public void untriggerableRowsDoNotVeto() {
        currentMinerals = 400;
        currentSupplyUsed = 9;
        currentSupplyTotal = 9;

        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;
            Queue.get().allOrders().list().clear();
            Queue.get().clearCache();

            Queue.get().addNew(0, new ProductionOrder(AUnitType.Protoss_Pylon, 8));
            Queue.get().addNew(1, new ProductionOrder(AUnitType.Protoss_Pylon, 13));
            Queue.get().addNew(2, new ProductionOrder(AUnitType.Protoss_Pylon, 20));
            Queue.get().addNew(3, new ProductionOrder(AUnitType.Protoss_Pylon, 27));
            Queue.get().clearCache();

            assertEquals(
                1, CancelTooLongConstructions.countTriggerableNotStarted(AUnitType.Protoss_Pylon),
                "at 9/9 only the supply-8 row could convert - the rest must not veto its rebuild");
            assertEquals(
                4, atlantis.production.orders.production.queue.CountInQueue
                    .countNotStarted(AUnitType.Protoss_Pylon),
                "sanity: the old veto saw all four rows and deadlocked");
        });
    }
}
