package atlantis.production.dynamic.protoss.buildings;

import atlantis.game.A;
import atlantis.production.dynamic.DynamicCommanderHelpers;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;

import static atlantis.units.AUnitType.Protoss_Shield_Battery;

public class ProduceShieldBattery {
    public static boolean produce() {
        if (A.minerals() <= 500 && A.minerals() <= 280 * Count.bases()) return false;
        if (Count.inProductionOrInQueue(type()) > 0) return false;

        if (Count.ourWithUnfinished(Protoss_Shield_Battery) >= max()) return false;

        if (A.everyNthGameFrame(43)) return produceNew();

        return false;
    }

    private static boolean produceNew() {
        return AddToQueue.toHave(type(), max());
    }

    private static int max() {
        return 1;
    }

    private static AUnitType type() {
        return Protoss_Shield_Battery;
    }
}
