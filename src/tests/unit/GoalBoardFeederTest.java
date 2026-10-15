package tests.unit;

import atlantis.decisions.board.BoardState;
import atlantis.production.GoalBoardFeeder;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.production.orders.production.queue.order.ProductionOrderPriority;
import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.Test;
import tests.acceptance.AbstractTestWithWorld;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * NEXT #91 feeder (board -> queue): active goals nobody owns get exactly
 * one order, then silence. Queue rows don't spend (readiness, builders
 * and the engine still decide), so feeding is safe by construction.
 * Workers/army/expansion/detection stay out (pump, strategy, site
 * approval and response system own them).
 */
public class GoalBoardFeederTest extends AbstractTestWithWorld {

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

    private BoardState blockedState() {
        return BoardState.builder()
            .minerals(150).supply(9, 9)
            .workers(6).bases(1).gateways(1).army(2)
            .seconds(120)
            .build();
    }

    @Test
    public void blockedSupplyFeedsTopPylon() {
        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;
            Queue.get().allOrders().list().clear();
            Queue.get().clearCache();

            String fed = GoalBoardFeeder.ensure(blockedState());

            assertTrue(fed.contains("supply-pylon"), "fed ids name the goal, got: " + fed);
            boolean topPylon = false;
            for (ProductionOrder order : Queue.get().allOrders().list()) {
                if (order.unitType() != null && order.unitType().equals(AUnitType.Protoss_Pylon)
                    && order.priority() == ProductionOrderPriority.TOP) {
                    topPylon = true;
                }
            }
            assertTrue(topPylon, "a blocked supply feeds one TOP Pylon row");
        });
    }

    @Test
    public void coveredGoalIsSilent() {
        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;
            Queue.get().allOrders().list().clear();
            Queue.get().clearCache();

            Queue.get().addNew(0, new ProductionOrder(AUnitType.Protoss_Pylon, 8));
            Queue.get().clearCache();
            int before = Queue.get().allOrders().list().size();

            GoalBoardFeeder.ensure(blockedState());

            assertEquals(before, Queue.get().allOrders().list().size(),
                "covered supply-pylon adds nothing");
        });
    }

    @Test
    public void gatewayFedWorkersArmyExpansionSkipped() {
        BoardState state = BoardState.builder()
            .minerals(300).supply(10, 17)
            .workers(6).bases(1).gateways(0).army(2)
            .seconds(200)
            .build();

        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Pylon, 22, 20),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;
            Queue.get().allOrders().list().clear();
            Queue.get().clearCache();

            GoalBoardFeeder.ensure(state);

            int gateways = 0;
            int others = 0;
            for (ProductionOrder order : Queue.get().allOrders().list()) {
                if (order.unitType() == null) {
                    continue;
                }
                if (order.unitType().equals(AUnitType.Protoss_Gateway)) {
                    gateways++;
                    assertEquals(ProductionOrderPriority.STANDARD, order.priority());
                }
                else if (order.unitType().equals(AUnitType.Protoss_Probe)
                    || order.unitType().equals(AUnitType.Protoss_Zealot)
                    || order.unitType().equals(AUnitType.Protoss_Nexus)
                    || order.unitType().equals(AUnitType.Protoss_Forge)) {
                    others++;
                }
            }
            assertEquals(1, gateways, "exactly one STANDARD Gateway row");
            assertEquals(0, others, "workers/army/expansion/detection stay with their owners");
        });
    }
}
