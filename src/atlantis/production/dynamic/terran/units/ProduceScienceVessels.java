package atlantis.production.dynamic.terran.units;

import atlantis.game.A;
import atlantis.information.enemy.EnemyFlags;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.production.orders.production.queue.order.ProductionOrderPriority;
import atlantis.units.select.Have;

import static atlantis.production.AbstractDynamicUnits.buildToHave;
import static atlantis.units.AUnitType.Terran_Science_Facility;
import static atlantis.units.AUnitType.Terran_Science_Vessel;

public class ProduceScienceVessels {
    public static void scienceVessels() {
        if (makeSureToHaveScienceFacility()) return;

        if (EnemyFlags.HAS_HIDDEN_COMBAT_UNIT) {
            AddToQueue.toHave(Terran_Science_Vessel, 1, ProductionOrderPriority.TOP);
            return;
        }

        int limit = Math.max(
            1 + (EnemyFlags.HAS_HIDDEN_COMBAT_UNIT ? 2 : 0),
            A.supplyTotal() / 35
        );
        AddToQueue.toHave(Terran_Science_Vessel, limit, ProductionOrderPriority.HIGH);
    }

    private static boolean makeSureToHaveScienceFacility() {
        if (!Have.notEvenPlanned(Terran_Science_Facility)) {
            if (EnemyFlags.HAS_HIDDEN_COMBAT_UNIT) {
                AddToQueue.toHave(Terran_Science_Facility);
            }
            return true;
        }

        if (Have.no(Terran_Science_Facility)) return true;
        return false;
    }
}
