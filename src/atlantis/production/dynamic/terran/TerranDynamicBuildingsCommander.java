package atlantis.production.dynamic.terran;

import atlantis.game.A;
import atlantis.production.dynamic.DynamicCommanderHelpers;
import atlantis.production.dynamic.reinforce.terran.turrets.ReinforceBunkersWithTurrets;
import atlantis.production.dynamic.reinforce.terran.turrets.here.OffensiveTurretsCommander;
import atlantis.production.dynamic.terran.buildings.*;
import atlantis.production.orders.production.queue.CountInQueue;

public class TerranDynamicBuildingsCommander extends DynamicCommanderHelpers {
    @Override
    public boolean applies() {
        return CountInQueue.countDynamicBuildingsOrders() <= 6;
    }

    @Override
    protected void handle() {
//        if (true) return;

//        ReinforceBasesWithCombatBuildings.get().invoke();

        // Turrets
//        (new ReinforceBunkersWithTurrets()).invoke();
//        (new OffensiveTurretsCommander()).invoke();

        if (A.everyNthGameFrame(57)) {
            ProduceScienceFacility.scienceFacilities();
            ProduceStarport.starport();
            ProduceEngBay.engBay();
        }

        if (A.everyNthGameFrame(13)) {
            ProduceComsatStation.comsats();
            ProduceControlTower.controlTowers();
            ProduceBarracks.barracks();
            ProduceFactoryWhenBioOnly.factoryIfBioOnly();
        }

        if (A.everyNthGameFrame(33)) {
            ProduceArmory.armory();
            ProduceAcademy.academy();
        }

        if (A.everyNthGameFrame(37)) {
            ProduceMachineShop.machineShop();
            ProduceFactory.factories();
        }
    }
}
