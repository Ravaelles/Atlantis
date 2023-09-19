package atlantis.production.dynamic.terran.buildings;

import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.units.select.Select;

import static atlantis.units.AUnitType.Terran_Comsat_Station;

public class ProduceComsatStation {
    public static void comsats() {
        if (!Have.academy()) {
            return;
        }

        if (
            Count.bases() > Count.withPlanned(Terran_Comsat_Station)
                && Count.inQueueOrUnfinished(Terran_Comsat_Station, 10) <= 0
        ) {
            AddToQueue.toHave(Terran_Comsat_Station, Select.ourBases().withoutAddon().size());
        }
    }
}
