package atlantis.production.dynamic.terran;

import atlantis.combat.micro.terran.bunker.TerranBunker;
import atlantis.game.A;
import atlantis.production.dynamic.DynamicCommanderHelpers;
import atlantis.production.dynamic.reinforce.terran.turrets.OffensiveTurretsCommander;
import atlantis.production.dynamic.terran.buildings.*;
import atlantis.production.orders.production.queue.CountInQueue;
import atlantis.util.We;

public class TerranDynamicBuildingsCommander extends DynamicCommanderHelpers {
    @Override
    public boolean applies() {
        return We.terran() && A.everyNthGameFrame(33);
//        A.canAffordWithReserved(92, 0);
    }

    @Override
    protected void handle() {
//        if (true) return;

//        ReinforceBasesWithCombatBuildings.get().invoke(this);

//        if (A.everyNthGameFrame(33)) {
        if (
            ProduceBunker.produce()
                || ProduceTurretForBunker.produce()
                || ProduceEngineeringBay.engBay()

                || ProduceScienceFacility.scienceFacilities()
                || ProduceStarport.starport()
                || ProduceComsatStation.comsats()
                || ProduceControlTower.controlTowers()

                || ProduceArmory.armory()
                || ProduceAcademy.academy()

                || ProduceMachineShop.produce()
                || ProduceFactory.factory()

                || ProduceBarracks.barracks()
        ) return;
//        }

        // Turrets
//        (new ReinforceBunkersWithTurrets()).invoke(this);
//        if (A.everyNthGameFrame(59)) {
        (new OffensiveTurretsCommander()).invokeCommander();
//        }

    }
}
