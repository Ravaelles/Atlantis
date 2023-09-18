package atlantis.production.dynamic.terran.units;

import atlantis.production.dynamic.DynamicCommanderHelpers;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.units.select.Have;

import static atlantis.units.AUnitType.Terran_Armory;
import static atlantis.units.AUnitType.Terran_Factory;

public class ProduceArmory {
    public static boolean armory() {
        if (!Have.factory()) return false;

        if (DynamicCommanderHelpers.enemyStrategy().isAirUnits()) {
            if (DynamicCommanderHelpers.haveNoExistingOrPlanned(Terran_Armory)) {
                return AddToQueue.toHave(Terran_Armory, 1);
            }
        }

        return false;
    }
}
