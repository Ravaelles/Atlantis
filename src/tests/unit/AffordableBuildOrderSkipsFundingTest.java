package tests.unit;

import atlantis.decisions.board.BoardState;
import atlantis.map.position.APosition;
import atlantis.production.GoalBoardFeeder;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionOrderStatus;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.orders.production.queue.Queue;
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
 * Queue order outranks wallet funding (owner): a READY build-order row
 * that covers its own price proceeds even when the wallet funds
 * something else (measured UMS 7th_rav: BO Core #5 PREPARING from 2:00,
 * bank 250-370, unfunded behind a dynamic Gateway while the queue order
 * sat). Dynamic demand still needs funding; poverty protection below
 * the own price is untouched (aim-while-poor still decides there).
 */
public class AffordableBuildOrderSkipsFundingTest extends AbstractTestWithWorld {

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
    public void affordableBuildOrderSkipsUnfundedWallet() {
        FakeUnit builder = fake(AUnitType.Protoss_Probe, 21, 21);
        currentSupplyUsed = 15;
        currentSupplyTotal = 17;
        currentMinerals = 80;
        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            builder
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;
            Queue.get().allOrders().list().clear();
            Queue.get().clearCache();

            // Funded set WITHOUT gateway (bank 80 covers nothing past
            // the top-ranked supply): adoption would refuse on funding
            // from here on.
            BoardState poor = BoardState.builder()
                .minerals(80).supply(15, 17)
                .workers(10).bases(1).gateways(1).army(4)
                .seconds(120)
                .build();
            GoalBoardFeeder.ensure(poor);
            assertFalse(
                GoalBoardFeeder.adoptionAllowed(AUnitType.Protoss_Gateway),
                "setup: the wallet does not fund the Gateway on 80 bank");

            // Bank recovers without a re-ranking: the affordable BO row
            // must not wait for the next funding.
            currentMinerals = 400;
            ProductionOrder order = new ProductionOrder(AUnitType.Protoss_Gateway, 10);
            order.markAsNotDynamic();
            Construction preparing = new Construction(AUnitType.Protoss_Gateway);
            preparing.setPositionToBuild(APosition.create(100, 100));
            preparing.setStatus(ConstructionOrderStatus.PREPARING);
            preparing.setReservedBuilder(builder);
            preparing.setProductionOrder(order);
            ConstructionRequests.constructions.add(preparing);

            EventLog.clearForTests();
            assertTrue(
                ProduceBuilding.produceBuilding(AUnitType.Protoss_Gateway, order),
                "affordable queue order skips an unfunded wallet: " + EventLog.toJsonLines());
            assertFalse(
                EventLog.toJsonLines().contains("\"guard\":\"funding\""),
                "no funding veto on an affordable BO row: " + EventLog.toJsonLines());
        });
    }
}
