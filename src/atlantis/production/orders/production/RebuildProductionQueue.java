package atlantis.production.orders.production;

import atlantis.combat.missions.Mission;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.information.tech.ATech;
import atlantis.production.constructing.ConstructionRequests;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.util.We;
import bwapi.TechType;
import bwapi.UpgradeType;

import java.util.ArrayList;

public class RebuildProductionQueue {
    private final ProductionQueueMode mode;
    private final ArrayList<ProductionOrder> queue;
    private final int[] resourcesNeededForNotStartedBuildings;
    private AUnitType unitOrBuilding;
    private UpgradeType upgrade;
    private TechType tech;
    private Mission mission;
    private boolean hasRequirements;
    private boolean canAfford;
    private int countCanNotAfford;

    public RebuildProductionQueue(ProductionQueueMode mode) {
        this.mode = mode;

        queue = new ArrayList<>();
        resourcesNeededForNotStartedBuildings = ConstructionRequests.resourcesNeededForNotStarted();
    }

    protected ArrayList<ProductionOrder> rebuildQueue() {
        ProductionQueue.mineralsNeeded = resourcesNeededForNotStartedBuildings[0];
        ProductionQueue.gasNeeded = resourcesNeededForNotStartedBuildings[1];

        // =========================================================
        // The idea as follows: as long as we can afford next enqueued production order,
        // add it to the list. So at any given moment we can either produce nothing, one unit
        // or even multiple units (if we have all the minerals, gas and techs/buildings required).

        countCanNotAfford = 0;
        for (ProductionOrder order : ProductionQueue.nextInQueue) {
            initializeVariablesForOrder(order);

            if (waitForProtossFirstPylon(mode)) continue;
            if (handleOrderBeingMissionEnforcement(order)) continue;

            reserveResourcesForThisOrder();

            // =========================================================

            // If we can afford this order (and all previous ones as well), add it to CurrentToProduceList.
            if (shouldAddOrderToQueue(order)) {
                addOrderToCurrentQueue(order);
            }

            // We can't afford to produce this order (possibly other, previous orders are blocking it).
            // Return current list of production orders (can be empty).
            else if (shouldReturnQueue()) break;
        }

        //noinspection unchecked
        return (ArrayList<ProductionOrder>) queue.clone();
    }

    private boolean shouldReturnQueue() {
        return ++countCanNotAfford >= 5;
    }

    private boolean shouldAddOrderToQueue(ProductionOrder order) {
        if (mode == ProductionQueueMode.ENTIRE_QUEUE || hasRequirements) {
            return unitOrBuilding == null || A.hasFreeSupply(unitOrBuilding.supplyNeeded());
        }

        return false;
    }

    private void addOrderToCurrentQueue(ProductionOrder order) {
        order.setHasWhatRequired(hasRequirements);
        queue.add(order);
    }

    private boolean handleOrderBeingMissionEnforcement(ProductionOrder order) {
        return mission != null && A.supplyAtLeast(order.minSupply());
    }

    private boolean waitForProtossFirstPylon(ProductionQueueMode mode) {
        return We.protoss()
            && mode == ProductionQueueMode.REQUIREMENTS_FULFILLED
            && (unitOrBuilding != null && !unitOrBuilding.isPylon())
            && Count.existingOrInProductionOrInQueue(AUnitType.Protoss_Pylon) == 0;
    }

    private void reserveResourcesForThisOrder() {
        // UNIT/BUILDING
        if (unitOrBuilding != null && hasRequirements && canAfford) {
            if (hasFreeBuildingToProduceUnit(unitOrBuilding) && (unitOrBuilding.isBuilding() || !CurrentProductionQueue.hasUnitInQueue(unitOrBuilding, queue))) {
                ProductionQueue.mineralsNeeded += unitOrBuilding.getMineralPrice();
                ProductionQueue.gasNeeded += unitOrBuilding.getGasPrice();
            }
        }

        // UPGRADE
        else if (upgrade != null && hasRequirements) {
            ProductionQueue.mineralsNeeded += upgrade.mineralPrice() * (1 + ATech.getUpgradeLevel(upgrade));
            ProductionQueue.gasNeeded += upgrade.gasPrice() * (1 + ATech.getUpgradeLevel(upgrade));
        }

        // TECH
        else if (tech != null && hasRequirements) {
            ProductionQueue.mineralsNeeded += tech.mineralPrice();
            ProductionQueue.gasNeeded += tech.gasPrice();
        }
    }

    private void initializeVariablesForOrder(ProductionOrder order) {
        hasRequirements = AGame.hasSupply(order.minSupply()) && Requirements.hasRequirements(order);
        canAfford = AGame.canAfford(
            ProductionQueue.mineralsNeeded + order.mineralPrice(),
            ProductionQueue.gasNeeded + order.gasPrice()
        );

        order.setCanAffordNow(canAfford);
        unitOrBuilding = order.unitType();
        upgrade = order.upgrade();
        tech = order.tech();
        mission = order.mission();
    }

    private boolean hasFreeBuildingToProduceUnit(AUnitType unit) {
        if (We.zerg()) return Count.ofType(AUnitType.Zerg_Larva) > 0;

        AUnitType building = unit.whatBuildsIt();
        if (building != null) return Select.ourOfType(building).free().isNotEmpty();

        return false;
    }
}
