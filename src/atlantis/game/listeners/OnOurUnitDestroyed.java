package atlantis.game.listeners;

import atlantis.Atlantis;
import atlantis.combat.missions.Missions;
import atlantis.combat.squad.transfers.SquadTransfersCommander;
import atlantis.debug.OurWorkerWasKilled;
import atlantis.game.A;
import atlantis.game.event.Events;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.generic.ArmyStrength;
import atlantis.production.orders.production.queue.Queue;
import atlantis.terran.repair.RepairAssignments;
import atlantis.units.AUnit;

public class OnOurUnitDestroyed {
    public static void update(AUnit unit) {
        printOurDeadUnit(unit);

        RepairAssignments.removeRepairer(unit);

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
                && EnemyUnits.discovered().ranged().count() >= 2
        ) {
            Missions.forceGlobalMissionDefend("Far too weak to attack!");
        }

        Events.dispatch("OurBunkerDestroyed", unit);
    }

    private static void printOurDeadUnit(AUnit unit) {
        if (unit.type().isGasBuilding()) return;

//        System.err.println(A.minSec() + " - Our " + unit.typeWithUnitId() + " DIED");
        System.out.println("___DEAD_" + unit.type().name() + " at " + A.minSec() + "______");
        System.out.println(unit.managerLogs().toString());
        System.out.println("_____________________________");
    }
}
