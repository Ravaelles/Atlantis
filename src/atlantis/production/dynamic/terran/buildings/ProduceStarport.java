package atlantis.production.dynamic.terran.buildings;

import atlantis.game.A;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.units.select.Select;
import atlantis.util.Enemy;

import static atlantis.units.AUnitType.Terran_Starport;

public class ProduceStarport {
    public static void starport() {
        if (shouldBuild()) {
            AddToQueue.toHave(Terran_Starport);
        }
    }

    private static boolean shouldBuild() {
        int minSupply = Enemy.zerg() ? 55 : 65;

        return (A.supplyUsed() >= minSupply || A.hasMinerals(500))
            && Have.factory()
            && noStarportsOrAllBusy();
    }

    private static boolean noStarportsOrAllBusy() {
        int all = Count.existingOrInProductionOrInQueue(Terran_Starport);
        return all == 0 || (A.canAfford(500, 250) && Select.ourFree(Terran_Starport).isEmpty());
    }
}
