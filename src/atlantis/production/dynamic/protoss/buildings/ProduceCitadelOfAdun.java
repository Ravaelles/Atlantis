package atlantis.production.dynamic.protoss.buildings;

import atlantis.game.A;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.production.orders.production.queue.order.ProductionOrderPriority;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;

import static atlantis.units.AUnitType.Protoss_Citadel_of_Adun;
import static atlantis.units.AUnitType.Protoss_Observatory;
import static atlantis.units.AUnitType.Protoss_Templar_Archives;

public class ProduceCitadelOfAdun {
    public static boolean produce() {

        if (Have.a(type())) return false;
        if (Count.ofType(Protoss_Observatory) == 0) return false;

        // Default floor (owner 2026-11-09): a standalone Citadel changes
        // little, so it waits for 110 supply. Planned for the Templar
        // Archives it keeps the old floor - the templar line owns its
        // timing (ProduceTemplarArchives queues it directly anyway).
        boolean forArchives = !Have.notEvenPlanned(Protoss_Templar_Archives);
        int floor = forArchives ? 70 : 110;
        if (A.supplyUsed() <= floor) return false;

        if (A.hasGas(180) && A.now() % 41 == 0 && Have.notEvenPlanned(type())) {
            return AddToQueue.toHave(type(), 1, ProductionOrderPriority.HIGH);
        }


        return false;
    }

    private static AUnitType type() {
        return Protoss_Citadel_of_Adun;
    }
}
