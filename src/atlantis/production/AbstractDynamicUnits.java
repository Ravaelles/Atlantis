package atlantis.production;

import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.orders.production.queue.add.AddToQueue;

import atlantis.production.orders.requirements.Requirements;
import atlantis.production.orders.production.queue.SoonInQueue;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.util.Helpers;

public class AbstractDynamicUnits extends Helpers {
    public static boolean buildToHave(AUnitType type, int haveN) {
        if (haveN <= 0) return false;

        if (Count.withPlanned(type) < haveN) {
//            System.err.println(Count.withPlanned(type) + " A/E " + haveN);
            if (type.isABuilding()) return AddToQueue.toHave(type, haveN);
            else return trainIfPossible(type);
        }

        return false;
    }

    public static boolean trainIfPossible(int minSupply, AUnitType type, boolean onlyOneAtTime) {
//        if (supplyUsedAtMost(minSupply)) return false;

        return trainIfPossible(type, onlyOneAtTime, type.mineralPrice(), type.gasPrice());
    }

    public static boolean trainIfPossible(AUnitType type) {
        return trainIfPossible(type, false, type.mineralPrice(), type.gasPrice());
    }

    public static boolean trainIfPossible(AUnitType type, boolean onlyOneAtTime) {
        return trainIfPossible(type, onlyOneAtTime, 0, 0);
    }

    public static boolean trainIfPossible(AUnitType type, boolean onlyOneAtTime, int hasMinerals, int hasGas) {
        if (!A.canAffordWithReserved(hasMinerals, hasGas)) return false;

        if (onlyOneAtTime) {
            if (type.isABuilding() && ConstructionRequests.hasRequestedConstructionOf(type)) return false;
        }

        return AddToQueue.addToQueueIfHaveFreeBuilding(type);
    }

    public static boolean trainNowIfHaveWhatsRequired(AUnitType type, boolean onlyOneAtTime) {
        if (!onlyOneAtTime) {
            AGame.exit("Unhandled yet");
        }

        if (!Requirements.hasRequirements(type)) return false;
        if (SoonInQueue.have(type)) return false;

        return AddToQueue.addToQueueIfHaveFreeBuilding(type);
    }

}
