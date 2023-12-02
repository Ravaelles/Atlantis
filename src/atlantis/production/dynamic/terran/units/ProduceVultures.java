package atlantis.production.dynamic.terran.units;

import atlantis.game.A;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.util.Enemy;

public class ProduceVultures {
    public static boolean vultures() {
//        return false;

        int vultures = Count.withPlanned(AUnitType.Terran_Vulture);

        if (
            vultures > 0
                && (!A.hasMinerals(700) || A.hasGas(400))
                && Select.ourFree(AUnitType.Terran_Factory).atMost(1)
        ) return false;

        if (vultures == 0) {
            int maxAtATime = Math.min(
                Count.factories(),
                Count.marines() <= 8 ? 2 : 1
            );
            return AddToQueue.maxAtATime(AUnitType.Terran_Vulture, maxAtATime) != null;
        }

        if (Enemy.terran()) {
            if (vultures * 2 >= Count.marines() && !A.hasMinerals(750)) return false;

            if (vultures < 2 || (A.hasMinerals(600) && !A.hasGas(100))) {
                return AddToQueue.maxAtATime(AUnitType.Terran_Vulture, 5) != null;
            }
        }

        return false;

//        if (true) return false;
//
//        if (!Decisions.produceVultures()) {
//            return false;
//        }
//
//        return AddToQueue.addToQueueIfNotAlreadyThere(AUnitType.Terran_Vulture);
    }
}
