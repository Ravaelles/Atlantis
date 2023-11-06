package atlantis.production.dynamic.terran.units;

import atlantis.game.AGame;
import atlantis.information.strategy.EnemyStrategy;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;

public class ProduceGoliaths {
    public static boolean goliaths(AUnit factory) {
        if (!Have.armory()) return false;

        if (EnemyStrategy.get().isAirUnits() && Count.withPlanned(AUnitType.Terran_Goliath) <= 20) {
            if (AGame.canAffordWithReserved(150, 100)) {
                return AddToQueue.addToQueueIfNotAlreadyThere(AUnitType.Terran_Goliath);
            }
        }

        return false;
    }
}
