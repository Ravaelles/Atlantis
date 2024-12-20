package atlantis.production.dynamic.protoss;

import atlantis.architecture.Commander;
import atlantis.config.AtlantisRaceConfig;
import atlantis.game.A;
import atlantis.production.orders.production.queue.CountInQueue;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnit;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.units.select.Select;
import atlantis.util.We;

import static atlantis.units.AUnitType.Protoss_Cybernetics_Core;

public class ProtossNewGasBuildingCommander extends Commander {

    @Override
    public boolean applies() {
        return We.protoss()
            && Count.bases() >= 2
            && A.supplyUsed() >= 55
            && A.everyNthGameFrame(85)
            && (A.gas() < A.minerals() && A.minerals() >= 105)
            && (A.gas() <= 150 || Count.ourCombatUnits() >= 12)
            && Count.bases() > Count.gasBuildingsWithUnfinished()
            && (CountInQueue.count(AtlantisRaceConfig.GAS_BUILDING) * 250 <= A.minerals() || A.minerals() >= 300)
            && Have.existingOrUnfinished(Protoss_Cybernetics_Core);
//            && !tooEarlyForAnotherGasBuilding()
    }

    @Override
    protected void handle() {
        requestAdditionalBuilding();
    }

    /**
     * Build Refineries/Assimilators/Extractors when it makes sense.
     */
    private static void requestAdditionalBuilding() {
        AUnit freeGeyser = baseWithFreeGeyser();

        if (freeGeyser != null) {
            ProductionOrder order = AddToQueue.withHighPriority(AtlantisRaceConfig.GAS_BUILDING);

//            if (order == null) return;
//            A.errPrintln("Request PROTOSS GAS, sup:" + A.supplyUsed() + " at " + freeGeyser
//                + " / " + order
//                + " / pos: " + order.atPosition()
//                + " (" + (order.atPosition() == null ? "-" : Select.main().distTo(order.atPosition())) + ")"
//            );
        }
    }

    private static AUnit baseWithFreeGeyser() {
        int maxDistBaseToGeyser = 10;

        for (AUnit base : Select.ourBases().list()) {
            if (Select.ourOfTypeWithUnfinished(AtlantisRaceConfig.GAS_BUILDING)
                .inRadius(maxDistBaseToGeyser, base)
                .notEmpty()
            ) continue;

            AUnit geyser = Select.geysers().inRadius(maxDistBaseToGeyser, base).nearestTo(base);
            if (geyser != null) {
                return geyser;
            }
        }

        return null;
    }
}
