package tests.acceptance;

import atlantis.game.AGame;
import atlantis.information.strategy.OurStrategy;
import atlantis.information.strategy.TerranStrategies;
import atlantis.production.orders.build.ABuildOrder;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.QueueInitializer;
import atlantis.units.AUnitType;
import tests.unit.FakeUnit;

public class NonAbstractTestFakingGame extends AbstractTestFakingGame {
    protected Queue queue = null;
    protected ABuildOrder buildOrder = null;

    @Override
    protected FakeUnit[] generateOur() {
        return null;
    }

    @Override
    protected FakeUnit[] generateEnemies() {
        return null;
    }

    public void initSupply() {
        int supplyUsed = options.getIntOr("supplyUsed", 49);
        int supplyTotal = options.getIntOr("supplyTotal", supplyUsed + 2);

        initSupply(supplyUsed, supplyTotal);
    }

    public void initSupply(int supplyUsed, int supplyTotal) {
        aGame.when(AGame::supplyUsed).thenReturn(supplyUsed);
        aGame.when(AGame::supplyTotal).thenReturn(supplyTotal);
        aGame.when(AGame::supplyFree).thenReturn(supplyTotal - supplyUsed);
    }

    protected FakeUnit[] fakeExampleOurs() {
        return fakeOurs(
            fake(AUnitType.Terran_Command_Center, 10),
            fake(AUnitType.Terran_SCV, 11),
            fake(AUnitType.Terran_SCV, 12),
            fake(AUnitType.Terran_SCV, 13),
            fake(AUnitType.Terran_SCV, 14)
        );
    }

    protected FakeUnit[] fakeExampleEnemies() {
        return fakeEnemies(fake(AUnitType.Zerg_Zergling, 19));
    }

    protected Queue initQueue() {
        return initQueue(3456, 2345);
    }

//    protected Queue initQueue(int minerals, int gas) {
//        aGame.when(AGame::minerals).thenReturn(minerals);
//        aGame.when(AGame::gas).thenReturn(gas);
//        OurStrategy.setTo(TerranStrategies.TERRAN_Tests);
//
//        initSupply();
//
//        QueueInitializer.initializeProductionQueue();
//
//        return queue = Queue.get();
//    }

    protected Queue initQueue(int minerals, int gas) {
        if (Queue.get() != null) Queue.get().clearCache();

        aGame.when(AGame::minerals).thenReturn(minerals);
        aGame.when(AGame::gas).thenReturn(gas);
        OurStrategy.setTo(TerranStrategies.TERRAN_Tests);

        buildOrder = OurStrategy.get().buildOrder();
        initSupply();

        QueueInitializer.initializeProductionQueue();

        return queue = Queue.get();
    }
}
