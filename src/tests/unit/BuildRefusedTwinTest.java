package tests.unit;

import atlantis.map.position.APosition;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionOrderStatus;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.constructions.PrepareConstructionSite;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.production.requests.produce.ProduceBuilding;
import atlantis.units.AUnitType;
import atlantis.util.log.EventLog;
import bwapi.Race;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.AbstractTestWithWorld;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * NEXT #148 probe: a ready building order that dies before siting must
 * name its refusing guard (20 Forge wants vanished with zero diagnostics
 * because every exit was silent). Only the stub-reachable guard fires
 * here; the rest fire only live, and the commit says so.
 */
@Tag("production")
public class BuildRefusedTwinTest extends AbstractTestWithWorld {

    private static ProductionOrder refusedOrder;

    private static int countOccurrences(String text, String needle) {
        int count = 0;
        int from = 0;
        while (true) {
            int at = text.indexOf(needle, from);
            if (at < 0) return count;

            count++;
            from = at + needle.length();
        }
    }

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
        ProduceBuilding.clearRefusedThrottleForTests();
    }

    @Test
    public void poorBankBelowMinSupplyNamesItsGuard() {
        currentMinerals = 0;
        currentSupplyUsed = 10;
        currentSupplyTotal = 18;
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 21, 21);
        world(5, fakeOurs(nexus, probe), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;

            EventLog.clearForTests();
            assertFalse(
                ProduceBuilding.requestBuilding(
                    AUnitType.Protoss_Forge, new ProductionOrder(AUnitType.Protoss_Forge, 52)),
                "below min supply on an empty bank builds nothing");
            String rows = EventLog.toJsonLines();
            assertTrue(
                rows.contains("BUILD_REFUSED") && rows.contains("below-min-supply-and-poor"),
                "the refusal names its guard, got: " + rows);
        });
    }

    /**
     * Owner 2026-10-19 (Wine: the At-22 Gateway's builder started while
     * its At-21 Pylon was still PREPARING, then the bank sat below 100
     * for the Pylon): a power-needing building with no standing Pylon
     * waits for Pylon price + own price (Forge: 100 + 150).
     */
    @Test
    public void powerHungryBuildingWaitsForPylonBank() {
        currentMinerals = 200;
        currentSupplyUsed = 22;
        currentSupplyTotal = 25;
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 21, 21);
        world(5, fakeOurs(nexus, probe), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;

            EventLog.clearForTests();
            assertFalse(
                ProduceBuilding.requestBuilding(
                    AUnitType.Protoss_Forge, new ProductionOrder(AUnitType.Protoss_Forge, 22)),
                "no Pylon standing and bank below 250 builds nothing");
            String rows = EventLog.toJsonLines();
            assertTrue(
                rows.contains("BUILD_REFUSED") && rows.contains("power-not-funded"),
                "the refusal names its guard, got: " + rows);
        });
    }

    @Test
    public void fundedPylonBankPassesThePowerGate() {
        currentMinerals = 300;
        currentSupplyUsed = 22;
        currentSupplyTotal = 25;
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 21, 21);
        world(5, fakeOurs(nexus, probe), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;

            EventLog.clearForTests();
            ProduceBuilding.requestBuilding(
                AUnitType.Protoss_Forge, new ProductionOrder(AUnitType.Protoss_Forge, 22));
            assertFalse(
                EventLog.toJsonLines().contains("power-not-funded"),
                "bank 300 covers Pylon + Forge - the power gate stays open");
        });
    }

    /**
     * NEXT #155 (B142 Gateway series): 7 prepare-site-overlap refusals at
     * one tile linked to the eventual warp only by parsing the order
     * string - the row carried no dedicated oid, and the DEMOTE row not
     * even that (cid only). Both now name the order, so the refusal
     * series, the stall demote and the warp join mechanically.
     */
    @Test
    public void prepareSiteOverlapRefusalNamesOid() {
        currentMinerals = 300;
        currentSupplyUsed = 24;
        currentSupplyTotal = 30;
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 21, 21);
        world(5, fakeOurs(nexus, probe), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;

            Construction cover = new Construction(AUnitType.Protoss_Cybernetics_Core);
            cover.setPositionToBuild(APosition.create(30, 30));
            cover.setStatus(ConstructionOrderStatus.PREPARING);
            ConstructionRequests.addConstruction(cover);

            ProductionOrder order = new ProductionOrder(AUnitType.Protoss_Gateway, 8);
            order.setAroundPosition(APosition.create(30, 30));
            order.markAsUsingExactPosition();
            EventLog.clearForTests();
            PrepareConstructionSite.prepareIfDue(order);
            String rows = EventLog.toJsonLines();
            assertTrue(
                rows.contains("prepare-site-overlap")
                    && rows.contains("\"oid\":" + order.id()),
                "the refusal names the refused order dedicately, got: " + rows);
        });
    }

    @Test
    public void refusalsThrottlePerOrder() {
        currentMinerals = 0;
        currentSupplyUsed = 10;
        currentSupplyTotal = 18;
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 21, 21);

        world(310, fakeOurs(nexus, probe), fakeEnemies(), () -> {
            int now = atlantis.game.A.now();
            if (now != 3 && now != 10 && now != 303) return;

            if (now == 3) {
                refusedOrder = new ProductionOrder(AUnitType.Protoss_Forge, 52);
            }
            ProduceBuilding.requestBuilding(AUnitType.Protoss_Forge, refusedOrder);
        });

        int rows = countOccurrences(EventLog.toJsonLines(), "below-min-supply-and-poor");
        assertEquals(
            2, rows,
            "one stuck demand names itself once per 300 frames, not once per evaluation - got: "
                + EventLog.toJsonLines());
    }
}
