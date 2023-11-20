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

        produceScienceVessels();
    }

    private static boolean produceScienceVessel() {
        System.err.println("@ " + A.now() + " - produce VESSEL A");
        return AddToQueue.toHave(Terran_Science_Vessel, 1, ProductionOrderPriority.TOP);
    }

    private static boolean produceScienceVessels() {
        int base = (A.canAfford(700, 450) || A.supplyTotal() >= 130) ? 2 : 1;

        int limit = Math.max(
            base + (EnemyFlags.HAS_HIDDEN_COMBAT_UNIT ? 1 : 0),
            A.supplyTotal() / 40
        );

        System.err.println("@ " + A.now() + " - produce VESSEL B = " + limit);

        return AddToQueue.toHave(Terran_Science_Vessel, limit, ProductionOrderPriority.TOP);
    }

    private static boolean dontHaveScienceFacility() {
        if (!Have.notEvenPlanned(Terran_Science_Facility)) {
            if (EnemyFlags.HAS_HIDDEN_COMBAT_UNIT || Have.controlTower()) {
                ProduceScienceFacility.produceScienceFacility();
            }

            return true;
        }

        return Have.no(Terran_Science_Facility);
    }
}
