package atlantis.production.dynamic.terran.units;

import atlantis.game.A;
import atlantis.information.decisions.terran.TerranDecisions;
import atlantis.production.dynamic.terran.tech.SiegeMode;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.game.player.Enemy;

public class ProduceTanks {

    private static int tanks;

    public static boolean tanks() {
        if (TerranDecisions.DONT_PRODUCE_TANKS_AT_ALL.isTrue()) return false;
        if (!Have.machineShop() || !Have.factory()) return false;

        if (!A.canAfford(150, 100)) return false;

        Selection freeFactory = Select.ourFree(AUnitType.Terran_Factory);
        if (freeFactory == null) return false;

        tanks = Select.ourWithUnfinished().tanks().count();
        if (tanks <= 2) return produceTank();

        boolean canAffordWithReserved = canAffordWithReserved();

        if (!Enemy.terran() && tanks >= 8) {
            if (!canAffordWithReserved && !A.canAfford(600, 200)) return false;
        }

        if (tanks >= 2 && Enemy.zerg() && Count.ofType(AUnitType.Terran_Wraith) <= 1) {
            if (!canAffordWithReserved) return false;
        }

//        if (Count.infantry() >= 6 && Count.medics() <= 1) {
//            return canAffordWithReserved;
//        }

        if (Enemy.terran() && tanks >= 1 && !SiegeMode.isResearched()) {
            if (!canAffordWithReserved) return false;
        }

        if (Enemy.protoss() && tanks >= 4 && Count.scienceVessels() == 0) {
            if (canAffordWithReserved) produceTank();
        }

//        int vultures = Count.vultures();
        if (!TerranDecisions.produceVultures() || tanks <= 5 || A.canAfford(700, 250)) {
            return produceTank();
        }

        return false;
    }

    private static boolean produceTank() {
        return ForceProduceUnit.forceProduce(AUnitType.Terran_Siege_Tank_Tank_Mode);
//        return AddToQueue.maxAtATime(
//            AUnitType.Terran_Siege_Tank_Tank_Mode,
//            A.inRange(1, AGame.minerals() / 160, Count.ofType(AUnitType.Terran_Machine_Shop))
//        ) != null;
    }

    private static boolean canAffordWithReserved() {
        return A.canAffordWithReserved(150, 100);
    }
}
