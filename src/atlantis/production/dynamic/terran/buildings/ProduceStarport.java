package atlantis.production.dynamic.terran.buildings;

import atlantis.game.A;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.units.select.Select;
import atlantis.game.player.Enemy;

import static atlantis.units.AUnitType.Terran_Starport;

public class ProduceStarport {
    public static boolean starport() {
        if (shouldBuild()) {
            return AddToQueue.toHave(Terran_Starport);
        }
        
        return false;
    }

    private static boolean shouldBuild() {
        if (A.supplyUsed() <= 140) return false;

        if (Count.freeStarports() > 0) return false;

        int minSupply = Enemy.zerg() ? 55 : 65;

        return (A.supplyUsed() >= minSupply || A.hasMinerals(500))
            && Have.factory()
            && noStarports();
    }

    private static boolean noStarports() {
        int all = Count.withPlanned(Terran_Starport);
        return all == 0 || existingOnesAreBusyAndWeHaveResources();
    }

    private static boolean existingOnesAreBusyAndWeHaveResources() {
        return A.canAfford(700, 350)
            && Select.ourFree(Terran_Starport).isEmpty()
            && Count.notBeingProduced(Terran_Starport);
    }
}
