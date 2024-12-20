package atlantis.game.listeners;

import atlantis.Atlantis;
import atlantis.combat.missions.Missions;
import atlantis.combat.squad.transfers.SquadTransfersCommander;
import atlantis.debug.OurWorkerWasKilled;
import atlantis.game.A;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.enemy.UnitsArchive;
import atlantis.information.generic.ArmyStrength;
import atlantis.map.path.OurClosestBaseToEnemy;
import atlantis.production.orders.production.queue.Queue;
import atlantis.terran.repair.RepairAssignments;
import atlantis.units.AUnit;
import atlantis.units.select.Select;

public class OnUnitDestroyed {
    public static void onUnitDestroyed(AUnit unit) {
        // Some ums maps have funky stuff happening at the start, exclude first 20 frames
        if (A.now() <= 20) return;

        if (unit.isOur() && unit.isBase()) OurClosestBaseToEnemy.clearCache();

        // Our unit
        if (unit.isOur() && unit.isRealUnit()) {
            onOurUnitDestroyed(unit);
        }
//        else if (unit.isEnemy() && unit.isRealUnit()) {
        else if (unit.isEnemy()) {
            onEnemyUnitDestroyed(unit);
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

    private static void onEnemyUnitDestroyed(AUnit unit) {
        EnemyInfo.removeDiscoveredUnit(unit);
        if (!unit.type().isGeyser()) {
            Atlantis.KILLED++;
            Atlantis.KILLED_RESOURCES += unit.type().getTotalResources();

            if (unit.type().isABuilding()) Atlantis.KILLED_BUILDINGS++;
        }
    }

    private static void printOurDeadUnit(AUnit unit) {
//        if (unit.type().isGasBuilding()) return;
//
//        System.err.println("@ " + A.now() + " - Our unit DIED: " + unit.typeWithUnitId());
//        System.err.println(unit.managerLogs().toString());
    }

    private static void onOurUnitDestroyed(AUnit unit) {
        printOurDeadUnit(unit);

        RepairAssignments.removeRepairer(unit);

//            ProductionQueueRebuilder.rebuildProductionQueueToExcludeProducedOrders();
        Queue.get().refresh();

        if (!unit.type().isGasBuilding()) {
            Atlantis.LOST++;
            Atlantis.LOST_RESOURCES += unit.type().getTotalResources();

            if (unit.type().isABuilding()) Atlantis.LOST_BUILDINGS++;
        }

        OurWorkerWasKilled.onWorkedKilled(unit);
        SquadTransfersCommander.removeUnitFromSquads(unit);

        // =========================================================

        if (
            unit.isMissionAttack()
                && A.s <= 60 * 7
                && ArmyStrength.ourArmyRelativeStrength() <= 85
                && !A.isUms()
        ) {
            Missions.forceGlobalMissionDefend("Far too weak to attack!");
        }
    }

}
