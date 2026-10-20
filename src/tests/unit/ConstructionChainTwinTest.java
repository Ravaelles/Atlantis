package tests.unit;

import atlantis.map.position.APosition;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionOrderStatus;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.constructions.cancelling.CancelConstruction;
import atlantis.production.orders.production.queue.order.OrderStatus;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnitType;
import atlantis.util.log.EventLog;
import bwapi.Race;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.AbstractTestWithWorld;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * The construction chain twin: every row on a construction's life
 * (REQUESTED / PREPARING / STARTED / CANCEL / ledger warp / ADOPTED)
 * carries the construction id (cid) and the order id (oid), so one
 * query joins the whole life of order #N without guessing by tile.
 *
 * <p>Pins the CANCEL half here (the twin fires from a live construction
 * with a linked order); the ADOPTED half is pinned in
 * {@code AdoptedConstructionTwinTest}, the ledger shape in
 * {@code BuildingLedgerTest}. REQUESTED / PREPARING / STARTED carry the
 * same two params by construction - their live proof is a game log,
 * not a stub (siting needs the map).</p>
 */
@Tag("production")
public class ConstructionChainTwinTest extends AbstractTestWithWorld {

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

    @BeforeEach
    public void clearLog() {
        EventLog.clearForTests();
    }

    @AfterEach
    public void clean() {
        ConstructionRequests.constructions.clear();
        EventLog.clearForTests();
    }

    @Test
    public void cancelNamesConstructionAndOrderIds() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10);
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 11);

        world(30, units(nexus, probe), new FakeUnit[0], () -> {
            if (atlantis.game.A.now() != 10)
                return;

            EventLog.clearForTests();
            ProductionOrder order = new ProductionOrder(AUnitType.Protoss_Gateway, 12);
            Construction construction = new Construction(AUnitType.Protoss_Gateway);
            construction.setProductionOrder(order);
            construction.setPositionToBuild(APosition.create(49, 107));
            construction.setStatus(ConstructionOrderStatus.NOT_STARTED);
            ConstructionRequests.constructions.add(construction);

            CancelConstruction.cancel(construction, "Gateway Can't build here");

            String rows = EventLog.toJsonLines();
            assertTrue(rows.contains("\"key\":\"CONSTRUCTION_CANCEL\""),
                    "the cancel must be twinned, got: " + rows);
            assertTrue(rows.contains("\"cid\":" + construction.id()),
                    "the twin must name the construction, got: " + rows);
            assertTrue(rows.contains("\"oid\":" + order.id()),
                    "the twin must name the order, got: " + rows);
        });
    }

    @Test
    public void orphanDropNamesConstructionAndOrderIds() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10);
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 11);

        world(30, units(nexus, probe), new FakeUnit[0], () -> {
            if (atlantis.game.A.now() != 10)
                return;

            EventLog.clearForTests();
            ProductionOrder order = new ProductionOrder(AUnitType.Protoss_Pylon, 15);
            Construction construction = new Construction(AUnitType.Protoss_Pylon);
            construction.setProductionOrder(order);
            construction.setPositionToBuild(APosition.create(79, 107));
            construction.setStatus(ConstructionOrderStatus.PREPARING);
            ConstructionRequests.constructions.add(construction);
            order.setStatus(OrderStatus.FINISHED);

            atlantis.production.constructions.PrepareConstructionSite.dropOrphanedPreparing();

            String dropped = EventLog.toJsonLines();
            assertTrue(dropped.contains("\"key\":\"CONSTRUCTION_CANCEL\""),
                    "the orphan drop must be twinned, got: " + dropped);
            assertTrue(dropped.contains("\"cid\":" + construction.id()),
                    "the twin must name the construction, got: " + dropped);
            assertTrue(dropped.contains("orphan-drop"),
                    "the twin must name the drop reason, got: " + dropped);
            assertTrue(!ConstructionRequests.constructions.contains(construction),
                    "the orphaned site is dropped, not pinned");
        });
    }
}
