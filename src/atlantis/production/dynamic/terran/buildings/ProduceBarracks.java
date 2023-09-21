package atlantis.production.dynamic.terran.buildings;

import atlantis.game.A;
import atlantis.production.dynamic.DynamicCommanderHelpers;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.units.select.Select;
import atlantis.util.Enemy;

import static atlantis.units.AUnitType.Terran_Barracks;

public class ProduceBarracks {
    public static boolean barracks() {
        int barracks = Count.withPlanned(Terran_Barracks);

        if (barracks >= 4) return false;
        if (barracks >= 2 && Enemy.terran()) return false;
        if (Select.ourFree(Terran_Barracks).notEmpty()) return false;

        if (Select.ourFree(Terran_Barracks).size() > 0)
            System.err.println("@ " + A.now() + " - FREE BARRACKS " + Select.ourFree(Terran_Barracks).size());

        if (!A.hasMinerals(630)) {
//            if (barracks >= 3) {
//                return false;
//            }

            //        if (!Have.academy() && Count.existingOrInProductionOrInQueue(Terran_Barracks) >= 2) {
            if (!Have.academy() && Count.existingOrInProductionOrInQueue(Terran_Barracks) >= 2) return false;

            if (barracks >= 3 && A.supplyUsed() <= 40) return false;

            if (barracks >= 4 && A.supplyUsed() <= 70) return false;
        }

        if (Count.inProductionOrInQueue(Terran_Barracks) > 0) return false;

        if (A.canAffordWithReserved(150, 0) || A.hasMinerals(650)) {
            return DynamicCommanderHelpers.buildIfAllBusyButCanAfford(Terran_Barracks, 0, 0);
        }

        return false;
    }
}
