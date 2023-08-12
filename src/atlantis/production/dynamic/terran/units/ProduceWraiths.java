package atlantis.production.dynamic.terran.units;

import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.production.orders.build.AddToQueue;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Select;

public class ProduceWraiths {
    public static boolean wraiths() {
        if (Count.ofType(AUnitType.Terran_Starport) == 0) return false;

        boolean produceWraiths = A.supplyUsed() >= 90;

        int wraiths = Count.ofType(AUnitType.Terran_Wraith);
        boolean canAffordWithReserved = AGame.canAffordWithReserved(150, 100);

        if (wraiths >= 5 && !canAffordWithReserved) return false;

        if (produceWraiths && wraiths <= 1) {
            return AddToQueue.addToQueueIfNotAlreadyThere(AUnitType.Terran_Wraith);
        }

        if (wraiths >= 12) return false;

        return produce();
    }

    private static boolean produce() {
        int maxAtATime = Math.min(5, Select.ourOfType(AUnitType.Terran_Starport).free().count());

        return AddToQueue.maxAtATime(AUnitType.Terran_Wraith, maxAtATime);
    }
}