package atlantis.production.dynamic.zerg.units;

import atlantis.game.A;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;

public class ProduceMutas {
    public static void mutalisks() {
        if (!Have.a(AUnitType.Zerg_Spire) && !Have.a(AUnitType.Zerg_Greater_Spire)) {
            return;
        }

        int mutas = Count.mutas();

        if (mutas <= 2) {
            AddToQueue.withHighPriority(AUnitType.Zerg_Mutalisk);
            AddToQueue.withHighPriority(AUnitType.Zerg_Mutalisk);
            AddToQueue.withHighPriority(AUnitType.Zerg_Mutalisk);
        }
        else {
            if (!A.canAffordWithReserved(75, 75)) {
                return;
            }

            if (Have.larvas(1) && A.canAffordWithReserved(50, 0)) {
                AddToQueue.withStandardPriority(AUnitType.Zerg_Mutalisk);
            }
        }
    }
}
