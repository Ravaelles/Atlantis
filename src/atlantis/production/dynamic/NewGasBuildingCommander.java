package atlantis.production.dynamic;

import atlantis.architecture.Commander;
import atlantis.config.AtlantisRaceConfig;
import atlantis.game.AGame;
import atlantis.production.constructing.ConstructionRequests;
import atlantis.production.orders.build.AddToQueue;
import atlantis.units.AUnit;
import atlantis.units.select.Count;
import atlantis.units.select.Select;

public class NewGasBuildingCommander extends Commander {
    @Override
    protected void handle() {
        requestGasBuildingIfNeeded();
    }

    /**
     * Build Refineries/Assimilators/Extractors when it makes sense.
     */
    private static void requestGasBuildingIfNeeded() {
        if (AGame.supplyUsed() <= 18) {
            return;
        }

        if (AGame.everyNthGameFrame(37)) {
            return;
        }

        // =========================================================

        int numberOfBases = Select.ourBases().count();
//        int numberOfGasBuildings = Select.ourWithUnfinished().ofType(AtlantisRaceConfig.GAS_BUILDING).count();
        int numberOfGasBuildings = Count.withPlanned(AtlantisRaceConfig.GAS_BUILDING);
        if (
            numberOfBases >= 2
                && numberOfBases > numberOfGasBuildings && !AGame.canAfford(0, 350)
                && ConstructionRequests.countNotStartedOfType(AtlantisRaceConfig.GAS_BUILDING) == 0
                && hasABaseWithFreeGeyser()
        ) {
//            System.err.println("Request GAS BUILDING at supply: " + A.supplyUsed());
            AddToQueue.withTopPriority(AtlantisRaceConfig.GAS_BUILDING);
        }
    }

    private static boolean hasABaseWithFreeGeyser() {
        for (AUnit base : Select.ourBases().list()) {
            if (Select.geysers().inRadius(13, base).isNotEmpty()) {
                return true;
            }
        }

        return false;
    }
}
