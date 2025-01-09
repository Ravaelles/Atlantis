package atlantis.production.dynamic.terran.units;

import atlantis.game.A;
import atlantis.information.enemy.EnemyFlags;
import atlantis.production.dynamic.terran.buildings.ProduceScienceFacility;
import atlantis.production.orders.production.queue.CountInQueue;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.production.orders.production.queue.order.ProductionOrderPriority;
import atlantis.units.select.Have;

import static atlantis.units.AUnitType.*;

public class ProduceScienceVessels {
    public static boolean scienceVessels() {
        if (dontHaveRequirements()) return false;

        if (EnemyFlags.HAS_HIDDEN_COMBAT_UNIT) {
            return produceScienceVessel();
        }

        return produceScienceVessels();
    }

    private static boolean produceScienceVessel() {
        return AddToQueue.toHave(Terran_Science_Vessel, 1, ProductionOrderPriority.TOP);
    }

    private static boolean produceScienceVessels() {
        int base = (A.canAfford(700, 450) || A.supplyTotal() >= 90) ? 2 : 1;

        int limit = Math.max(
            base + (EnemyFlags.HAS_HIDDEN_COMBAT_UNIT ? 1 : 0),
            A.supplyTotal() / 40
        );

        return AddToQueue.toHave(Terran_Science_Vessel, limit, ProductionOrderPriority.TOP);
    }

    private static boolean dontHaveRequirements() {
//        if (Have.a(Terran_Control_Tower)) {
        if (Have.controlTower()) {
            if (CountInQueue.count(Terran_Science_Vessel) == 0) {
                if (produceScienceVessel()) return true;
            }
        }

        if (!Have.notEvenPlanned(Terran_Science_Facility)) {
            if (EnemyFlags.HAS_HIDDEN_COMBAT_UNIT || Have.controlTower()) {
                ProduceScienceFacility.produceScienceFacility();
            }

            return true;
        }

        return Have.no(Terran_Science_Facility);
    }
}
