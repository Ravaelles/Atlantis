package tests.unit;

import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.AbstractTestWithWorld;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * The queue door refuses Pylon rows above 12 free supply (owner 2026-10-19,
 * Wine 38/65 = 27 free: every author walks through this one door, so
 * per-author triggers below it cannot pile past the cap).
 */
@Tag("production")
public class PylonSpamDoorTest extends AbstractTestWithWorld {

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
        if (Queue.get() != null) {
            Queue.get().allOrders().list().clear();
            Queue.get().clearCache();
        }
    }

    @Test
    public void refusesPylonAboveTwelveFree() {
        currentMinerals = 300;
        currentSupplyUsed = 30;
        currentSupplyTotal = 43;

        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;

            Queue.get().allOrders().list().clear();
            Queue.get().clearCache();

            assertNull(
                AddToQueue.withTopPriority(AUnitType.Protoss_Pylon),
                "free 13: the door refuses, whatever the author wanted");
        });
    }

    @Test
    public void allowsPylonAtTwelveFree() {
        currentMinerals = 300;
        currentSupplyUsed = 30;
        currentSupplyTotal = 42;

        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;

            Queue.get().allOrders().list().clear();
            Queue.get().clearCache();

            assertNotNull(
                AddToQueue.withTopPriority(AUnitType.Protoss_Pylon),
                "free 12 is the cap itself - the row goes through");
        });
    }
}
