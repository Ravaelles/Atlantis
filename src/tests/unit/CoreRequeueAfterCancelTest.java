package tests.unit;

import atlantis.decisions.board.BoardState;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.goalboard.GoalBoardFeeder;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.AbstractTestWithWorld;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.fakes.FakeUnitHelper.merge;

/**
 * NEXT #58 follow-up (game 152437): after a cancelled Core, zero Core lines
 * (ADDED/NOT_READY/QUEUE_BASE) appeared for 200+ seconds while the bank
 * swung 100-440. Replicates that late-game state: 48 supply,
 * 300 bank, 10 Zealots, 2 Gateways, 5 Pylons, no Core, Zerg enemy.
 * B1: the V2 goal + feeder own the requeue now - a missing Core with
 * the bank behind it feeds a Core row.
 */
@Tag("production")
public class CoreRequeueAfterCancelTest extends AbstractTestWithWorld {

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
    public void coreProducerSaysYesWhenCoreMissing() {
        FakeUnit[] zealots = new FakeUnit[10];
        for (int i = 0; i < 10; i++) zealots[i] = fake(AUnitType.Protoss_Zealot, 30 + i, 20);

        world(20, merge(
            fakeOurs(
                fake(AUnitType.Protoss_Nexus, 20, 20),
                fake(AUnitType.Protoss_Pylon, 24, 20),
                fake(AUnitType.Protoss_Pylon, 26, 20),
                fake(AUnitType.Protoss_Gateway, 28, 20),
                fake(AUnitType.Protoss_Gateway, 30, 20)
            ),
            zealots
        ), fakeEnemies(fake(AUnitType.Zerg_Zergling, 60, 20)), () -> {
            // The frame the legacy commander ran on (every 17th).
            if (atlantis.game.A.now() != 17) return;

            Queue.get().allOrders().list().clear();
            Queue.get().clearCache();
            ConstructionRequests.constructions.clear();

            BoardState requeue = BoardState.builder()
                .minerals(300).supply(48, 60)
                .workers(10).bases(1).gateways(2).army(10)
                .enemyRace("Zerg")
                .seconds(600)
                .build();
            String fed = GoalBoardFeeder.ensure(requeue);

            assertTrue(fed.contains("core"),
                "cancelled Core, bank 300 behind it: the V2 goal feeds a Core row, got: " + fed);
        });
    }
}
