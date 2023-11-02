package atlantis.production.dynamic.terran;

import atlantis.game.A;
import atlantis.production.dynamic.DynamicCommanderHelpers;
import atlantis.production.dynamic.reinforce.terran.turrets.ReinforceBunkersWithTurrets;
import atlantis.production.dynamic.reinforce.terran.turrets.TurretNeededHere;
import atlantis.production.dynamic.terran.buildings.*;
import atlantis.production.orders.production.queue.CountInQueue;

public class TerranDynamicBuildingsCommander extends DynamicCommanderHelpers {
    @Override
    public boolean applies() {
        return CountInQueue.countDynamicBuildingsOrders() <= 3;
    }

    @Override
    protected void handle() {
//        if (true) return;

//        ReinforceBasesWithCombatBuildings.get().invoke();
        (new ReinforceBunkersWithTurrets()).invoke();
        (new TurretNeededHere()).invoke();

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

        if (A.everyNthGameFrame(67)) {
            ProduceScienceFacility.scienceFacilities();
            ProduceStarport.starport();
            ProduceEngBay.engBay();
        }
    }
}
