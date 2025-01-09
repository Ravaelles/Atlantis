package atlantis.production.dynamic.expansion.terran;

import atlantis.config.AtlantisRaceConfig;
import atlantis.game.A;
import atlantis.information.generic.Army;
import atlantis.information.strategy.Strategy;
import atlantis.production.orders.production.queue.CountInQueue;
import atlantis.units.select.Count;
import atlantis.units.select.Have;

public class TerranShouldExpandToNatural {
    public static boolean shouldExpandToNatural() {
        if (A.supplyUsed() <= 20) return false;
        if (Count.existingOrInProductionOrInQueue(AtlantisRaceConfig.BASE) > 1) {
            return TerranShouldExpand.returnNo("Pending");
        }

        if (A.s >= 60 * 9) return TerranShouldExpand.returnYes("GotLate");

        if (Strategy.get().goingBio() && Army.strength() <= 200) return TerranShouldExpand.returnNo("NeedStrongerBio");

        if (!A.hasMinerals(550) && !Have.factoryWithUnfinished()) return TerranShouldExpand.returnNo("NoFactory");

        if (A.s >= 60 * 6 && Army.strength() >= 110) return TerranShouldExpand.returnYes("LooksSafe");
        if (manyMinerals()) return TerranShouldExpand.returnYes("ManyMins");

//        if (
////            ArmyStrength.weAreStronger()
////                && (
//            A.canAffordWithReserved(360, 0)
//                || Count.withPlanned(AUnitType.Terran_Bunker) == 0
////            )
//        ) return true;

        return TerranShouldExpand.returnNo("JustNo");
    }

    private static boolean manyMinerals() {
        return A.hasMinerals(370) && Count.basesWithUnfinished() <= 1 && CountInQueue.bases() == 0;
    }
}
