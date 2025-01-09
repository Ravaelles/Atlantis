package atlantis.production.dynamic.terran;

import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.production.dynamic.DynamicCommanderHelpers;
import atlantis.production.dynamic.expansion.decision.ShouldExpand;
import atlantis.production.dynamic.supply.ProduceFallbackDepotWhenSupplyLow;
import atlantis.production.dynamic.reinforce.terran.turrets.OffensiveTurretsCommander;
import atlantis.production.dynamic.terran.buildings.*;
import atlantis.util.We;

public class TerranDynamicBuildingsCommander extends DynamicCommanderHelpers {
    @Override
    public boolean applies() {
        return We.terran()
            && AGame.everyNthGameFrame(17)
            && (
            A.canAffordWithReserved(92, 0) || (A.hasMinerals(260) && !ShouldExpand.shouldExpand())
        );
    }

    @Override
    protected void handle() {
        if ((new ProduceFallbackDepotWhenSupplyLow()).produceIfNeeded()) return;

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
