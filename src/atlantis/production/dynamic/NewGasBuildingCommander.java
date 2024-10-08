package atlantis.production.dynamic;

import atlantis.architecture.Commander;
import atlantis.config.AtlantisRaceConfig;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.production.constructing.ConstructionRequests;
import atlantis.production.orders.production.queue.CountInQueue;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.units.select.Select;
import atlantis.util.We;

public class NewGasBuildingCommander extends Commander {
    @Override
    public boolean applies() {
        return A.everyNthGameFrame(23)
            && CountInQueue.count(AtlantisRaceConfig.GAS_BUILDING) == 0
            && !tooEarlyForAnotherGasBuilding();
    }

    @Override
    protected void handle() {
        requestGasBuildingIfNeeded();
    }

    private static boolean tooEarlyForAnotherGasBuilding() {
        if (Count.existingOrInProduction(AtlantisRaceConfig.GAS_BUILDING) >= 1) {
            if (!A.hasMinerals(200) || A.supplyTotal() <= 30) {
                return true;
            }
        }

        if (We.zerg()) {
            if (!A.hasMinerals(300)) return false;
            if (!Have.unfinishedOrPlanned(AUnitType.Zerg_Spawning_Pool)) return false;
        }

        return false;
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
                && numberOfBases > numberOfGasBuildings && !A.canAfford(0, 350)
                && ConstructionRequests.countNotStartedOfType(AtlantisRaceConfig.GAS_BUILDING) == 0
                && hasABaseWithFreeGeyser()
        ) {
//            A.errPrintln("Request GAS BUILDING at supply: " + A.supplyUsed());
            AddToQueue.withTopPriority(AtlantisRaceConfig.GAS_BUILDING);
        }
    }

    private static boolean hasABaseWithFreeGeyser() {
        for (AUnit base : Select.ourBases().list()) {
//            for (AUnit geyser : Select.geysers().inRadius(10, base).list()) {
//            }
            if (Select.ourOfType(AtlantisRaceConfig.GAS_BUILDING).inRadius(10, base).isNotEmpty()) {
                return false;
            }

            if (Select.geysers().inRadius(10, base).isNotEmpty()) {
                return true;
            }
        }

        return false;
    }
}
