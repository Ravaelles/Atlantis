package atlantis.production.dynamic;

import atlantis.architecture.Commander;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.information.strategy.AStrategy;
import atlantis.information.strategy.EnemyStrategy;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.orders.production.queue.CountInQueue;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Select;

public class DynamicCommanderHelpers extends Commander {
    public static AStrategy enemyStrategy() {
        return EnemyStrategy.get();
    }

    public static boolean buildToHaveOne(int minSupply, AUnitType type) {
        if (AGame.supplyUsed() >= minSupply) {
            return buildToHaveOne(type);
        }

        return false;
    }

    public static boolean buildToHaveOne(AUnitType type) {
        if (Count.withPlanned(type) > 0) {
            return false;
        }

//        System.out.println("@" + A.now() + " buildToHaveOne " + type);
        return buildNow(type, true);
    }

    public static void buildIfHaveMineralsAndGas(AUnitType type) {
        buildIfHaveMineralsAndGas(type, true, type.mineralPrice() + 100, type.gasPrice() + 50);
    }

    public static void buildIfCanAffordWithReserved(AUnitType type) {
        buildIfCanAffordWithReserved(type, true, type.mineralPrice(), type.gasPrice());
    }

    public static boolean buildIfAllBusyButCanAfford(AUnitType type, int extraMinerals, int extraGas) {
        if (Count.inProductionOrInQueue(type) > 0) return false;

        if (Select.ourOfType(type).areAllBusy()) {
            return buildIfHaveMineralsAndGas(type, true, type.mineralPrice() + extraMinerals, type.gasPrice() + extraGas);
        }

        return false;
    }

    public static boolean buildIfHaveMineralsAndGas(AUnitType type, boolean onlyOneAtTime, int hasMinerals, int hasGas) {
        if (!A.canAfford(hasMinerals, hasGas)) return false;

        return buildNow(type, onlyOneAtTime);
    }

    public static void buildIfCanAffordWithReserved(AUnitType type, boolean onlyOneAtTime, int hasMinerals, int hasGas) {
        if (!A.canAffordWithReserved(hasMinerals, hasGas)) {
            return;
        }

        buildNow(type, onlyOneAtTime);
    }

    public static boolean buildNow(AUnitType type) {
        return buildNow(type, false);
    }

    public static boolean buildNow(AUnitType type, boolean onlyOneAtTime) {
        if (onlyOneAtTime && (
            CountInQueue.count(type, 5) > 0
                || ConstructionRequests.hasRequestedConstructionOf(type)
        )) return false;

        if (!type.hasRequiredUnit()) {
            buildToHaveOne(type.whatIsRequired());
            return false;
        }

        AddToQueue.withTopPriority(type);
        return true;
    }

    public static boolean haveNoExistingOrPlanned(AUnitType type) {
//        if (Count.ofType(type) > 0) return false;

        return Count.existingOrInProductionOrInQueue(type) == 0;
    }

//    public static boolean addWithTopPriorityThisOrItsRequirement(AUnitType target, AUnitType itsRequirement) {
//        if (haveNoExistingOrPlanned(itsRequirement)) {
//            AddToQueue.withHighPriority(itsRequirement);
//            return true;
//        }
//        else {
//            AddToQueue.withTopPriority(target);
//            return true;
//        }
//    }
}
