package atlantis.production.dynamic.terran.units;

import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.units.select.Select;

public class ProduceWraiths {
    public static boolean wraiths() {
        if (Count.ofType(AUnitType.Terran_Starport) == 0) return false;
        if (prioritizeScienceFacility()) return false;
        if (Count.inProductionOrInQueue(AUnitType.Terran_Wraith) >= 2) return false;

        boolean produceWraiths = A.supplyUsed() >= 90 || A.canAfford(600, 400);

        int wraiths = Count.ofType(AUnitType.Terran_Wraith);
        boolean canAffordWithReserved = AGame.canAffordWithReserved(150, 100);

        if (wraiths >= 5 && !canAffordWithReserved) return false;

        if (produceWraiths && wraiths <= 1) {
            return AddToQueue.addToQueueIfNotAlreadyThere(AUnitType.Terran_Wraith);
        }

        if (wraiths >= 12) return false;

        return produce();
    }

    private static boolean prioritizeScienceFacility() {
        if (!A.canAfford(650, 350) && Have.notEvenPlanned(AUnitType.Terran_Science_Facility)) return true;

        return false;
    }

    private static boolean produce() {
        int maxAtATime = Math.min(2, Select.ourOfType(AUnitType.Terran_Starport).free().count());

        return AddToQueue.maxAtATime(AUnitType.Terran_Wraith, maxAtATime) != null;
    }
}
