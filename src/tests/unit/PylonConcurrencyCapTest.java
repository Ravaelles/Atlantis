package tests.unit;

import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.production.requests.produce.ProduceBuilding;
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
 * One supply block, one Pylon at a time (two in flight max): BO rows, the
 * fallback and the supply commander each fire alone correctly and tripled
 * together in a Wine game (three Pylon warps at 3:31/3:35/3:45 out of the
 * 25/25 block, from BO#8 + its dynamic mirror + BO#11). The cap lives at
 * the single door every author passes, counting warping + not-started.
 *
 * <p>
 * Boundary: one unfinished Pylon still lets the next row through (the
 * pipeline-overlap doctrine), two hold it as a plain queued order until a
 * warp completes.
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
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 21, 21);
        world(5, fakeOurs(nexus, probe), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;

            ConstructionRequests.constructions.add(new Construction(AUnitType.Protoss_Pylon));
            assertFalse(
                ProduceBuilding.tooManyPylonsInFlight(),
                "one unfinished Pylon is the pipeline, not a pile-up");
        });
    }
}
