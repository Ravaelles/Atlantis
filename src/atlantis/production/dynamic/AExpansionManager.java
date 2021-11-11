package atlantis.production.dynamic;

import atlantis.AGame;
import atlantis.AtlantisConfig;
import atlantis.map.Bases;
import atlantis.production.constructing.AConstructionRequests;
import atlantis.production.orders.AddToQueue;
import atlantis.production.orders.ProductionQueue;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.util.A;

public class AExpansionManager {

    public static boolean shouldBuildNewBase() {
        if (
                A.seconds() >= 600
                && Count.includingPlanned(AtlantisConfig.BASE) <= 1
        ) {
            return true;
        }

        boolean hasPlentyOfMinerals = AGame.hasMinerals(600);
        int minMinerals = 100 + (AGame.isPlayingAsZerg() ? 268 : 356);

        // It makes sense to think about expansion only if we have a lot of minerals.
        if (!AGame.canAffordWithReserved(minMinerals, 0)) {
            return false;
        }

        // If we have lenty of minerals, then every new base is a hazard
        if (!AGame.canAffordWithReserved(minMinerals, 1200)) {
            return false;
        }

        int inConstruction = ProductionQueue.countInQueue(AtlantisConfig.BASE, 8);
        if (inConstruction >= 1) {
            return false;
        }

        // === Force decent army before 3rd base =========================================

        // Enforce too have a lot of tanks before expansion
        int numberOfBases = Select.ourBases().count() + inConstruction;
        if (!hasPlentyOfMinerals && AGame.isPlayingAsTerran() && numberOfBases >= 2) {
            if (Select.ourTanks().count() <= 8) {
                return false;
            }
        }

        // === Check if we have almost as many bases as base locations; if so, exit ======

        if (numberOfBases >= Bases.baseLocations().size() - 2) {
            return false;
        }

        int numberOfUnfinishedBases = AConstructionRequests.countNotFinishedConstructionsOfType(AtlantisConfig.BASE);

        boolean haveEnoughMinerals = AGame.hasMinerals(minMinerals);
        boolean haveEnoughBases = numberOfBases >= 4
                && AGame.isPlayingAsZerg() && Select.ourLarva().count() >= 2;
        boolean noBaseToConstruct = numberOfUnfinishedBases == 0;
        boolean allowExtraExpansion = AGame.hasMinerals(minMinerals + 200)
                && numberOfUnfinishedBases <= 1;

        return haveEnoughMinerals && !haveEnoughBases && (noBaseToConstruct || allowExtraExpansion);
    }

    public static void requestNewBase() {
        // ZERG case
        if (AGame.isPlayingAsZerg()) {
            AddToQueue.withHighPriority(AtlantisConfig.BASE, Select.naturalOrMain());
        }

        // TERRAN + PROTOSS
        else {
            AddToQueue.withHighPriority(AtlantisConfig.BASE);
        }
    }
}
