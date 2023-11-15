package atlantis.production.dynamic.terran.units;

import atlantis.game.A;
import atlantis.information.enemy.EnemyFlags;
import atlantis.production.dynamic.terran.buildings.ProduceScienceFacility;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.production.orders.production.queue.order.ProductionOrderPriority;
import atlantis.units.select.Have;

import static atlantis.units.AUnitType.Terran_Science_Facility;
import static atlantis.units.AUnitType.Terran_Science_Vessel;

public class ProduceScienceVessels {
    public static void scienceVessels() {
        if (dontHaveScienceFacility()) return;

        if (EnemyFlags.HAS_HIDDEN_COMBAT_UNIT) {
            produceScienceVessel();
            return;
        }

        int limit = Math.max(
            1 + (EnemyFlags.HAS_HIDDEN_COMBAT_UNIT ? 2 : 0),
            A.supplyTotal() / 35
        );
        produceScienceVessels(limit);
    }

    private static boolean produceScienceVessel() {
        return AddToQueue.toHave(Terran_Science_Vessel, 1, ProductionOrderPriority.TOP);
    }

    private static boolean produceScienceVessels(int upTo) {
        return AddToQueue.toHave(Terran_Science_Vessel, upTo, ProductionOrderPriority.TOP);
    }

    private static boolean dontHaveScienceFacility() {
        if (!Have.notEvenPlanned(Terran_Science_Facility)) {
            if (EnemyFlags.HAS_HIDDEN_COMBAT_UNIT) ProduceScienceFacility.produceScienceFacility();

            return true;
        }

        return Have.no(Terran_Science_Facility);
    }
}
