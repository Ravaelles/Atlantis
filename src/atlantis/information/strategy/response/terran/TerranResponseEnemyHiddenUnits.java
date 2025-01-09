package atlantis.information.strategy.response.terran;

import atlantis.game.player.Enemy;
import atlantis.information.strategy.response.StrategyResponse;
import atlantis.map.choke.Chokes;
import atlantis.production.dynamic.protoss.buildings.ProduceCannonAtNatural;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;

public class TerranResponseEnemyHiddenUnits extends StrategyResponse {
    public void handle() {
        int howMany = 2;

        for (int i = 0; i < howMany; i++) {
            AddToQueue.withHighPriority(
                AUnitType.Terran_Missile_Turret,
                Chokes.mainChoke().translateTilesTowards(6, Select.mainOrAnyBuilding())
            );
        }
    }
}
