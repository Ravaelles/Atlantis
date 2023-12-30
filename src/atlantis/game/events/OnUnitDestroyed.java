package atlantis.game.events;

import atlantis.Atlantis;
import atlantis.combat.missions.Missions;
import atlantis.combat.squad.transfers.SquadTransfersCommander;
import atlantis.debug.OurWorkerWasKilled;
import atlantis.game.A;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.enemy.UnitsArchive;
import atlantis.information.generic.ArmyStrength;
import atlantis.information.generic.OurArmyStrength;
import atlantis.map.path.OurClosestBaseToEnemy;
import atlantis.production.orders.production.queue.Queue;
import atlantis.terran.repair.RepairAssignments;
import atlantis.units.AUnit;
import atlantis.units.select.Select;

public class OnUnitDestroyed {
    public static void update(AUnit unit) {
        if (unit.isOur() && unit.isBase()) OurClosestBaseToEnemy.clearCache();

        // Our unit
        if (unit.isOur() && unit.isRealUnit()) {
            handleForOurUnit(unit);
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

    private static void handleForOurUnit(AUnit unit) {
        RepairAssignments.removeRepairer(unit);

//            ProductionQueueRebuilder.rebuildProductionQueueToExcludeProducedOrders();
        Queue.get().refresh();

        if (!unit.type().isGasBuilding()) {
            Atlantis.LOST++;
            Atlantis.LOST_RESOURCES += unit.type().getTotalResources();
        }

        OurWorkerWasKilled.onWorkedKilled(unit);
        SquadTransfersCommander.removeUnitFromSquads(unit);

        // =========================================================

        if (unit.isMissionAttack() && ArmyStrength.ourArmyRelativeStrength() <= 85 && !A.isUms()) {
            Missions.forceGlobalMissionDefend("Far too weak to attack!");
        }
    }

}
