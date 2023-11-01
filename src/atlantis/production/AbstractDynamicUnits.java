package atlantis.production;

import atlantis.game.AGame;
import atlantis.production.constructing.ConstructionRequests;
import atlantis.production.orders.production.queue.add.AddToQueue;

import atlantis.production.orders.production.Requirements;
import atlantis.production.orders.production.queue.SoonInQueue;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.util.Helpers;

public class AbstractDynamicUnits extends Helpers {
    public static void buildToHave(AUnitType type, int haveN) {
        if (haveN <= 0) {
            return;
        }

        if (Count.withPlanned(type) < haveN) {
            trainIfPossible(type);
        }
    }

    public static boolean trainIfPossible(int minSupply, AUnitType type, boolean onlyOneAtTime) {
        if (noSupply(minSupply)) return false;

        return trainIfPossible(type, onlyOneAtTime, type.getMineralPrice(), type.getGasPrice());
    }

    public static boolean trainIfPossible(AUnitType type) {
        return trainIfPossible(type, false, type.getMineralPrice(), type.getGasPrice());
    }

    public static boolean trainIfPossible(AUnitType type, boolean onlyOneAtTime) {
        return trainIfPossible(type, onlyOneAtTime, 0, 0);
    }

    public static boolean trainIfPossible(AUnitType type, boolean onlyOneAtTime, int hasMinerals, int hasGas) {
        if (!AGame.canAffordWithReserved(hasMinerals, hasGas)) return false;

        if (onlyOneAtTime) {
            if (type.isABuilding() && ConstructionRequests.hasRequestedConstructionOf(type)) return false;
        }

        return AddToQueue.addToQueueIfHaveFreeBuilding(type);
    }

    public static void trainNowIfHaveWhatsRequired(AUnitType type, boolean onlyOneAtTime) {
        if (!onlyOneAtTime) {
            AGame.exit("Unhandled yet");
        }

        if (!Requirements.hasRequirements(type)) return;
        if (SoonInQueue.have(type)) return;

        AddToQueue.addToQueueIfHaveFreeBuilding(type);
    }

}
