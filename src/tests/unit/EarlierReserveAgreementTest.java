package tests.unit;

import atlantis.map.position.APosition;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionOrderStatus;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.constructions.builders.ShouldNotTravelToConstructYet;
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

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Pins the unified earlier-demand models (NEXT #156): the door counts
 * earlier demand by linked-order id over the construction list
 * ({@code ProduceBuilding.earlierUnfinishedMinerals}), the travel
 * forecast by creation order over the same list
 * ({@code ShouldNotTravelToConstructYet}) - and the two agree, because a
 * v2 order is stapled at dispatch, so id order and creation order march
 * together. No queue is read anywhere here; queue-position drift (re-added
 * rows) cannot desync the models anymore.
 */
@Tag("production")
public class EarlierReserveAgreementTest extends AbstractTestWithWorld {

    private static final AUnitType PYLON = AUnitType.Protoss_Pylon;

    private static Construction linkNotStartedSite(ProductionOrder order) {
        Construction construction = new Construction(PYLON);
        construction.setPositionToBuild(APosition.create(30, 30));
        construction.setStatus(ConstructionOrderStatus.NOT_STARTED);
        construction.setProductionOrder(order);
        order.setConstruction(construction);
        ConstructionRequests.constructions.add(construction);

        return construction;
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
    }

    @Test
    public void earlierConstructionCountsForLaterInBothModels() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10);
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 11);

        world(30, AbstractTestWithWorld.units(nexus, probe), new FakeUnit[0], () -> {
            if (atlantis.game.A.now() != 10) return;

            ProductionOrder earlier = new ProductionOrder(PYLON, 8);
            ProductionOrder later = new ProductionOrder(AUnitType.Protoss_Gateway, 10);
            linkNotStartedSite(earlier);
            Construction laterSite = linkNotStartedSite(later);

            int price = PYLON.mineralPrice();
            assertEquals(price, ProduceBuilding.earlierUnfinishedMinerals(later),
                "door model: the earlier id's unfinished site counts");
            assertEquals(price, ShouldNotTravelToConstructYet.earlierUnpaidForTests(laterSite)[0],
                "forecast model: the construction ahead counts the same");
        });
    }

    @Test
    public void paidWarpCountsForNeitherModel() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10);
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 11);

        world(30, AbstractTestWithWorld.units(nexus, probe), new FakeUnit[0], () -> {
            if (atlantis.game.A.now() != 10) return;

            ProductionOrder earlier = new ProductionOrder(PYLON, 8);
            ProductionOrder later = new ProductionOrder(AUnitType.Protoss_Gateway, 10);
            Construction earlierSite = linkNotStartedSite(earlier);
            FakeUnit warping = fake(PYLON, 30, 30);
            earlierSite.setBuildingUnit(warping);
            Construction laterSite = linkNotStartedSite(later);

            assertEquals(0, ProduceBuilding.earlierUnfinishedMinerals(later),
                "door model: a live warp already paid");
            assertEquals(0, ShouldNotTravelToConstructYet.earlierUnpaidForTests(laterSite)[0],
                "forecast model: a live warp already paid");
        });
    }

    @Test
    public void noEarlierConstructionCountsNothing() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10);
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 11);

        world(30, AbstractTestWithWorld.units(nexus, probe), new FakeUnit[0], () -> {
            if (atlantis.game.A.now() != 10) return;

            ProductionOrder only = new ProductionOrder(PYLON, 8);
            Construction onlySite = linkNotStartedSite(only);

            assertEquals(0, ProduceBuilding.earlierUnfinishedMinerals(only),
                "door model: nothing ahead");
            assertEquals(0, ShouldNotTravelToConstructYet.earlierUnpaidForTests(onlySite)[0],
                "forecast model: nothing ahead");
        });
    }
}
