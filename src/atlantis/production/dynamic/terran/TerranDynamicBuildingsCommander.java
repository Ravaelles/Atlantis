package atlantis.production.dynamic.terran;

import atlantis.game.A;
import atlantis.production.dynamic.DynamicCommanderHelpers;
import atlantis.production.dynamic.reinforce.terran.turrets.OffensiveTurretsCommander;
import atlantis.production.dynamic.terran.buildings.*;
import atlantis.production.orders.production.queue.CountInQueue;
import atlantis.util.We;

public class TerranDynamicBuildingsCommander extends DynamicCommanderHelpers {
    @Override
    public boolean applies() {
        return We.terran() && CountInQueue.countDynamicBuildingsOrders() <= 6;
    }

    @Override
    protected void handle() {
//        if (true) return;

//        ReinforceBasesWithCombatBuildings.get().invoke(this);

        // Turrets
//        (new ReinforceBunkersWithTurrets()).invoke(this);
        if (A.everyNthGameFrame(59)) {
            (new OffensiveTurretsCommander()).invokeCommander();
        }

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
