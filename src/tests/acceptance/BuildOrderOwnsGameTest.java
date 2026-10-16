package tests.acceptance;

import atlantis.production.orders.build.BuildOrderOwnsGame;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * Owner 2026-10-18 (__PLAN-DECISIONS steps 3-4): the memorized opening
 * owns the game while its rows are due - dynamic producers stay off and
 * the script (explicit worker lines included) drives alone. Only
 * build-order rows own, only within reach, only below supply 18.
 */
public class BuildOrderOwnsGameTest extends WorldStubForTests {

    private boolean fired = false;

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    @AfterEach
    public void clean() {
        Queue.get().allOrders().list().clear();
        Queue.get().clearCache();
        fired = false;
    }

    private void seedBuildOrderRow(AUnitType type, int minSupply) {
        ProductionOrder row = new ProductionOrder(type, minSupply);
        row.markAsNotDynamic();
        Queue.get().addNew(0, row);
        Queue.get().clearCache();
    }

    @Test
    public void dueBuildOrderRowOwns() {
        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3 || fired) return;
            fired = true;
            Queue.get().allOrders().list().clear();
            Queue.get().clearCache();

            currentSupplyUsed = 7;
            currentSupplyTotal = 9;
            seedBuildOrderRow(AUnitType.Protoss_Pylon, 8);

            assertTrue(
                BuildOrderOwnsGame.owns(),
                "Pylon@8 due at supply 7 - the script owns, pumps stay off");
        });

        assertTrue(fired, "the frame was reached - else this test proves nothing");
    }

    @Test
    public void dynamicRowsNeverOwn() {
        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3 || fired) return;
            fired = true;
            Queue.get().allOrders().list().clear();
            Queue.get().clearCache();

            currentSupplyUsed = 7;
            currentSupplyTotal = 9;
            Queue.get().addNew(0, new ProductionOrder(AUnitType.Protoss_Gateway, 8));
            Queue.get().clearCache();

            assertFalse(
                BuildOrderOwnsGame.owns(),
                "a dynamic row, however due, never owns the game");
        });

        assertTrue(fired, "the frame was reached - else this test proves nothing");
    }

    @Test
    public void farFutureRowsDoNotOwn() {
        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3 || fired) return;
            fired = true;
            Queue.get().allOrders().list().clear();
            Queue.get().clearCache();

            currentSupplyUsed = 12;
            currentSupplyTotal = 17;
            seedBuildOrderRow(AUnitType.Protoss_Cybernetics_Core, 22);

            assertFalse(
                BuildOrderOwnsGame.owns(),
                "Core@22 at supply 12 is a far chapter - improvisation runs");
        });

        assertTrue(fired, "the frame was reached - else this test proves nothing");
    }

    @Test
    public void dynamicCommanderStandsDownWhileOwned() {
        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3 || fired) return;
            fired = true;
            Queue.get().allOrders().list().clear();
            Queue.get().clearCache();

            currentSupplyUsed = 7;
            currentSupplyTotal = 9;
            seedBuildOrderRow(AUnitType.Protoss_Pylon, 8);

            assertFalse(
                new atlantis.production.dynamic.DynamicProductionCommander().applies(),
                "owned opening: the dynamic commander does not run at all");
        });

        assertTrue(fired, "the frame was reached - else this test proves nothing");
    }

    @Test
    public void openingEndsAtEighteen() {
        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3 || fired) return;
            fired = true;
            Queue.get().allOrders().list().clear();
            Queue.get().clearCache();

            currentSupplyUsed = 19;
            currentSupplyTotal = 25;
            seedBuildOrderRow(AUnitType.Protoss_Cybernetics_Core, 22);

            assertFalse(
                BuildOrderOwnsGame.owns(),
                "past supply 18 a stuck row must not freeze the mid-game");
        });

        assertTrue(fired, "the frame was reached - else this test proves nothing");
    }

    @Override
    protected FakeUnit[] generateOur() {
        return null;
    }

    @Override
    protected FakeUnit[] generateEnemies() {
        return null;
    }
}
