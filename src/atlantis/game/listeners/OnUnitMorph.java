package atlantis.game.listeners;

import atlantis.combat.squad.NewUnitsToSquadsAssigner;
import atlantis.combat.squad.transfers.SquadTransfersCommander;
import atlantis.config.AtlantisRaceConfig;
import atlantis.game.A;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.enemy.EnemyUnitsUpdater;
import atlantis.information.strategy.response.protoss.AsProtossUnitDiscoveredResponse;
import atlantis.production.constructing.Construction;
import atlantis.production.constructing.ConstructionOrderStatus;
import atlantis.production.constructing.ConstructionRequests;
import atlantis.production.constructing.protoss.ProtossWarping;
import atlantis.production.orders.production.queue.Queue;
import atlantis.units.AUnit;

public class OnUnitMorph {

    /**
     * Called when a unit changes its AUnitType.
     * <p>
     * For example, when a Drone transforms into a Hatchery, a Siege Tank uses Siege Mode, or a Vespene Geyser
     * receives a Refinery.
     */
    public static void update(AUnit unit) {
        if (unit == null) {
            return;
        }

        EnemyUnitsUpdater.removeFoggedUnit(unit);
        unit.refreshType();
        unit.removeTooltip();

        // =========================================================

        // Our unit
        if (unit.isOur()) {
            updateGasBuildingSpecialCaseWhereGeyserMorphsIntoAGasBuilding(unit);
            ProtossWarping.updateNewBuildingJustWarped(unit);
            releaseReservedResources(unit);

            // =========================================================

//            ProductionQueueRebuilder.rebuildProductionQueueToExcludeProducedOrders();
            Queue.get().refresh();

            // Add to combat squad if it's military unit
            if (unit.isRealUnit()) {
                SquadTransfersCommander.removeUnitFromSquads(unit);
                (new NewUnitsToSquadsAssigner(unit)).possibleCombatUnitCreated();
            }
        }

        // Enemy unit
        else if (unit.isEnemy()) {
            EnemyInfo.refreshEnemyUnit(unit);

            if (unit.isLurkerEgg()) AsProtossUnitDiscoveredResponse.updateEnemyUnitDiscovered(unit);
        }
    }

    private static void updateGasBuildingSpecialCaseWhereGeyserMorphsIntoAGasBuilding(AUnit unit) {
        if (unit.type().isGasBuilding()) {
            for (Construction order : ConstructionRequests.all()) {
                if (order.buildingType().equals(AtlantisRaceConfig.GAS_BUILDING)
                    && order.status().equals(ConstructionOrderStatus.NOT_STARTED)) {
                    order.setBuild(unit);
                    break;
                }
            }
        }
    }

    private static void releaseReservedResources(AUnit unit) {
        Construction construction = unit.construction();
        if (construction == null && unit.isABuilding() && !unit.type().isAddon()) {
            A.errPrintln("No construction for " + unit);
        }
        if (construction != null) {
            construction.releaseReservedResources();
        }
    }
}
