package atlantis.production.dynamic.protoss;

import atlantis.architecture.Commander;
import atlantis.config.AtlantisRaceConfig;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.units.AUnit;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.We;

public class ProtossNewGasBuildingCommander extends Commander {

    @Override
    public boolean applies() {
        return We.protoss()
            && A.everyNthGameFrame(85)
            && (A.gas() < A.minerals() && A.minerals() >= 270)
            && Count.bases() >= 2
            && Count.bases() > Count.gasBuildingsWithUnfinished()
            && (A.gas() <= 600 || A.minerals() >= 500)
            && (A.gas() <= 100 || Count.ourCombatUnits() >= 6)
            && Count.inProductionOrInQueue(AtlantisRaceConfig.GAS_BUILDING) <= (A.hasMinerals(300) ? 1 : 0);
//            && !tooEarlyForAnotherGasBuilding()
    }

    @Override
    protected void handle() {
        requestAdditionalBuildingIfNeeded();
    }

    /**
     * Build Refineries/Assimilators/Extractors when it makes sense.
     */
    private static void requestAdditionalBuildingIfNeeded() {
        AUnit freeGeyser = baseWithFreeGeyser();

        if (freeGeyser != null) {
//            A.errPrintln("Request GAS BUILDING at supply: " + A.supplyUsed() + " at " + freeGeyser);
            AddToQueue.withHighPriority(AtlantisRaceConfig.GAS_BUILDING);
        }
    }

    private static AUnit baseWithFreeGeyser() {
        for (AUnit base : Select.ourBases().list()) {
            if (Select.ourOfType(AtlantisRaceConfig.GAS_BUILDING).inRadius(10, base).isNotEmpty()) {
                continue;
            }

            AUnit geyser = Select.geysers().inRadius(10, base).nearestTo(base);
            if (geyser != null) {
                return geyser;
            }
        }

        return null;
    }
}
