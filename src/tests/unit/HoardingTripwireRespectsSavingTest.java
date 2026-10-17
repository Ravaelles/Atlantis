package tests.unit;

import atlantis.config.env.Env;
import atlantis.production.ForceExitLocallyInOpenBW;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.production.orders.production.queue.order.ProductionOrderPriority;
import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import tests.acceptance.AbstractTestWithWorld;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * Game 175329: the hoarding tripwire (600+ minerals) ended a healthy game
 * at 385 s with Nexus-2 warping - its 400 reserve legitimately sits inside
 * 400-600. Saving is not hoarding: an unfinished HIGH+ waiter suppresses
 * the exit. Game 20261017_143207: bank 604 at 37/49 with free supply is
 * saving/friction too - the deadlock signature is hoard PLUS blocked
 * supply, so free supply stays silent even with nothing waiting.
 * Game 20261017_143626: bank 604 at 40/41 with a Pylon warping is
 * waiting for supply, not deadlock - a coming provider suppresses too.
 */
public class HoardingTripwireRespectsSavingTest extends AbstractTestWithWorld {

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
    public void resetBackend() {
        Env.setOpenBWForTest(false);
    }

    @Test
    public void savingForNexusSuppressesTheExit() {
        currentMinerals = 650;
        currentSupplyUsed = 48;
        currentSupplyTotal = 50;
        Env.setOpenBWForTest(true);

        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;
            Queue.get().allOrders().list().clear();
            Queue.get().clearCache();

            ProductionOrder nexus = new ProductionOrder(AUnitType.Protoss_Nexus, 0);
            nexus.setPriority(ProductionOrderPriority.HIGH);
            Queue.get().addNew(0, nexus);
            Queue.get().clearCache();

            assertFalse(
                new ForceExitLocallyInOpenBW().applies(),
                "650 bank with an unfinished HIGH Nexus is saving, not hoarding (game 175329)");
        });
    }

    @Test
    public void hoardingWithNothingWaitingStillExits() {
        currentMinerals = 650;
        currentSupplyUsed = 48;
        currentSupplyTotal = 50;
        Env.setOpenBWForTest(true);

        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;
            Queue.get().allOrders().list().clear();
            Queue.get().clearCache();

            assertTrue(
                new ForceExitLocallyInOpenBW().applies(),
                "650 bank, blocked supply, nothing waiting is still the broken hoard");
        });
    }

    @Test
    public void warpingProviderSuppressesTheExit() {
        currentMinerals = 650;
        currentSupplyUsed = 40;
        currentSupplyTotal = 41;
        Env.setOpenBWForTest(true);

        FakeUnit warping = fake(AUnitType.Protoss_Pylon, 24, 24);
        warping.setCompleted(false);
        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 21, 21),
            warping
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;
            Queue.get().allOrders().list().clear();
            Queue.get().clearCache();

            assertFalse(
                new ForceExitLocallyInOpenBW().applies(),
                "650 bank at 40/41 with a Pylon warping is waiting for supply, not deadlock (game 20261017_143626)");
        });
    }

    @Test
    public void bankWithFreeSupplyStaysSilent() {        currentMinerals = 650;
        currentSupplyUsed = 30;
        currentSupplyTotal = 50;
        Env.setOpenBWForTest(true);

        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;
            Queue.get().allOrders().list().clear();
            Queue.get().clearCache();

            assertFalse(
                new ForceExitLocallyInOpenBW().applies(),
                "650 bank with 20 free supply is saving/friction, not deadlock (game 20261017_143207)");
        });
    }
}
