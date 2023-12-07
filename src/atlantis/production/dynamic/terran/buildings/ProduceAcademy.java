package atlantis.production.dynamic.terran.buildings;

import atlantis.game.A;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.units.select.Count;
import atlantis.units.select.Have;

import static atlantis.units.AUnitType.Terran_Academy;

public class ProduceAcademy {
    public static void academy() {
        if (
            (!A.supplyUsed(35) && !A.hasMinerals(300))
                || Have.academy()
                || Count.withPlanned(Terran_Academy) > 0
        ) return;

        if (Count.marines() >= 3 && A.hasMinerals(350)) {
            AddToQueue.toHave(Terran_Academy);
        }
    }
}
