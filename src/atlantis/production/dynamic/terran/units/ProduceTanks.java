package atlantis.production.dynamic.terran.units;

import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.information.decisions.Decisions;
import atlantis.production.dynamic.terran.tech.SiegeMode;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.units.select.Select;
import atlantis.util.Enemy;

public class ProduceTanks {
    public static boolean tanks(AUnit factory) {
        if (!Have.machineShop() || !Have.factory()) return false;

        int tanks = Select.ourWithUnfinished().tanks().count();
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
        if (!Decisions.produceVultures() || tanks <= 5 || A.canAfford(700, 250)) {
            return produceTank();
        }

        return false;
    }

    private static boolean produceTank() {
        return AddToQueue.maxAtATime(
            AUnitType.Terran_Siege_Tank_Tank_Mode,
            A.inRange(1, AGame.minerals() / 160, Count.ofType(AUnitType.Terran_Machine_Shop))
        ) != null;
    }

    private static boolean canAffordWithReserved() {
        return AGame.canAffordWithReserved(150, 100);
    }
}
