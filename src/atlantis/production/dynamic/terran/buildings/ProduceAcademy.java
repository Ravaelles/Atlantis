package atlantis.production.dynamic.terran.buildings;

import atlantis.game.A;
import atlantis.production.orders.production.queue.CountInQueue;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.units.select.Count;
import atlantis.units.select.Have;

import static atlantis.units.AUnitType.Terran_Academy;

public class ProduceAcademy {
    public static boolean academy() {
        if (
            (!A.supplyUsed(35) && !A.hasMinerals(300))
                || Have.academy()
                || Count.withPlanned(Terran_Academy) > 0
                || CountInQueue.count(Terran_Academy) > 0
        ) return false;

        if (Count.marines() >= 3 && A.hasMinerals(350)) {
            return AddToQueue.toHave(Terran_Academy);
        }

        return false;
    }
}
