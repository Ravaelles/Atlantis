package tests.unit;

import atlantis.production.ProductionHealthWatcher;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionOrderStatus;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.units.AUnitType;
import atlantis.util.log.EventLog;
import bwapi.Race;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.AbstractTestWithWorld;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * Grep-able CRITICAL rows for the three economy diseases (owner 2026-10-19:
 * free supply above 14, 3+ Pylons in production, bank above 800 with the
 * why-breakdown). Edge-triggered: one row per episode, never per frame.
 */
@Tag("production")
public class ProductionHealthWatcherTest extends AbstractTestWithWorld {

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
        ConstructionRequests.constructions.clear();
        EventLog.clearForTests();
        ProductionHealthWatcher.clearForTests();
        currentMinerals = 0;
        currentGas = 0;
    }

    @Test
    public void healthyEconomyStaysSilent() {
        currentMinerals = 100;
        currentSupplyUsed = 30;
        currentSupplyTotal = 40;

        world(20, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 10) return;

            EventLog.clearForTests();
            ProductionHealthWatcher.checkOverstock();
            ProductionHealthWatcher.checkFlood();
            ProductionHealthWatcher.checkHoard();

            assertFalse(EventLog.toJsonLines().contains("SUPPLY_OVERSTOCK"),
                "free 10 is healthy");
            assertFalse(EventLog.toJsonLines().contains("PYLON_FLOOD"),
                "no Pylons coming is healthy");
            assertFalse(EventLog.toJsonLines().contains("BANK_HOARD"),
                "bank 100 is healthy");
        });
    }

    @Test
    public void overstockFiresOncePerEpisode() {
        currentMinerals = 100;
        currentSupplyUsed = 30;
        currentSupplyTotal = 55;

        world(20, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            int now = atlantis.game.A.now();
            if (now != 10 && now != 11) return;

            ProductionHealthWatcher.checkOverstock();

            if (now == 10) {
                assertTrue(EventLog.toJsonLines().contains("\"key\":\"SUPPLY_OVERSTOCK\""),
                    "free 25 names the overstock");
                assertTrue(EventLog.toJsonLines().contains("\"free\":25"),
                    "the twin names the free supply, got: " + EventLog.toJsonLines());
            }
            else {
                assertFalse(countRows("SUPPLY_OVERSTOCK") > 1,
                    "edge-triggered: the same episode fires once, not per frame");
            }
        });
    }

    @Test
    public void floodFiresAtThreeUnfinishedPylons() {
        currentMinerals = 100;
        currentSupplyUsed = 30;
        currentSupplyTotal = 40;

        world(20, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 10) return;

            for (int i = 0; i < 3; i++) {
                Construction c = new Construction(AUnitType.Protoss_Pylon);
                c.setStatus(ConstructionOrderStatus.NOT_STARTED);
                ConstructionRequests.constructions.add(c);
            }

            try {
                EventLog.clearForTests();
                ProductionHealthWatcher.checkFlood();

                assertTrue(EventLog.toJsonLines().contains("\"key\":\"PYLON_FLOOD\""),
                    "3 unfinished Pylons is the flood");
                assertTrue(EventLog.toJsonLines().contains("\"coming\":3"),
                    "the twin names the count");
            } finally {
                ConstructionRequests.constructions.clear();
            }
        });
    }

    @Test
    public void hoardNamesWhy() {
        currentMinerals = 900;
        currentGas = 100;
        currentSupplyUsed = 40;
        currentSupplyTotal = 41;

        world(20, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 10) return;

            EventLog.clearForTests();
            ProductionHealthWatcher.checkHoard();

            String rows = EventLog.toJsonLines();
            assertTrue(rows.contains("\"key\":\"BANK_HOARD\""),
                "bank 900 names the hoard, got: " + rows);
            assertTrue(rows.contains("\"reserved\""),
                "the twin names reserved minerals (demand waiting on the queue)");
            assertTrue(rows.contains("\"workerGate\""),
                "the twin names the worker pump's refusing gate");
        });
    }

    private int countRows(String key) {
        String rows = EventLog.toJsonLines();
        int count = 0;
        int at = 0;
        while ((at = rows.indexOf("\"key\":\"" + key + "\"", at)) >= 0) {
            count++;
            at++;
        }
        return count;
    }
}
