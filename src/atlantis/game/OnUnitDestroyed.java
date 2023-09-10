package atlantis.game;

import atlantis.Atlantis;
import atlantis.combat.squad.transfers.SquadTransfersCommander;
import atlantis.debug.OurWorkerWasKilled;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.enemy.UnitsArchive;
import atlantis.production.orders.production.queue.Queue;
import atlantis.terran.repair.RepairAssignments;
import atlantis.units.AUnit;
import atlantis.units.select.Select;

public class OnUnitDestroyed {
    public static void update(AUnit unit) {

        // Our unit
        if (unit.isOur() && unit.isRealUnit()) {
            RepairAssignments.removeRepairer(unit);

//            ProductionQueueRebuilder.rebuildProductionQueueToExcludeProducedOrders();
            Queue.get().refresh();

            if (!unit.type().isGasBuilding()) {
                Atlantis.LOST++;
                Atlantis.LOST_RESOURCES += unit.type().getTotalResources();
            }

            OurWorkerWasKilled.onWorkedKilled(unit);
            SquadTransfersCommander.removeUnitFromSquads(unit);
        }
//        else if (unit.isEnemy() && unit.isRealUnit()) {
        else if (unit.isEnemy()) {
            EnemyInfo.removeDiscoveredUnit(unit);
            if (!unit.type().isGeyser()) {
                Atlantis.KILLED++;
                Atlantis.KILLED_RESOURCES += unit.type().getTotalResources();
            }
        }

        // Needs to be at the end, otherwise unit is reported as dead too early
        UnitsArchive.markUnitAsDestroyed(unit);

        // =========================================================

        if (A.now() >= 50 && A.isUms() && A.supplyUsed() == 0 && Select.ourCombatUnits().isEmpty()) {
            System.out.println("### ROUND END at " + A.seconds() + "s ###");
            UnitsArchive.paintLostUnits();
            UnitsArchive.paintKilledUnits();
        }
    }

}
