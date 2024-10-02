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

    private static int numberOfGasBuildings;

    @Override
    public boolean applies() {
        return !We.protoss()
            && A.everyNthGameFrame(23)
            && (A.gas() <= 600 || A.minerals() >= 500)
            && (A.gas() <= 100 || Count.ourCombatUnits() >= 10)
            && CountInQueue.count(AtlantisRaceConfig.GAS_BUILDING) * 250 <= A.minerals()
            && !tooEarlyForAnotherGasBuilding();
    }

    @Override
    protected void handle() {
        if (numberOfGasBuildings <= 0) {
            requestFirstBuildingIfNeeded();
        }
        else {
            requestAdditionalBuildingIfNeeded();
        }
    }

    private void requestFirstBuildingIfNeeded() {
        if (A.minerals() >= 435 || Count.ourWithUnfinished(AUnitType.Protoss_Cybernetics_Core) > 0) {
//            System.err.println("@ " + A.now() + " - requested first gas building @" + A.supplyUsed() + ", min:" +
//                A.minerals() + " / res: " + A.reservedMinerals());
            AddToQueue.withStandardPriority(AtlantisRaceConfig.GAS_BUILDING);
        }
    }

    private static boolean tooEarlyForAnotherGasBuilding() {
        if (Count.existingOrInProduction(AtlantisRaceConfig.GAS_BUILDING) >= 1) {
            if (!A.hasMinerals(160) || A.supplyTotal() <= 30) {
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
    private static void requestAdditionalBuildingIfNeeded() {
        if (AGame.supplyUsed() <= 18) {
            return;
        }

//        if (AGame.everyNthGameFrame(37)) {
//            return;
//        }

        // =========================================================

        int numberOfBases = Select.ourBases().count();
//        int numberOfGasBuildings = Select.ourWithUnfinished().ofType(AtlantisRaceConfig.GAS_BUILDING).count();
        numberOfGasBuildings = Count.withPlanned(AtlantisRaceConfig.GAS_BUILDING);
        if (
            numberOfBases >= 2
                && numberOfBases > numberOfGasBuildings
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
