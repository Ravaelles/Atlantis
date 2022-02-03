package atlantis.production;

import atlantis.combat.missions.Missions;
import atlantis.config.AtlantisConfig;
import atlantis.game.AGame;
import atlantis.information.tech.ATechRequests;
import atlantis.production.constructing.ConstructionRequests;
import atlantis.production.dynamic.ADynamicWorkerProductionManager;
import atlantis.production.orders.build.CurrentBuildOrder;
import atlantis.production.orders.production.CurrentProductionQueue;
import atlantis.production.orders.production.ProductionQueueMode;
import atlantis.production.orders.build.ZergBuildOrder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.util.We;
import bwapi.TechType;
import bwapi.UpgradeType;

import java.util.ArrayList;

public class AProductionManager {

    /**
     * Is responsible for training new units and issuing construction requests for buildings.
     */
    protected static void update() {

        // Get sequence of units (Production Orders) based on current build order
        ArrayList<ProductionOrder> queue = CurrentProductionQueue.thingsToProduce(ProductionQueueMode.ONLY_WHAT_CAN_AFFORD);
        for (ProductionOrder order : queue) {
            handleProductionOrder(order);
        }
    }

    private static void handleProductionOrder(ProductionOrder order) {

        // Produce UNIT
        if (order.unitType() != null) {
            AUnitType unitType = order.unitType();
//            System.out.println("PRODUCE NOW unitType = " + unitType);
            if (unitType.isBuilding()) {
                produceBuilding(unitType, order);
            } else {
                produceUnit(unitType);
            }
        }

        // =========================================================
        // Produce UPGRADE

        else if (order.upgrade() != null) {
            UpgradeType upgrade = order.upgrade();
            ATechRequests.researchUpgrade(upgrade);
        }

        // =========================================================
        // Produce TECH

        else if (order.tech() != null) {
            TechType tech = order.tech();
            ATechRequests.researchTech(tech);
        }

        // =========================================================
        // Mission CHANGE

        else if (order.mission() != null) {
            Missions.setGlobalMissionTo(order.mission());
        }

        // === Nothing! ============================================

        else {
            System.err.println(order + " was not handled at all!");
        }
    }

    // =========================================================
    // =========================================================
    // =========================================================

    public static boolean produceWorker() {
        AUnit base = Select.ourOneNotTrainingUnits(AtlantisConfig.BASE);
        if (base == null) {
            return false;
        }

        if (isSafeToProduceWorkerAt(base)) {
            return ADynamicWorkerProductionManager.produceWorker(base);
        }

        return false;
    }

    private static boolean produceUnit(AUnitType type) {
        assert !type.isBuilding();

        // Supply: OVERLORD / PYLON / DEPOT
//        if (AGame.supplyFree() == 0 && !unitType.isSupplyUnit()) {
//            // Supply production is handled by AtlantisSupplyManager
//            return false;
//        }

        // =========================================================
        // Worker

        if (type.equals(AtlantisConfig.WORKER)) {
            return produceWorker();
        } 

        // =========================================================
        // Non-worker

        else if (AGame.canAfford(type.getMineralPrice(), type.getGasPrice())) {
            return CurrentBuildOrder.get().produceUnit(type);
        }

        return false;
    }

    private static void produceBuilding(AUnitType type, ProductionOrder order) {
        assert type.isBuilding();

        if (type.isZerg()) {
            ZergBuildOrder.produceZergBuilding(type, order);
            return;
        }

        if (type.isAddon()) {
            produceAddon(type);
        } else {
            ConstructionRequests.requestConstructionOf(order);
        }
    }

    private static void produceAddon(AUnitType addon) {
        for (AUnit building : Select.ourOfType(addon.whatBuildsIt()).free().list()) {
            building.buildAddon(addon);
            return;
        }
    }

    private static boolean isSafeToProduceWorkerAt(AUnit base) {
        return Select.enemyCombatUnits().excludeTypes().inRadius(10, base).atMost(1);
    }

}
