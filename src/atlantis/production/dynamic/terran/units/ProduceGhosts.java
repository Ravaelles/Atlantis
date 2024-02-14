package atlantis.production.dynamic.terran.units;

import atlantis.game.A;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.util.Enemy;

public class ProduceGhosts {
    public static boolean ghosts() {
        if (Enemy.zerg()) return false;

        if (
            Count.ofType(AUnitType.Terran_Covert_Ops) == 0
                && Count.ofType(AUnitType.Terran_Science_Facility) == 0
        ) return false;

        int ghosts = Count.ofType(AUnitType.Terran_Ghost);

        if (ghosts >= 2 && !A.hasGas(150)) return false;

        if (ghosts >= 8 || A.supplyUsed() <= 80 + 5 * ghosts) return false;

        if (ghosts >= 4 && !A.canAffordWithReserved(60, 200)) return false;

        if (!A.hasGas(ghosts * 25)) return false;

        return AddToQueue.addToQueueIfNotAlreadyThere(AUnitType.Terran_Ghost);
    }
}
