package atlantis.production.dynamic.terran;

import atlantis.game.A;
import atlantis.production.dynamic.DynamicCommanderHelpers;
import atlantis.production.dynamic.reinforce.terran.turrets.ReinforceBunkersWithTurrets;
import atlantis.production.dynamic.reinforce.terran.turrets.TurretNeededHere;
import atlantis.production.dynamic.terran.buildings.*;

public class TerranDynamicBuildingsCommander extends DynamicCommanderHelpers {
    @Override
    protected void handle() {
//        if (true) return;

//        ReinforceBasesWithCombatBuildings.get().invoke();
        (new ReinforceBunkersWithTurrets()).invoke();
        (new TurretNeededHere()).invoke();

        if (A.everyNthGameFrame(13)) {
            ProduceComsatStation.comsats();
            ProduceBarracks.barracks();
            ProduceFactoryWhenBioOnly.factoryIfBioOnly();
        }

        if (A.everyNthGameFrame(33)) {
            ProduceArmory.armory();
            ProduceAcademy.academy();
        }

        if (A.everyNthGameFrame(37)) {
            ProduceMachineShop.machineShop();
            BuildFactory.factories();
        }

        if (A.everyNthGameFrame(67)) {
            ProduceScienceFacility.scienceFacilities();
            ProduceStarport.starport();
            ProduceEngBay.engBay();
        }
    }
}
