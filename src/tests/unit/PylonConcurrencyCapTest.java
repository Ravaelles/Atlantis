package tests.unit;

import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.production.requests.produce.ProduceBuilding;
import atlantis.units.AUnitType;
import atlantis.util.log.EventLog;
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
 * Below 40 supply used, one Pylon in flight at a time (owner: 10+ free
 * supply above 25 from overlapping BO + dynamic Pylons; the goal is
 * never more than 8 free there). At 40+ the pipeline pair stays.
 * The cap lives at the single door every author passes, counting
 * warping + not-started.
 *
 * <p>
 * Boundary: below 40, one unfinished Pylon already holds the next row
 * as a plain queued order until a warp completes; at 40+ one unfinished
 * Pylon is the pipeline, two hold it.
 * </p>
 */
public class PylonConcurrencyCapTest extends AbstractTestWithWorld {

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
    public void cleanConstructions() {
        ConstructionRequests.constructions.clear();
    }

    @Test
    public void thirdConcurrentPylonRefused() {
        currentMinerals = 500;
        currentSupplyUsed = 25;
        currentSupplyTotal = 25;
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 21, 21);
        world(5, fakeOurs(nexus, probe), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;

            ConstructionRequests.constructions.add(new Construction(AUnitType.Protoss_Pylon));
            ConstructionRequests.constructions.add(new Construction(AUnitType.Protoss_Pylon));
            assertTrue(ProduceBuilding.tooManyPylonsInFlight());

            assertFalse(
                ProduceBuilding.requestBuilding(
                    AUnitType.Protoss_Pylon, new ProductionOrder(AUnitType.Protoss_Pylon, 27)),
                "two unfinished Pylons already cover the block - BO#11 waits");
        });
    }

    @Test
    public void secondConcurrentPylonAllowed() {
        currentMinerals = 500;
        currentSupplyUsed = 41;
        currentSupplyTotal = 41;
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 21, 21);
        world(5, fakeOurs(nexus, probe), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;

            ConstructionRequests.constructions.add(new Construction(AUnitType.Protoss_Pylon));
            assertFalse(
                ProduceBuilding.tooManyPylonsInFlight(),
                "at 40+ one unfinished Pylon is the pipeline, not a pile-up");
        });
    }

    @Test
    public void overstockedFreeSupplyHoldsPylonBelowForty() {
        // BO rows bypass the dynamic demand veto, so the door enforces
        // the same ceiling for every author: At-29 fired at 25/33 into
        // 12 free (UMS 7th_rav). Below 40, free above 8 waits.
        currentMinerals = 300;
        currentSupplyUsed = 29;
        currentSupplyTotal = 41;
        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;

            assertFalse(
                ProduceBuilding.requestBuilding(
                    AUnitType.Protoss_Pylon, new ProductionOrder(AUnitType.Protoss_Pylon, 29)),
                "12 free below 40 is overstock - the row waits");
            assertTrue(
                EventLog.toJsonLines().contains("pylon-overstock"),
                "the overstock names itself: " + EventLog.toJsonLines());
        });
    }

    @Test
    public void secondConcurrentPylonBlockedBelowForty() {
        currentMinerals = 500;
        currentSupplyUsed = 25;
        currentSupplyTotal = 25;
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 21, 21);
        world(5, fakeOurs(nexus, probe), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;

            ConstructionRequests.constructions.add(new Construction(AUnitType.Protoss_Pylon));
            assertTrue(
                ProduceBuilding.tooManyPylonsInFlight(),
                "below 40 one unfinished Pylon already covers the need");

            assertFalse(
                ProduceBuilding.requestBuilding(
                    AUnitType.Protoss_Pylon, new ProductionOrder(AUnitType.Protoss_Pylon, 27)),
                "below 40 a second Pylon waits for the warp instead of stockpiling free supply");
        });
    }
}
