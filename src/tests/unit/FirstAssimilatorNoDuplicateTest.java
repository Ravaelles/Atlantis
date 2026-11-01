package tests.unit;

import atlantis.decisions.board.BoardState;
import atlantis.production.goalboard.GoalBoardFeeder;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.order.OrderStatus;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.AbstractTestWithWorld;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Game 174710: two concurrent Assimilator waiters for one geyser - the
 * build-order row (supply 22) sat outside the next-2 cap the dynamic path
 * checks, so it added a second at 4:04.
 * B1: the V2 feeder owns the dedup now - a progressing Assimilator row
 * counts as in-flight, so a wanted Assimilator goal feeds no second row.
 */
@Tag("production")
public class FirstAssimilatorNoDuplicateTest extends AbstractTestWithWorld {

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

    @Test
    public void progressingRowBlocksDynamicAdd() {
        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Cybernetics_Core, 24, 20),
            fake(AUnitType.Protoss_Pylon, 26, 20),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;

            Queue.get().allOrders().list().clear();
            Queue.get().clearCache();

            // A progressing Assimilator row already in flight...
            ProductionOrder row = new ProductionOrder(AUnitType.Protoss_Assimilator, 22);
            row.setStatus(OrderStatus.IN_PROGRESS);
            Queue.get().addNew(0, row);
            Queue.get().clearCache();

            // ...while the goal still wants gas: the feeder must not
            // queue a second row for the same geyser.
            BoardState thirsty = BoardState.builder()
                .minerals(400).gas(100).supply(50, 60)
                .workers(10).bases(1).gateways(2).army(6)
                .core(true).assimilator(false)
                .seconds(400)
                .build();
            GoalBoardFeeder.ensure(thirsty);

            int assims = 0;
            for (ProductionOrder order : Queue.get().allOrders().list()) {
                if (order.unitType() != null && order.unitType().isGasBuilding()) assims++;
            }
            assertEquals(1, assims, "one geyser, one Assimilator row - no dynamic second");
        });
    }
}
