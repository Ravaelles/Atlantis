package atlantis.production.dynamic;

import atlantis.architecture.Commander;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.generic.ArmyStrength;
import atlantis.information.strategy.AStrategy;
import atlantis.information.strategy.EnemyStrategy;
import atlantis.production.constructing.ConstructionRequests;
import atlantis.production.dynamic.expansion.ExpansionCommander;
import atlantis.production.orders.build.AddToQueue;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Select;

import static atlantis.util.Helpers.hasRequiredUnitFor;

public class DynamicBuildingsCommander extends Commander {
    @Override
    protected Class<? extends Commander>[] subcommanders() {
        return new Class[]{
            ExpansionCommander.class,
            NewGasBuildingCommander.class,
        };
    }

    // =========================================================

    protected static AStrategy enemyStrategy() {
        return EnemyStrategy.get();
    }

    // =========================================================

    protected static void buildToHaveOne(int minSupply, AUnitType type) {
        if (AGame.supplyUsed() >= minSupply) {
            buildToHaveOne(type);
        }
    }

    protected static void buildToHaveOne(AUnitType type) {
        if (Count.withPlanned(type) > 0) {
            return;
        }

        buildNow(type, true);
    }

    protected static void buildIfHaveMineralsAndGas(AUnitType type) {
        buildIfHaveMineralsAndGas(type, true, type.getMineralPrice() + 100, type.getGasPrice() + 50);
    }

    protected static void buildIfCanAffordWithReserved(AUnitType type) {
        buildIfCanAffordWithReserved(type, true, type.getMineralPrice(), type.getGasPrice());
    }

    protected static boolean buildIfAllBusyButCanAfford(AUnitType type, int extraMinerals, int extraGas) {
        if (Count.inProductionOrInQueue(type) > 0) return false;

        if (Select.ourOfType(type).areAllBusy()) {
            return buildIfHaveMineralsAndGas(type, true, type.getMineralPrice() + extraMinerals, type.getGasPrice() + extraGas);
        }

        return false;
    }

    protected static boolean buildIfHaveMineralsAndGas(AUnitType type, boolean onlyOneAtTime, int hasMinerals, int hasGas) {
        if (!AGame.canAfford(hasMinerals, hasGas)) return false;

        return buildNow(type, onlyOneAtTime);
    }

    protected static void buildIfCanAffordWithReserved(AUnitType type, boolean onlyOneAtTime, int hasMinerals, int hasGas) {
        if (!AGame.canAffordWithReserved(hasMinerals, hasGas)) {
            return;
        }

        buildNow(type, onlyOneAtTime);
    }

    protected static void buildNow(AUnitType type) {
        buildNow(type, false);
    }

    protected static boolean buildNow(AUnitType type, boolean onlyOneAtTime) {
        if (onlyOneAtTime && ConstructionRequests.hasRequestedConstructionOf(type)) return false;

        if (!hasRequiredUnitFor(type)) {
            buildToHaveOne(type.whatIsRequired());
            return false;
        }

        AddToQueue.withTopPriority(type);
        return true;
    }

    protected static boolean haveNoExistingOrPlanned(AUnitType type) {
        if (Count.ofType(type) > 0) return false;

        return Count.existingOrInProductionOrInQueue(type) == 0;
    }

    protected static boolean addWithTopPriorityThisOrItsRequirement(AUnitType target, AUnitType itsRequirement) {
        if (haveNoExistingOrPlanned(itsRequirement)) {
            AddToQueue.withHighPriority(itsRequirement);
            return true;
        }
        else {
            AddToQueue.withTopPriority(target);
            return true;
        }
    }

    // =========================================================

    protected static boolean isItSafeToAddTechBuildings() {
        if (EnemyStrategy.get().isRushOrCheese()) {
            if (ArmyStrength.ourArmyRelativeStrength() <= 80 && !A.hasMineralsAndGas(250, 100)) return false;
        }

        AUnit enemyUnitInMainBase = EnemyInfo.enemyUnitInMainBase();
        if (enemyUnitInMainBase == null || enemyUnitInMainBase.effUndetected()) return false;

        return true;
    }

}
