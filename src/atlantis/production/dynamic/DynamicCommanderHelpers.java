package atlantis.production.dynamic;

import atlantis.architecture.Commander;
import atlantis.game.AGame;
import atlantis.information.strategy.AStrategy;
import atlantis.information.strategy.EnemyStrategy;
import atlantis.production.constructing.ConstructionRequests;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Select;

public class DynamicCommanderHelpers extends Commander {
    public static AStrategy enemyStrategy() {
        return EnemyStrategy.get();
    }

    public static void buildToHaveOne(int minSupply, AUnitType type) {
        if (AGame.supplyUsed() >= minSupply) {
            buildToHaveOne(type);
        }
    }

    public static void buildToHaveOne(AUnitType type) {
        if (Count.withPlanned(type) > 0) {
            return;
        }

        buildNow(type, true);
    }

    public static void buildIfHaveMineralsAndGas(AUnitType type) {
        buildIfHaveMineralsAndGas(type, true, type.getMineralPrice() + 100, type.getGasPrice() + 50);
    }

    public static void buildIfCanAffordWithReserved(AUnitType type) {
        buildIfCanAffordWithReserved(type, true, type.getMineralPrice(), type.getGasPrice());
    }

    public static boolean buildIfAllBusyButCanAfford(AUnitType type, int extraMinerals, int extraGas) {
        if (Count.inProductionOrInQueue(type) > 0) return false;

        if (Select.ourOfType(type).areAllBusy()) {
            return buildIfHaveMineralsAndGas(type, true, type.getMineralPrice() + extraMinerals, type.getGasPrice() + extraGas);
        }

        return false;
    }

    public static boolean buildIfHaveMineralsAndGas(AUnitType type, boolean onlyOneAtTime, int hasMinerals, int hasGas) {
        if (!AGame.canAfford(hasMinerals, hasGas)) return false;

        return buildNow(type, onlyOneAtTime);
    }

    public static void buildIfCanAffordWithReserved(AUnitType type, boolean onlyOneAtTime, int hasMinerals, int hasGas) {
        if (!AGame.canAffordWithReserved(hasMinerals, hasGas)) {
            return;
        }

        buildNow(type, onlyOneAtTime);
    }

    public static void buildNow(AUnitType type) {
        buildNow(type, false);
    }

    public static boolean buildNow(AUnitType type, boolean onlyOneAtTime) {
        if (onlyOneAtTime && ConstructionRequests.hasRequestedConstructionOf(type)) return false;

        if (!type.hasRequiredUnit()) {
            buildToHaveOne(type.whatIsRequired());
            return false;
        }

        AddToQueue.withTopPriority(type);
        return true;
    }

    public static boolean haveNoExistingOrPlanned(AUnitType type) {
        if (Count.ofType(type) > 0) return false;

        return Count.existingOrInProductionOrInQueue(type) == 0;
    }

    public static boolean addWithTopPriorityThisOrItsRequirement(AUnitType target, AUnitType itsRequirement) {
        if (haveNoExistingOrPlanned(itsRequirement)) {
            AddToQueue.withHighPriority(itsRequirement);
            return true;
        }
        else {
            AddToQueue.withTopPriority(target);
            return true;
        }
    }
}