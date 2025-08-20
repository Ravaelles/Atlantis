package atlantis.production.dynamic.protoss.prioritize;

import atlantis.game.A;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.orders.production.queue.CountInQueue;
import atlantis.units.AUnitType;
import atlantis.units.select.Have;
import atlantis.util.We;

import static atlantis.units.AUnitType.Protoss_Photon_Cannon;

public class ProtossCriticalStuffInQueue {
    public static boolean hasEnoughResources() {
        return hasEnoughMinerals() && hasEnoughGas();
    }

    public static boolean hasEnoughMinerals() {
        if (!We.protoss()) return true;

        if (!A.hasMinerals(250)) {
            if (CountInQueue.countNotStarted(AUnitType.Protoss_Robotics_Facility) > 0) return false;
            if (CountInQueue.countNotStarted(AUnitType.Protoss_Photon_Cannon) > 0) return false;
        }

        return true;
    }

    public static boolean hasEnoughGas() {
        if (!We.protoss()) return true;

        if (!A.hasGas(250) && CountInQueue.countNotStarted(AUnitType.Protoss_Robotics_Facility) > 0) {
            return false;
        }

        if (!A.hasGas(210) && CountInQueue.countNotStarted(AUnitType.Protoss_Observatory) > 0 && Have.factoryWithUnfinished()) {
            return false;
        }

        return true;
    }
}
