package atlantis.production.dynamic.expansion.protoss;

import atlantis.config.AtlantisRaceConfig;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.map.base.BaseLocations;
import atlantis.production.constructing.ConstructionRequests;
import atlantis.production.dynamic.zerg.ZergExpansionCommander;
import atlantis.production.orders.production.queue.CountInQueue;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.util.We;

public class ProtossShouldExpand {
    public static boolean shouldExpand() {
        int bases = Count.bases();
        int basesInProduction = Count.inProductionOrInQueue(AtlantisRaceConfig.BASE);

        if (basesInProduction > 0) return false;
        if (bases >= 5) return false;

        // === Second base ===========================================

        if (bases <= 1) {
            return forSecondBase();
        }

        // =========================================================

        if (bases >= 3 && Count.workers() <= 17 * (bases + basesInProduction)) return false;

        boolean hasPlentyOfMinerals = AGame.hasMinerals(580);
        int minMinerals = 100 + (AGame.isPlayingAsZerg() ? 268 : 356);

        // It makes sense to think about expansion only if we have a lot of minerals.
        if (!AGame.canAffordWithReserved(minMinerals, 0)) return false;

        // === False again ===========================================

        // If we have plenty of minerals, then every new base is only a hazard
        if (!AGame.canAffordWithReserved(minMinerals, 1200)) return false;

        int inConstruction = CountInQueue.count(AtlantisRaceConfig.BASE, 8);
        if (inConstruction >= 1) return false;

        // === Check if we have almost as many bases as base locations; if so, exit ======

        if (bases >= BaseLocations.baseLocations().size() - 2) return false;

        int numberOfUnfinishedBases = ConstructionRequests.countNotFinishedOfType(AtlantisRaceConfig.BASE);

        boolean haveEnoughMinerals = AGame.hasMinerals(minMinerals);
//        boolean haveEnoughBases = bases >= 4 && AGame.isPlayingAsZerg() && Select.ourLarva().count() >= 2;
        boolean noBaseToConstruct = numberOfUnfinishedBases == 0;
        boolean allowExtraExpansion = AGame.hasMinerals(minMinerals + 200) && numberOfUnfinishedBases <= 1;

        return haveEnoughMinerals && (noBaseToConstruct || allowExtraExpansion);
    }

    private static boolean forSecondBase() {
        boolean secondsAllow =
            (
                (A.seconds() >= 400 && Count.ourCombatUnits() >= 20)
                    || (A.seconds() >= 520 && Count.ourCombatUnits() >= 8)
                    || (A.seconds() >= 650)
            );
//                (
//                    (A.seconds() >= 500 && Count.ourCombatUnits() >= 20)
//                    || (A.seconds() >= 600 && Count.ourCombatUnits() >= 8)
//                    || (A.seconds() >= 700)
//                );
        if (AGame.canAfford(330, 0) || secondsAllow) {
            return true;
        }

        return false;
    }
}
