package tests.unit;

import atlantis.map.position.APosition;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionOrderStatus;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.constructions.position.conditions.OverlapsOurStandingBuilding;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.production.requests.produce.ProduceBuilding;
import atlantis.units.AUnitType;
import atlantis.util.log.EventLog;
import bwapi.Race;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * A PREPARING site a standing sibling covered since the pick must never
 * be adopted as-is: adoption walks the builder into a same-frame
 * proximity cancel (measured: Pylon adopts on [119,12] against a
 * standing Pylon, twice). Adoption re-sites instead - the resite runs
 * the spiral, which refuses covered tiles since the finder cure - or
 * refuses loudly when no tile is left.
 */
public class AdoptCoveredSiteTest extends WorldStubForTests {

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
        ProduceBuilding.clearRefusedThrottleForTests();
    }

    @Test
    public void coveredSiteIsNeverAdopted() {
        currentMinerals = 400;
        currentSupplyUsed = 9;
        currentSupplyTotal = 17;

        FakeUnit builder = fake(AUnitType.Protoss_Probe, 21, 21);

        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Pylon, 118, 16),
            builder
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;

            // [118,17] shares the y=17 row with the standing [118,16]
            // footprint: covered, adoption must not take it as-is.
            ProductionOrder order = new ProductionOrder(AUnitType.Protoss_Pylon, 8);
            Construction prepared = new Construction(AUnitType.Protoss_Pylon);
            prepared.setPositionToBuild(APosition.create(118, 17));
            prepared.setStatus(ConstructionOrderStatus.PREPARING);
            prepared.setReservedBuilder(builder);
            prepared.setProductionOrder(order);
            ConstructionRequests.constructions.add(prepared);

            boolean ok = ProduceBuilding.produceBuilding(AUnitType.Protoss_Pylon, order);

            if (ok) {
                assertEquals(
                    ConstructionOrderStatus.NOT_STARTED, prepared.status());
                assertFalse(
                    OverlapsOurStandingBuilding.check(
                        prepared.buildPosition(), AUnitType.Protoss_Pylon),
                    "adopted site must be re-sited off the cover, got: "
                        + prepared.buildPosition());
            }
            else {
                assertTrue(
                    EventLog.toJsonLines().contains("adopt-site-blocked"),
                    "a refused adopt must name itself, got: " + EventLog.toJsonLines());
            }
        });
    }
}
