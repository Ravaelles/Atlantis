package atlantis.game;

import atlantis.combat.squad.ASquadManager;
import atlantis.combat.squad.NewUnitsToSquadsAssigner;
import atlantis.config.AtlantisConfig;
import atlantis.information.enemy.EnemyInfo;
import atlantis.production.constructing.ConstructionOrder;
import atlantis.production.constructing.ConstructionOrderStatus;
import atlantis.production.constructing.ConstructionRequests;
import atlantis.production.orders.production.ProductionQueueRebuilder;
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

        EnemyInfo.removeDiscoveredUnit(unit);
        unit.refreshType();

        // Geyser fix - destroyed enemy assimilator is detected as enemy fogged unit
        if (unit.isEnemy() && !unit.u().getType().isNeutral()) {
            EnemyInfo.refreshEnemyUnit(unit);
        }

        // =========================================================

        // Our unit
        if (unit.isOur()) {

            // === Fix for Zerg Extractor ========================================
            // Detect morphed gas building meaning construction has just started
            if (unit.type().isGasBuilding()) {
                for (ConstructionOrder order : ConstructionRequests.all()) {
                    if (order.buildingType().equals(AtlantisConfig.GAS_BUILDING)
                            && order.status().equals(ConstructionOrderStatus.CONSTRUCTION_NOT_STARTED)) {
                        order.setConstruction(unit);
                        break;
                    }
                }
            }

            // =========================================================

            ProductionQueueRebuilder.rebuildProductionQueueToExcludeProducedOrders();

            // Add to combat squad if it's military unit
            if (unit.isRealUnit()) {
                ASquadManager.removeUnitFromSquads(unit);
                NewUnitsToSquadsAssigner.possibleCombatUnitCreated(unit);
            }
        }

        // Enemy unit
        else {
            EnemyInfo.refreshEnemyUnit(unit);
        }
    }
}