package atlantis.production.dynamic.terran.units;

import atlantis.game.A;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.units.select.Have;
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
            && Have.factory();
    }
}
