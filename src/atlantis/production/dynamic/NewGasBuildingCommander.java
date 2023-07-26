package atlantis.production.dynamic;

import atlantis.architecture.Commander;
import atlantis.config.AtlantisConfig;
import atlantis.game.AGame;
import atlantis.production.constructing.ConstructionRequests;
import atlantis.production.orders.build.AddToQueue;
import atlantis.units.AUnit;
import atlantis.units.select.Select;

public class NewGasBuildingCommander extends Commander {
    @Override
    public void handle() {
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
        int numberOfGasBuildings = Select.ourWithUnfinished().ofType(AtlantisConfig.GAS_BUILDING).count();
        if (
            numberOfBases >= 2
                && numberOfBases > numberOfGasBuildings && !AGame.canAfford(0, 350)
                && ConstructionRequests.countNotStartedOfType(AtlantisConfig.GAS_BUILDING) == 0
                && hasABaseWithFreeGeyser()
        ) {
//            System.err.println("Request GAS BUILDING at supply: " + A.supplyUsed());
            AddToQueue.withTopPriority(AtlantisConfig.GAS_BUILDING);
        }
    }

    private static boolean hasABaseWithFreeGeyser() {
        for (AUnit base : Select.ourBases().list()) {
            if (Select.geysers().inRadius(8, base).isNotEmpty()) {
                return true;
            }
        }

        return false;
    }
}
